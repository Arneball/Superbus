package co.touchlab.android.superbus;

import android.content.Context;
import co.touchlab.android.superbus.storage.CommandPersistenceProvider;

/**
 * Callback for bus events.
 *
 * Developer note: This has poor dependency on provider package. Should reorg that.
 *
 * User: kgalligan
 * Date: 10/8/12
 * Time: 10:36 PM
 */
public interface SuperbusEventListener
{
    void onBusStarted(Context context, CommandPersistenceProvider provider);
    void onBusFinished(Context context, CommandPersistenceProvider provider, boolean complete);

    //Should probably add something for each command
}
