package co.touchlab.android.superbus.provider.json;

import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.provider.sqlite.AbstractSqlitePersistenceProvider;
import co.touchlab.android.superbus.provider.sqlite.SQLiteDatabaseFactory;
import co.touchlab.android.superbus.provider.sqlite.SqliteCommand;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 4/6/13
 * Time: 5:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class JsonSqlitePersistenceProvider extends AbstractSqlitePersistenceProvider
{
    JsonStoredCommandAdapter commandAdapter;

    public JsonSqlitePersistenceProvider(SQLiteDatabaseFactory databaseFactory) throws StorageException
    {
        this(databaseFactory, new BusLogImpl());
    }

    public JsonSqlitePersistenceProvider(SQLiteDatabaseFactory databaseFactory, BusLog log) throws StorageException
    {
        super(databaseFactory, log);
        commandAdapter = new JsonStoredCommandAdapter();
    }

    @Override
    protected SqliteCommand inflateCommand(String commandData, String className) throws StorageException, ClassNotFoundException
    {
        return (SqliteCommand)commandAdapter.inflateCommand(commandData, className);
    }

    @Override
    protected String serializeCommand(SqliteCommand command) throws StorageException
    {
        return commandAdapter.storeCommand(command);
    }
}
