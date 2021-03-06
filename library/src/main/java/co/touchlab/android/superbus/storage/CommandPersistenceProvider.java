package co.touchlab.android.superbus.storage;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.SuperbusProcessor;
import co.touchlab.android.superbus.SuperbusService;
import co.touchlab.android.superbus.errorcontrol.StorageException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.storage.sqlite.CursorIntf;
import co.touchlab.android.superbus.storage.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.storage.sqlite.SQLiteDatabaseIntf;
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
public class CommandPersistenceProvider implements PersistenceProvider
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

    public CommandPersistenceProvider(SQLiteDatabaseFactory databaseFactory, StoredCommandAdapter storedCommandAdapter, BusLog log)
    {
        this.databaseFactory = databaseFactory;
        this.storedCommandAdapter = storedCommandAdapter;
        this.log = log == null ? new BusLogImpl() : log;
    }

    public BusLog getLog()
    {
        return log;
    }

    public final synchronized void put(final Context context, final Command c) throws StorageException
    {
        //There may be serious I/O going on here.  Assert we're OK for that.
        UiThreadContext.assertBackgroundThread();
        if(context == null)
            throw new StorageException("Can't save without context");

        loadInitialCommands();

        boolean duplicate = checkHasDuplicate(c);

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

    /**
     * There's a bit of an issue here.  If a command is currently in process, it'll be queried for 'same'.  However,
     * from a temporally complete perspective, its conceivable that you wouldn't want that.  However, its difficult
     * to temporarily remove the command, and checking the running flag is tricky.
     *
     * In theory, if its super important, the command itself could check if the compared one is running.
     *
     * @param c
     * @return
     */
    private boolean checkHasDuplicate(Command c)
    {
        boolean duplicate = false;

        for (Command command : commandQueue)
        {
            if (c.same(command))
            {
                duplicate = true;
                break;
            }
        }
        return duplicate;
    }

    public synchronized Command readTop() throws StorageException
    {
        loadInitialCommands();
        return commandQueue.peek();
    }

    public synchronized void persistCommand(Context context, Command command) throws StorageException
    {
        //Sanity check. StoredCommand classes need a no-arg constructor
        checkNoArg(command);

        try
        {
            ContentValues values = prepCommandSave(command);

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

    private ContentValues prepCommandSave(Command command) throws StorageException
    {
        String commandData = storedCommandAdapter.storeCommand(command);

        ContentValues values = new ContentValues();

        values.put("type", command.getClass().getName());
        values.put("commandData", commandData);
        return values;
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

    public synchronized void removeCommand(Command command) throws StorageException
    {
        try
        {
            int removedCount = databaseFactory.getDatabase().delete(TABLE_NAME, "id = ?", new String[]{command.getId().toString()});
            if(removedCount != 1)
                throw new StorageException("Deleted count != 1, was "+ removedCount);
            commandQueue.remove(command);
        }
        catch (Exception e)
        {
            if(e instanceof StorageException)
                throw (StorageException)e;
            else
                throw new StorageException(e);
        }
    }

    private Command loadFromCursor(CursorIntf c) throws Exception
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
    public void runInTransaction(Runnable r)
    {
        SQLiteDatabaseIntf db = databaseFactory.getDatabase();
        db.beginTransaction();

        try
        {
            r.run();
            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
        }
    }

    public synchronized int getSize() throws StorageException
    {
        loadInitialCommands();
        return commandQueue.size();
    }

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
            SQLiteDatabaseIntf db = databaseFactory.getDatabase();

            //Run query in a transaction to block changes while loading.  Probably not critical, but good for consistency
            db.beginTransaction();

            try
            {
                CursorIntf cursor = db.query(TABLE_NAME, COLUMN_LIST);

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

                db.setTransactionSuccessful();

                return commands;
            }
            finally
            {
                db.endTransaction();
            }
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    public static void createTables(SQLiteDatabaseIntf database) throws StorageException
    {
        database.execSQL("create table "+ TABLE_NAME +" ("+ COLUMNS +")");
    }

    public static void dropTables(SQLiteDatabaseIntf database) throws StorageException
    {
        database.execSQL("drop table "+ TABLE_NAME);
    }
}
