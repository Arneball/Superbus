package co.touchlab.android.superbus.example_sql;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.StrictMode;
import co.touchlab.android.superbus.AbstractPersistedApplication;
import co.touchlab.android.superbus.BusHelper;

/**
 * User: William Sanville
 * Date: 8/16/12
 * Time: 4:35 PM
 * An implementation of the PersistedApplication interface, to maintain a singleton of the object we're using to persist
 * commands.
 */
public class MyApplication extends AbstractPersistedApplication
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
                BusHelper.submitCommandSync(MyApplication.this, new GetMessageCommand());
            }
        }.start();
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
