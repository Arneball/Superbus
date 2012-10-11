package co.touchlab.android.superbus.provider.file;

import android.content.Context;
import android.util.Log;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.provider.AbstractPersistenceProvider;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 9/4/12
 * Time: 2:32 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractFilePersistenceProvider extends AbstractPersistenceProvider
{
    private File filesDir;
    private Set<Class> checkedCommandClasses = new HashSet<Class>();

    protected AbstractFilePersistenceProvider(Context context, BusLog log) throws StorageException
    {
        super(log);
        this.filesDir = context.getFilesDir();
    }

    @Override
    public Collection<? extends Command> loadAll() throws StorageException
    {
        File commandsDirectory = commandsDirectory();

        File[] commandFiles = commandsDirectory.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return !file.getName().startsWith("__");
            }
        });

        List<Command> commands = new ArrayList<Command>(commandFiles.length);

        for (File commandFile : commandFiles)
        {
            try
            {
                Command command = createCommand(commandFile);
                if (command == null)
                    continue;

                commands.add(command);
            }
            catch (Exception e)
            {
                throw new StorageException("Couldn't load command: " + commandFile.getName(), e);
            }
        }

        return commands;
    }

    //TODO: when exception triggered, in-memory list needs a refresh (or a full exception thrown).
    @Override
    public synchronized Command getAndRemoveCurrent() throws StorageException
    {
        Command command = super.getAndRemoveCurrent();
        removeCommand(command);
        return command;
    }

    /**
     * StoredCommand instances are saved to file storage.  Other commands are simply added.
     *
     * @param context
     * @param command
     * @throws StorageException
     */
    @Override
    public void persistCommand(Context context, Command command)throws StorageException
    {
        if(command instanceof StoredCommand)
        {
            //Sanity check. StoredCommand classes need a no-arg constructor
            checkNoArg(command);

            StoredCommand storedCommand = (StoredCommand) command;

            try
            {
                File commands = commandsDirectory();
                String commandClassName = storedCommand.getClass().getName();
                String fullCommandFileName = commandClassName + "." + System.currentTimeMillis();

                File tempCommandFile = new File(commands, "__" + fullCommandFileName);
                File finalCommandFile = new File(commands, fullCommandFileName);

                storedCommand.setCommandFileName(finalCommandFile.getName());

                storeCommand(storedCommand, tempCommandFile);

                boolean success = tempCommandFile.renameTo(finalCommandFile);

                if (!success)
                {
                    throw new StorageException("Couldn't rename command file");
                }
            }
            catch (Exception e)
            {
                throw new StorageException("Couldn't save command file", e);
            }
        }
    }

    private void checkNoArg(Command command) throws StorageException
    {
        if(checkedCommandClasses.contains(command))
            return;

        boolean isNoArg = false;
        Class<? extends Command> commandClass = command.getClass();
        Constructor<?>[] constructors = commandClass.getConstructors();

        for (Constructor<?> constructor : constructors)
        {
            if(constructor.getParameterTypes().length == 0)
            {
                isNoArg = true;
                break;
            }
        }

        if(!isNoArg)
            throw new StorageException("All StoredCommand classes must have a no-arg constructor");

        checkedCommandClasses.add(commandClass);
    }

    private StoredCommand createCommand(File commandFile)
    {
        try
        {
            String commandFileName = commandFile.getName();
            int lastDot = commandFileName.lastIndexOf('.');
            String className = commandFileName.substring(0, lastDot);
            StoredCommand command = inflateCommand(commandFile, commandFileName, className);

            command.setCommandFileName(commandFileName);

            return command;
        }
        catch (Exception e)
        {
            Log.e(AbstractFilePersistenceProvider.class.getSimpleName(), null, e);
            commandFile.delete();

            return null;
        }
    }

    protected abstract StoredCommand inflateCommand(File commandFile, String commandFileName, String className) throws StorageException;

    protected abstract void storeCommand(StoredCommand command, File tempCommandFile)throws StorageException;

    private File commandsDirectory()
    {
        File commands = new File(filesDir, "commands");
        commands.mkdirs();
        return commands;
    }

    private void removeCommand(Command command) throws StorageException
    {
        if (command instanceof StoredCommand)
        {
            boolean success;

            try
            {
                StoredCommand fileCommand = (StoredCommand) command;
                File storedCommand = new File(commandsDirectory(), fileCommand.getCommandFileName());
                success = storedCommand.delete();
            }
            catch (Exception e)
            {
                throw new StorageException("Couldn't remove command file", e);
            }

            if (!success)
            {
                throw new StorageException("Couldn't remove command file");
            }
        }
    }
}
