package co.touchlab.android.superbus.storage;

import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.errorcontrol.StorageException;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/25/13
 * Time: 1:57 AM
 * To change this template use File | Settings | File Templates.
 */
public interface PersistenceProvider
{
    void logPersistenceState();
    Command readTop() throws StorageException;
    void removeCommand(Command command) throws StorageException;
    void repostCommand(Command command) throws StorageException;
    int getSize() throws StorageException;
    void queryAll(CommandQuery query);
    void runInTransaction(Runnable r);
}
