package co.touchlab.android.superbus;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;

/**
 * The heart of the command bus.  Processes commands.
 * <p/>
 * User: kgalligan
 * Date: 1/11/12
 * Time: 8:57 AM
 */
public class SuperbusService extends Service
{
    private SuperbusProcessor processor;
    private SuperbusConfig config;

    private BusLog log;
    public static final String TAG = SuperbusService.class.getSimpleName();

    public class LocalBinder extends Binder
    {
        public SuperbusService getService()
        {
            return SuperbusService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        init(getApplication());
        processor = new SuperbusProcessor(this, config);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        log.v(TAG, "onStartCommand");
        processor.checkAndStart();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        log.i(TAG, "onDestroy");
    }

    public void init(Application application)
    {
        if (application instanceof PersistedApplication)
        {
            PersistedApplication persistedApplication = (PersistedApplication) application;

            config = persistedApplication.getConfig();
            log = config.log;

            ForegroundNotificationManager foregroundNotificationManager = config.foregroundNotificationManager;

            if (foregroundNotificationManager.isForeground())
            {
                startForeground(foregroundNotificationManager.notificationId(), foregroundNotificationManager.updateNotification(this));
            }
        }
        else
        {
            Log.e(TAG, "Application does not implement PersistedApplication. Could not load provider.");
        }
    }

    /**
     * Call to manually trigger bus processing.
     *
     * @param c Standard Android Context
     */
    public static void notifyStart(Context c)
    {
        c.startService(new Intent(c, SuperbusService.class));
    }

    public static PendingIntent pendingNotifyStart(Context c)
    {
        return PendingIntent.getService(c, 523444, new Intent(c, SuperbusService.class), 0);
    }
}
