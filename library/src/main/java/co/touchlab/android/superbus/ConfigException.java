package co.touchlab.android.superbus;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 7/4/13
 * Time: 2:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigException extends Exception
{
    public ConfigException()
    {
        super();
    }

    public ConfigException(String detailMessage)
    {
        super(detailMessage);
    }

    public ConfigException(String detailMessage, Throwable throwable)
    {
        super(detailMessage, throwable);
    }

    public ConfigException(Throwable throwable)
    {
        super(throwable);
    }
}
