package co.touchlab.android.superbus.errorcontrol;

import co.touchlab.android.superbus.Command;

/**
 * Commands that trigger TransientException will live forever.
 *
 * This is the default, and in general models desired
 * functionality.  However, this is dangerous.  If something triggers TransientException, but never resolves itself,
 * the queue will be forever blocked.
 *
 * However, in real world scenarios, a device may have a spotty connection, or be offline for several days, or both.
 * Killing commands based on network issues alone could cause serious issues with app functionality.
 *
 * User: kgalligan
 * Date: 10/13/12
 * Time: 3:32 AM
 */
public class TransientMethuselahCommandPurgePolicy implements CommandPurgePolicy
{
    @Override
    public boolean purgeCommandOnTransientException(Command command, TransientException exception)
    {
        return false;
    }
}
