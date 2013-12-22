package co.touchlab.android.superbus;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 7/4/13
 * Time: 10:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class BusHelper
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
            application.getProvider().put((Context)application, command);
        }
        catch (StorageException e)
        {
            throw new RuntimeException(e);
        }
    }

}
