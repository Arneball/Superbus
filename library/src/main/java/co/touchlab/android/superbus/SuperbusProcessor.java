package co.touchlab.android.superbus;

import android.app.Application;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import co.touchlab.android.superbus.errorcontrol.PermanentException;
import co.touchlab.android.superbus.errorcontrol.StorageException;
import co.touchlab.android.superbus.errorcontrol.TransientException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.storage.PersistenceProvider;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 4/3/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SuperbusProcessor
{
    public static final String TAG = SuperbusProcessor.class.getSimpleName();
    private CommandThread thread;
    private SuperbusConfig config;
    private PersistenceProvider provider;
    private BusLog log;
    private Handler mainThreadHandler;
    private Service parentService;
    private Application appContext;

    public SuperbusProcessor(Service parentService, SuperbusConfig config)
    {
        this.appContext = parentService.getApplication();
        this.parentService = parentService;
        this.config = config;
        log = config.log;
        provider = config.persistenceProvider;
        mainThreadHandler = new Handler();
    }

    private void logCommandDebug(Command command, String methodName)
    {
        try
        {
            log.d(TAG, methodName + ": " + command.getAdded() + " : " + command.logSummary());
        }
        catch (Exception e)
        {
            //Just in case...
        }
    }

    private void logCommandVerbose(Command command, String methodName)
    {
        try
        {
            log.v(TAG, methodName + ": " + command.getAdded() + " : " + command.logSummary());
        }
        catch (Exception e)
        {
            //Just in case...
        }
    }

    protected synchronized void checkAndStart()
    {
        if (thread == null)
        {
            thread = new CommandThread();
            thread.start();
        }
    }

    private class CommandThread extends Thread
    {
        @Override
        public void run()
        {
            log.i(TAG, "CommandThread loop started");

            Command c;

            try
            {
                for (SuperbusEventListener eventListener : config.eventListeners)
                {
                    eventListener.onBusStarted(appContext, provider);
                }

                provider.logPersistenceState();

                while ((c = provider.readTop()) != null)
                {
                    c.setCommandRunning(true);
                    logCommandDebug(c, "[CommandThread]");

                    if(config.foregroundNotificationManager.isForeground())
                    {
                         final NotificationManager notificationManager = (NotificationManager) parentService.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                         notificationManager.notify(config.foregroundNotificationManager.notificationId(), config.foregroundNotificationManager.updateNotification(parentService));
                    }

                    try
                    {
                        callCommand(c);

                        //TODO: need to deal with failure of save
                        provider.removeCommand(c);

                        //TODO: need to deal with failure of callback
                        c.onSuccess(appContext);
                    }
                    catch (TransientException e)
                    {
                        try
                        {
                            log.e(TAG, null, e);
                            c.setTransientExceptionCount(c.getTransientExceptionCount() + 1);

                            boolean purge = config.commandPurgePolicy.purgeCommandOnTransientException(c, e);

                            if (purge)
                            {
                                log.w(TAG, "Purging command on TransientException: {" + c.logSummary() + "}");
                                provider.removeCommand(c);
                                c.onPermanentError(appContext, new PermanentException(e));
                            }
                            else
                            {
                                c.setCommandRunning(false);
                                provider.repostCommand(c);
                                c.onTransientError(appContext, e);
                            }

                            break;
                        }
                        catch (StorageException e1)
                        {
                            provider.removeCommand(c);
                            logPermanentException(c, e1);
                        }
                    }
                    catch (Throwable e)
                    {
                        provider.removeCommand(c);
                        logPermanentException(c, e);
                    }

                    log.d(TAG, "Command [" + c.getClass().getSimpleName() + "] ended: " + System.currentTimeMillis());
                }
            }
            catch (Throwable e)
            {
                log.e(TAG, "Thread ended with exception", e);
            }

            mainThreadHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    finishThread();
                    stopService();
                }
            });
        }

        private void logPermanentException(Command c, Throwable e)
        {
            log.e(TAG, null, e);
            PermanentException pe = e instanceof PermanentException ? (PermanentException) e : new PermanentException(e);
            c.onPermanentError(appContext, pe);
        }
    }

    /**
     * Finally shut down.  This should ONLY be in the main UI thread.  Presumably, if we call stopSelf here,
     * and another call comes in right after, the service will be restarted.  If that assumption is incorrect,
     * there's the remote possibility that a command will not be processed right away, but it SHOULD still
     * stick around, so at worst the processing will be delayed.
     */
    private void stopService()
    {
        allDone(appContext);
        parentService.stopSelf();
    }

    private synchronized void finishThread()
    {
        thread = null;
    }

    void allDone(Context context)
    {
        try
        {
            for (SuperbusEventListener eventListener : config.eventListeners)
            {
                eventListener.onBusFinished(context, provider, provider.getSize() == 0);
            }
        }
        catch (StorageException e)
        {
            log.e(TAG, null, e);
        }
    }

    private void callCommand(final Command command) throws Exception
    {
        logCommandVerbose(command, "callCommand-start");

        command.callCommand(appContext);

        logCommandVerbose(command, "callComand-finish");
    }
}
