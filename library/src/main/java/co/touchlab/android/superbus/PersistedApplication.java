package co.touchlab.android.superbus;

import co.touchlab.android.superbus.errorcontrol.CommandPurgePolicy;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
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
//    CommandPersistenceProvider getProvider();
    SuperbusConfig getConfig();
}
