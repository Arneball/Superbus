package co.touchlab.android.superbus;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;
import co.touchlab.android.superbus.provider.gson.GsonStoredCommandAdapter;
import co.touchlab.android.superbus.provider.sqlite.ClearSQLiteDatabase;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseIntf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 6:16 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractPersistedApplication extends Application implements PersistedApplication
{
    private CommandPersistenceProvider persistenceProvider;

    @Override
    public void onCreate()
    {
        super.onCreate();
        persistenceProvider = new CommandPersistenceProvider(new LocalDatabaseFactory(), new GsonStoredCommandAdapter(), null);
    }

    @Override
    public CommandPersistenceProvider getProvider()
    {
        return persistenceProvider;
    }

    @Override
    public BusLog getLog()
    {
        return null;
    }

    @Override
    public Collection<SuperbusEventListener> getEventListeners()
    {
        List<SuperbusEventListener> list = new ArrayList<SuperbusEventListener>(1);
        list.add(new TransientRetryBusEventListener());
        return list;
    }

    @Override
    public CommandPurgePolicy getCommandPurgePolicy()
    {
        return null;
    }

    @Override
    public ForegroundNotificationManager getForegroundNotificationManager()
    {
        return null;
    }

    private final class LocalDatabaseFactory implements SQLiteDatabaseFactory
    {
        @Override
        public SQLiteDatabaseIntf getDatabase()
        {
            return new ClearSQLiteDatabase(getWritableDatabase());
        }
    }

    protected abstract SQLiteDatabase getWritableDatabase();
}
