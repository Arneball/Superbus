package co.touchlab.android.superbus;


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
    SuperbusConfig getConfig();
}
