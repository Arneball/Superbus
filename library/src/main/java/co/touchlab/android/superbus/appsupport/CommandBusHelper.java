package co.touchlab.android.superbus.appsupport;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.PersistedApplication;
import co.touchlab.android.superbus.errorcontrol.StorageException;
import co.touchlab.android.superbus.storage.CommandPersistenceProvider;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 7/4/13
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommandBusHelper
{
    static ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static void submitCommandAsync(final Context context, final Command command)
    {
        executorService.execute(new Runnable()
        {
            @Override
            public void run()
            {
                submitCommandSync(context, command);
            }
        });
    }

    public static void submitCommandSync(Context context, Command command)
    {
        final PersistedApplication application = (PersistedApplication) context.getApplicationContext();
        try
        {
            ((CommandPersistenceProvider)application.getConfig().getPersistenceProvider()).put((Context)application, command);
        }
        catch (StorageException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static void sendMessage(Context context, String message)
    {
        sendMessage(context, message, null);
    }

    public static void sendMessage(Context context, String message, Map args)
    {
        final PersistedApplication application = (PersistedApplication) context.getApplicationContext();

        if(args == null)
            ((CommandPersistenceProvider)application.getConfig().getPersistenceProvider()).sendMessage(context, message);
        else
            ((CommandPersistenceProvider)application.getConfig().getPersistenceProvider()).sendMessage(context, message, args);
    }

}
