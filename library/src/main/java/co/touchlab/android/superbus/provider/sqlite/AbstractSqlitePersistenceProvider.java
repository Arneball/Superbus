package co.touchlab.android.superbus.provider.sqlite;

import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;

/**
 * THIS IS NOT READY.  Coming soon.
 *
 * If you are doing the bulk of your data processing in SQLite, you might want to use this.
 *
 * If done correctly, you can put your command storage in the same transaction as your data storage.
 *
 * User: kgalligan
 * Date: 8/24/12
 * Time: 1:12 AM
 */
public abstract class AbstractSqlitePersistenceProvider extends CommandPersistenceProvider
{


    private SQLiteDatabaseFactory databaseFactory;

    @Override


    @Override
    public void removeCurrent(Command c) throws StorageException
    {
        super.removeCurrent(c);
        removeCommand(c);
    }

    @Override
    public synchronized void removeFromQueue(Command c) throws StorageException
    {
        super.removeFromQueue(c);
        removeCommand(c);
    }

    @Override


    protected abstract Command inflateCommand(String commandData, String className) throws StorageException, ClassNotFoundException;

    protected abstract String serializeCommand(Command command)throws StorageException;


}
