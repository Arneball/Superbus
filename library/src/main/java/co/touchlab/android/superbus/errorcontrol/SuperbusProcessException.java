package co.touchlab.android.superbus.errorcontrol;

/**
 * Basically a RuntimeException.  This happens if something with the actual bus process fails.  Also, users
 * can throw from callback methods to simply app exit (ie fail fast).
 *
 * Created by kgalligan on 6/29/14.
 */
public class SuperbusProcessException extends RuntimeException
{
    public SuperbusProcessException()
    {
    }

    public SuperbusProcessException(String detailMessage)
    {
        super(detailMessage);
    }

    public SuperbusProcessException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

    public SuperbusProcessException(Throwable throwable)
    {
        super(throwable);
    }
}
