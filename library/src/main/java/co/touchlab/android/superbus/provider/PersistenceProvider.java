package co.touchlab.android.superbus.provider;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.StorageException;

import java.util.Collection;
import java.util.Map;

/**
 * Provides persistence for commands, and the interface that the bus works with to get
 * commands.  In almost all cases, you should not implement this yourself.  Complex dynamics.
 *
 * Use AbstractPersistenceProvider or some derivative instead.
 *
 * User: William Sanville
 * Date: 8/16/12
 * Time: 1:58 PM
 */
public interface PersistenceProvider
{
    void put(Context context, Command c) throws StorageException;

    void persistCommand(Context context, Command c)throws StorageException;

    /**
     * Pull the top, keep it around just in case, and return to bus.
     *
     * @return
     * @throws StorageException
     */
    Command stageCurrent() throws StorageException;

    /**
     * Top failed with (most likely) transient.  Push top back.
     *
     * @param context
     * @param c
     * @throws StorageException
     */
    void unstageCurrent(Context context, Command c) throws StorageException;

    /**
     * Dump top.  With persisted implementations, this will actually remove from persistence.
     *
     * @param c
     * @throws StorageException
     */
    void removeCurrent(Command c)throws StorageException;

    void removeFromQueue(Command c)throws StorageException;

    int getSize() throws StorageException;

    void logPersistenceState();

    /**
     * Send a message to all queued commands.  This will NOT go to the top command.
     *
     * @param context You know what this is. If not, you shouldn't be using this.
     * @param message User-defined string message
     */
    void sendMessage(Context context, String message);

    /**
     * Send a message to all queued commands.  This will NOT go to the top command.
     *
     * @param context You know what this is. If not, you shouldn't be using this.
     * @param message User-defined string message
     * @param args User-defined map of arguments
     */
    void sendMessage(Context context, String message, Map args);

    /**
     * Run a query against the queue.  Basically, figure out what's happening.
     *
     * This WILL look at the top command.  "sendMessage" will not.
     *
     * @param query
     */
    void queryAll(CommandQuery query);
}
