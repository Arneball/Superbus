package co.touchlab.android.superbus.appsupport;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.PersistedApplication;
import co.touchlab.android.superbus.SuperbusConfig;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
import co.touchlab.android.superbus.errorcontrol.TransientRetryBusEventListener;
import co.touchlab.android.superbus.storage.CommandPersistenceProvider;
import co.touchlab.android.superbus.storage.gson.GsonStoredCommandAdapter;
import co.touchlab.android.superbus.storage.sqlite.ClearSQLiteDatabase;
import co.touchlab.android.superbus.storage.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.storage.sqlite.SQLiteDatabaseIntf;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 6:16 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractCommandPersistedApplication extends Application implements PersistedApplication
{
    private SuperbusConfig config;

    @Override
    public void onCreate()
    {
        super.onCreate();
        try
        {
            config = new SuperbusConfig.Builder()
                            .setPersistenceProvider(new CommandPersistenceProvider(new LocalDatabaseFactory(), new GsonStoredCommandAdapter(), null))
                            .addEventListener(new TransientRetryBusEventListener())
                            .build();
        }
        catch (ConfigException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SuperbusConfig getConfig()
    {
        return config;
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
