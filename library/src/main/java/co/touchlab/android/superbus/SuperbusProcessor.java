package co.touchlab.android.superbus;

import android.app.Application;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.os.Handler;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;

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
    private CommandPersistenceProvider provider;
    private SuperbusEventListener eventListener;
    private CommandPurgePolicy commandPurgePolicy;
    private BusLog log;
    private Handler mainThreadHandler;
    private Service parentService;
    private Application appContext;
    private ForegroundNotificationManager foregroundNotificationManager;

    void init(Service parentService) throws ConfigException
    {
        this.appContext = parentService.getApplication();
        this.parentService = parentService;
        provider = checkLoadProvider(appContext);
        eventListener = checkLoadEventListener(appContext);
        commandPurgePolicy = checkLoadCommandPurgePolicy(appContext);
        log.v(TAG, "onCreate " + System.currentTimeMillis());

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

            boolean forceShutdown;
            try
            {
                forceShutdown = false;

                if (eventListener != null)
                    eventListener.onBusStarted(appContext, provider);

                provider.logPersistenceState();

                while ((c = provider.readTop()) != null)
                {
                    logCommandDebug(c, "[CommandThread]");

                    if(foregroundNotificationManager != null)
                    {
                         final NotificationManager notificationManager = (NotificationManager) parentService.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                         notificationManager.notify(foregroundNotificationManager.notificationId(), foregroundNotificationManager.updateNotification(parentService));
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

                            boolean purge = commandPurgePolicy.purgeCommandOnTransientException(c, e);

                            if (purge)
                            {
                                log.w(TAG, "Purging command on TransientException: {" + c.logSummary() + "}");
                                provider.removeCommand(c);
                                c.onPermanentError(appContext, new PermanentException(e));
                            }
                            else
                            {
                                provider.updateCommand(c);
                                c.onTransientError(appContext, e);
                            }

                            forceShutdown = true;
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
                forceShutdown = true;
            }

            if (forceShutdown)
            {
                log.i(TAG, "CommandThread loop done (forced)");
                finishThread();
                mainThreadHandler.post(new Runnable()
                {
                    public void run()
                    {
                        stopService();
                    }
                });
            }
            else
            {
                //Running wrap up in ui thread.  The concern here is that between the time that the while loop ends,
                //and the kill logic runs, another command comes in.  The "start" logic would've rejected starting a new
                //thread.  However, the loop would end, and the command would stay out in the queue.  Data would stay
                //in play, but wouldn't automatically start processing.  Rare, but frustrating bug.
                //The assumption here is that either onStartCommand, and this block, would be called in exclusion, so
                //either the service would be stopped and restarted, or we'd see the new command and restart.
                //TODO: Should confirm this assumption.
                mainThreadHandler.post(new Runnable()
                {
                    public void run()
                    {
                        log.i(TAG, "CommandThread loop done (natural)");
                        finishThread();

                        //This is complex.  In the EXTREMELY unlikely case that the call to getSize fails,
                        //just return 0 and exit.  I honestly have no idea what else we should do here.
                        //Probably better off to throw up hands and crash app, but willing to take votes on the matter.
                        int size = 0;
                        try
                        {
                            size = provider.getSize();
                        }
                        catch (StorageException e)
                        {
                            log.e(TAG, null, e);
                        }

                        //Extremely unlikely, but still.
                        if (size > 0)
                        {
                            checkAndStart();
                        }
                        else
                        {
                            stopService();
                        }
                    }
                });
            }

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
            if (eventListener != null)
                eventListener.onBusFinished(context, provider, provider.getSize() == 0);
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

    /**
     * We expect the application that uses this library to have a custom subclass of Application which implements
     * PersistedApplication. This convention is to agree upon a way to specify how the service stores/loads its commands.
     *
     * @param application The Application object.
     * @return Some implementation of PersistenceProvider.
     */
    public CommandPersistenceProvider checkLoadProvider(Application application) throws ConfigException
    {
        CommandPersistenceProvider result;

        if (application instanceof PersistedApplication)
        {
            PersistedApplication persistedApplication = (PersistedApplication) application;

            log = persistedApplication.getLog();

            foregroundNotificationManager = persistedApplication.getForegroundNotificationManager();

            if(foregroundNotificationManager != null && foregroundNotificationManager.notificationId() <= 0)
                throw new ConfigException("Foreground notification id should be greater than 0");

            result = persistedApplication.getProvider();
        }
        else
            throw new RuntimeException("No PersistenceProvider was found");

        if (log == null)
            log = new BusLogImpl();

        return result;
    }

    /**
     * We expect the application that uses this library to have a custom subclass of Application which implements
     * PersistedApplication. This convention is to agree upon a way to specify how the service stores/loads its commands.
     *
     * @param application The Application object.
     * @return Some implementation of PersistenceProvider.
     */
    public SuperbusEventListener checkLoadEventListener(Application application)
    {
        if (application instanceof PersistedApplication)
        {
            PersistedApplication persistedApplication = (PersistedApplication) application;

            return persistedApplication.getEventListener();
        }

        return null;
    }

    public CommandPurgePolicy checkLoadCommandPurgePolicy(Application application)
    {
        CommandPurgePolicy purgePolicy = null;

        if (application instanceof PersistedApplication)
        {
            PersistedApplication persistedApplication = (PersistedApplication) application;
            purgePolicy = persistedApplication.getCommandPurgePolicy();
        }

        if (purgePolicy == null)
            purgePolicy = new TransientMethuselahCommandPurgePolicy();

        return purgePolicy;
    }
}
