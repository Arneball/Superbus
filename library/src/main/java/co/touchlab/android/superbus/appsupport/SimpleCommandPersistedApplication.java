package co.touchlab.android.superbus.appsupport;

import android.database.sqlite.SQLiteDatabase;
import co.touchlab.android.superbus.SuperbusConfig;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
import co.touchlab.android.superbus.errorcontrol.TransientRetryBusEventListener;
import co.touchlab.android.superbus.storage.sqlite.SimpleDatabaseHelper;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 5:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleCommandPersistedApplication extends AbstractCommandPersistedApplication
{
    @Override
    protected void buildConfig(SuperbusConfig.Builder configBuilder) throws ConfigException
    {
        configBuilder.addEventListener(new TransientRetryBusEventListener());
    }

    @Override
    protected SQLiteDatabase getWritableDatabase()
    {
        return SimpleDatabaseHelper.getInstance(SimpleCommandPersistedApplication.this).getWritableDatabase();
    }
}
