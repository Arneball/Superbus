package co.touchlab.android.superbus.appsupport;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.PersistedApplication;
import co.touchlab.android.superbus.errorcontrol.PermanentException;
import co.touchlab.android.superbus.errorcontrol.TransientException;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/26/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class CancellableCommand extends Command
{
    private AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public final void callCommand(Context context) throws TransientException, PermanentException
    {
        //If we're cancelled, we're done
        if(cancelled.get())
            return;

        setupData(context);

        PersistedApplication persistedApplication = (PersistedApplication) context.getApplicationContext();
        persistedApplication.getConfig().getPersistenceProvider().runInTransaction(new PostCancelRunnable(context));
    }

    public void setCancelled(boolean cancel)
    {
        cancelled.set(cancel);
    }

    public boolean isCancelled()
    {
        return cancelled.get();
    }

    class PostCancelRunnable implements Runnable
    {
        Context context;

        PostCancelRunnable(Context context)
        {
            this.context = context;
        }

        @Override
        public void run()
        {
            if(cancelled.get())
                return;

            commitData(context);
        }
    }

    protected abstract void setupData(Context context);

    protected abstract void commitData(Context context);
}
