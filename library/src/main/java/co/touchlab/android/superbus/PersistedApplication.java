package co.touchlab.android.superbus;

import co.touchlab.android.superbus.errorcontrol.CommandPurgePolicy;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.storage.CommandPersistenceProvider;

import java.util.Collection;

/**
 * To use the bus, you MUST provide an implementation of this in your Application class.
 *
 * User: William Sanville
 * Date: 8/16/12
 * Time: 2:27 PM
 *
 */
public interface PersistedApplication
{
    /**
     * @return The PersistenceProvider of your choice.  May I recommend GsonPersistenceProvider?
     */
    CommandPersistenceProvider getProvider();

    /**
     * @return Log implementation.  If left null, the LogCat default will be used.
     */
    BusLog getLog();

    /**
     * @return Bus lifecycle event listener.  Can, and will usually be, null.  To enable/disable network restart processing, use ConnectionChangeBusEventListener.
     */
    Collection<SuperbusEventListener> getEventListeners();

    /**
     * @return Return a custom purge policy. Can be null.
     */
    CommandPurgePolicy getCommandPurgePolicy();

    /**
     * Manage foreground service updates.  Can be null.
     * @return
     */
    ForegroundNotificationManager getForegroundNotificationManager();
}
