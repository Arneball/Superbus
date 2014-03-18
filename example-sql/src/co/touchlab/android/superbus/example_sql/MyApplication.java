package co.touchlab.android.superbus.example_sql;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.StrictMode;
import co.touchlab.android.superbus.SuperbusConfig;
import co.touchlab.android.superbus.appsupport.AbstractCommandPersistedApplication;
import co.touchlab.android.superbus.appsupport.CommandBusHelper;
import co.touchlab.android.superbus.appsupport.WakeLockEventListener;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
import co.touchlab.android.superbus.errorcontrol.TransientRetryBusEventListener;

/**
 * User: William Sanville
 * Date: 8/16/12
 * Time: 4:35 PM
 * An implementation of the PersistedApplication interface, to maintain a singleton of the object we're using to persist
 * commands.
 */
public class MyApplication extends AbstractCommandPersistedApplication
{
    public static final int ICS = 15;

    @Override
    public void onCreate()
    {
        super.onCreate();

        DatabaseHelper.writeDbToSdCard(this);

        setupStrictMode();

        new Thread()
        {
            @Override
            public void run()
            {
                CommandBusHelper.submitCommandSync(MyApplication.this, new GetMessageCommand());
            }
        }.start();
    }

    @Override
    protected void buildConfig(SuperbusConfig.Builder configBuilder) throws ConfigException
    {
        //Add event listeners here.
        configBuilder.addEventListener(new WakeLockEventListener());
    }

    private void setupStrictMode()
    {
        if (Build.VERSION.SDK_INT >= ICS)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyFlashScreen()
                    .penaltyLog()
                    .build());
        }
    }

    @Override
    protected SQLiteDatabase getWritableDatabase()
    {
        return DatabaseHelper.getInstance(MyApplication.this).getWritableDatabase();
    }
}
