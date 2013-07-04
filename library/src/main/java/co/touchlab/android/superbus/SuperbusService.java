package co.touchlab.android.superbus;

import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.provider.PersistedApplication;

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
    private BusLog log;
    public static final String TAG = SuperbusService.class.getSimpleName();
    private ForegroundNotificationManager foregroundNotificationManager;

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

        checkLoadLog(getApplication());
        processor = new SuperbusProcessor();
        try
        {
            processor.init(this);
        }
        catch (ConfigException e)
        {
            throw new RuntimeException(e);
        }
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

    public void checkLoadLog(Application application)
    {
        if (application instanceof PersistedApplication)
        {
            PersistedApplication persistedApplication = (PersistedApplication) application;

            log = persistedApplication.getLog();
            if (log == null)
                log = new BusLogImpl();

            foregroundNotificationManager = persistedApplication.getForegroundNotificationManager();
            if(foregroundNotificationManager == null)
                foregroundNotificationManager = new ForegroundNotificationManager()
                {
                    @Override
                    public boolean isForeground()
                    {
                        return false;
                    }

                    @Override
                    public Notification updateNotification(Context superbusService)
                    {
                        return null;
                    }

                    @Override
                    public int notificationId()
                    {
                        return 24601;
                    }
                };

            if(foregroundNotificationManager.isForeground())
            {
                startForeground(foregroundNotificationManager.notificationId(), foregroundNotificationManager.updateNotification(this));
            }
        }
        else
            Log.e(TAG, "Application does not implement PersistedApplication. Could not load provider.");
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
}
