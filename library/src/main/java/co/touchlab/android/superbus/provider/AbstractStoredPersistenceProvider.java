package co.touchlab.android.superbus.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.SuperbusProcessor;
import co.touchlab.android.superbus.SuperbusService;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.provider.stringbased.StoredCommandAdapter;
import co.touchlab.android.superbus.utils.UiThreadContext;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/13/12
 * Time: 5:16 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractStoredPersistenceProvider implements PersistenceProvider
{
    public static final String TABLE_NAME = "__SQL_PERS_PROV";
    public static final String COLUMNS = "id INTEGER PRIMARY KEY AUTOINCREMENT, type VARCHAR, commandData VARCHAR";
    public static final String[] COLUMN_LIST = {"id", "type", "commandData"};

    private Set<Class> checkedCommandClasses = new HashSet<Class>();
    private BusLog log;
    private SQLiteDatabaseFactory databaseFactory;
    private StoredCommandAdapter storedCommandAdapter;

    private final PriorityQueue<Command> commandQueue = new PriorityQueue<Command>();
    private boolean initCalled = false;

    protected AbstractStoredPersistenceProvider(SQLiteDatabaseFactory databaseFactory, StoredCommandAdapter storedCommandAdapter, BusLog log)
    {
        this.databaseFactory = databaseFactory;
        this.storedCommandAdapter = storedCommandAdapter;
        this.log = log;
    }

    public BusLog getLog()
    {
        return log;
    }

    @Override
    public final synchronized void put(final Context context, final Command c) throws StorageException
    {
        //There may be serious I/O going on here.  Assert we're OK for that.
        UiThreadContext.assertBackgroundThread();
        if(context == null)
            throw new StorageException("Can't save without context");

        loadInitialCommands();

        boolean duplicate = false;

        for (Command command : commandQueue)
        {
            if (c.same(command))
            {
                duplicate = true;
                break;
            }
        }

        if (!duplicate)
        {
            try
            {
                persistCommand(context, c);
            }
            catch (StorageException e)
            {
                throw new RuntimeException(e);
            }

            commandQueue.add(c);
        }

        SuperbusService.notifyStart(context);
    }

    @Override
    protected synchronized Command readTop() throws StorageException
    {
        loadInitialCommands();
        return commandQueue.peek();
    }

    public void persistCommand(Context context, Command command) throws StorageException
    {
        //Sanity check. StoredCommand classes need a no-arg constructor
        checkNoArg(command);

        try
        {
            String commandData = storedCommandAdapter.storeCommand(command);

            ContentValues values = new ContentValues();

            values.put("type", command.getClass().getName());
            values.put("commandData", commandData);

            long newRowId = databaseFactory.getDatabase().insertOrThrow(
                    TABLE_NAME, "type", values
            );

            command.setId(newRowId);
        }
        catch (StorageException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    protected void checkNoArg(Command command) throws StorageException
    {
        Class<? extends Command> commandClass = command.getClass();

        if (checkedCommandClasses.contains(commandClass))
            return;

        boolean isNoArg = false;

        Constructor<?>[] constructors = commandClass.getConstructors();

        for (Constructor<?> constructor : constructors)
        {
            if (constructor.getParameterTypes().length == 0)
            {
                isNoArg = true;
                break;
            }
        }

        if (!isNoArg)
            throw new StorageException("All StoredCommand classes must have a no-arg constructor");

        checkedCommandClasses.add(commandClass);
    }

    protected void removeCommand(Command command) throws StorageException
    {
        try
        {
            databaseFactory.getDatabase().delete(TABLE_NAME, "id = ?", new String[]{command.getId().toString()});
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    private Command loadFromCursor(Cursor c) throws Exception
    {
        try
        {
            long id = c.getLong(0);
            String type = c.getString(1);
            String commandData = c.getString(2);

            Command storedCommand = storedCommandAdapter.inflateCommand(commandData, type);

            storedCommand.setId(id);

            return storedCommand;
        }
        catch (Exception e)
        {
            if(e instanceof ClassNotFoundException)
            {
                getLog().e(SuperbusProcessor.TAG, "Class cast on load. Nothing to do here. Be more careful.", e);
                return null;
            }
            else if(e instanceof StorageException)
                throw e;
            else
                throw new StorageException(e);
        }
    }

    public final synchronized void sendMessage(Context context, String message)
    {
        for (Command command : commandQueue)
        {
            command.onRuntimeMessage(context, message);
        }
    }

    public final synchronized void sendMessage(Context context, String message, Map args)
    {
        for (Command command : commandQueue)
        {
            command.onRuntimeMessage(context, message, args);
        }
    }

    @Override
    public final synchronized void queryAll(CommandQuery query)
    {
        for (Command command : commandQueue)
        {
            query.runQuery(command);
        }
    }

    @Override
    public int getSize() throws StorageException
    {
        loadInitialCommands();     //TODO: Not sure this is right
        return commandQueue.size();
    }

    @Override
    public synchronized void logPersistenceState()
    {
        if (log.isLoggable(SuperbusService.TAG, Log.INFO))
        {
            log.d(SuperbusService.TAG, "queue size: " + commandQueue.size());
            if (log.isLoggable(SuperbusService.TAG, Log.DEBUG))
            {
                int count = 0;

                for (Command command : commandQueue)
                {
                    log.d(SuperbusService.TAG, "command[" + count + "] {" + command.logSummary() + "}");
                    count++;
                }
            }
        }
    }

    /**
     * Load all commands from storage.
     */
    private synchronized void loadInitialCommands()
    {
        if (initCalled)
            return;

        Collection<? extends Command> c;
        try
        {
            c = loadAll();
        }
        catch (StorageException e)
        {
            throw new RuntimeException(e);
        }
        if (c != null)
            commandQueue.addAll(c);

        initCalled = true;
    }

    private Collection<? extends Command> loadAll() throws StorageException
    {
        try
        {
//            TODO: Replace for sqlcipher
//            SQLiteDatabaseIntf db = databaseFactory.getDatabase();
//            Cursor cursor = db.query(TABLE_NAME, COLUMN_LIST);

            SQLiteDatabase db = databaseFactory.getDatabase();
            Cursor cursor = db.query(TABLE_NAME, COLUMN_LIST, null, null, null, null, null, null);

            List<Command> commands = null;
            try
            {
                commands = new ArrayList<Command>();

                while (cursor.moveToNext())
                {
                    Command command = loadFromCursor(cursor);
                    if(command != null)
                        commands.add(command);
                }
            }
            finally
            {
                cursor.close();
            }

            return commands;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    public void createTables(SQLiteDatabase database)
    {
        database.execSQL("create table "+ TABLE_NAME +" ("+ COLUMNS +")");
    }

    public void dropTables(SQLiteDatabase database)
    {
        database.execSQL("drop table "+ TABLE_NAME);
    }
}
