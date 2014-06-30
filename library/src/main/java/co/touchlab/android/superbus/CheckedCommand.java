package co.touchlab.android.superbus;

import android.content.Context;
import co.touchlab.android.superbus.errorcontrol.PermanentException;
import co.touchlab.android.superbus.errorcontrol.SuperbusProcessException;

/**
 * Created by kgalligan on 6/29/14.
 */
public abstract class CheckedCommand extends Command
{
    public abstract boolean handlePermanentError(Context context, PermanentException exception);

    @Override
    public final void onPermanentError(Context context, PermanentException exception)
    {
        boolean handled = handlePermanentError(context, exception);
        if(!handled)
            throw new SuperbusProcessException(exception);
    }
}
