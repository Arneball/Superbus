package co.touchlab.android.superbus.storage;

import co.touchlab.android.superbus.Command;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 4/6/13
 * Time: 2:42 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CommandQuery
{
    void runQuery(Command c);
}
