package co.touchlab.android.superbus.provider.gson;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.provider.file.AbstractFilePersistenceProvider;
import co.touchlab.android.superbus.provider.file.StoredCommand;
import co.touchlab.android.superbus.utils.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Command data stored as json, storing and inflating handled by http://code.google.com/p/google-gson/
 *
 * Be careful about the data you put in here.  ANYTHING in the class will be saved.  If a value should
 * not be saved, make sure to mark it 'transient'.
 *
 * BTW, if you're just getting started and don't know what to use, this is highly recommended.
 *
 * User: kgalligan
 * Date: 10/11/12
 * Time: 3:08 AM
 */
public class GsonFilePersistenceProvider extends AbstractFilePersistenceProvider
{
    private GsonStoredCommandAdapter commandAdapter;

    public GsonFilePersistenceProvider(Context c) throws StorageException
    {
        this(c, new BusLogImpl());
    }

    public GsonFilePersistenceProvider(Context c, BusLog log) throws StorageException
    {
        super(c, log);
        commandAdapter = new GsonStoredCommandAdapter();
    }

    @Override
    protected StoredCommand inflateCommand(File file, String fileName, String className) throws StorageException
    {
        try
        {
            FileReader input = new FileReader(file);
            String jsonData = IOUtils.toString(input);
            input.close();
            return (StoredCommand) commandAdapter.inflateCommand(jsonData, className);
        }
        catch (StorageException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    @Override
    protected void storeCommand(StoredCommand command, File file) throws StorageException
    {
        try
        {
            String jsonData = commandAdapter.storeCommand(command);
            FileWriter output = new FileWriter(file);
            output.write(jsonData);
            output.close();
        }
        catch (StorageException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
    }

    public void removePersistedCommand(Command c) throws StorageException
    {
        throw new UnsupportedOperationException("File store needs fixing");
    }
}