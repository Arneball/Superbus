package co.touchlab.android.superbus;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;
import co.touchlab.android.superbus.provider.gson.GsonStoredCommandAdapter;
import co.touchlab.android.superbus.provider.sqlite.ClearSQLiteDatabase;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseIntf;
import co.touchlab.android.superbus.provider.sqlite.SimpleDatabaseHelper;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimplePersistedApplication extends Application implements PersistedApplication
{
    private CommandPersistenceProvider persistenceProvider;

    private final class LocalDatabaseFactory implements SQLiteDatabaseFactory
    {
        @Override
        public SQLiteDatabaseIntf getDatabase()
        {
            return new ClearSQLiteDatabase(SimpleDatabaseHelper.getInstance(SimplePersistedApplication.this).getWritableDatabase());
        }
    }

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
    public SuperbusEventListener getEventListener()
    {
        return null;
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
}
