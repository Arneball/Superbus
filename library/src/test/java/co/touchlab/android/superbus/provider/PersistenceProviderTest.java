package co.touchlab.android.superbus.provider;

import android.content.Context;
import co.touchlab.android.superbus.Command;
import co.touchlab.android.superbus.PermanentException;
import co.touchlab.android.superbus.StorageException;
import co.touchlab.android.superbus.TransientException;
import co.touchlab.android.superbus.provider.memory.MemoryPersistenceProvider;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO: Need to set up testing package and put this in it.
 *
 * User: kgalligan
 * Date: 10/15/12
 * Time: 12:12 AM
 */
public class PersistenceProviderTest
{
    @Test
    public void testCountQuery() throws StorageException, InterruptedException
    {
        MemoryPersistenceProvider provider = new MemoryPersistenceProvider(new NullBusLog());

        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));
        provider.put(null, new FirstTypeCommand(1));
        provider.put(null, new FirstTypeCommand(3));
        provider.put(null, new FirstTypeCommand(5));
        provider.put(null, new FirstTypeCommand(7));

        CountQuery query = new CountQuery();
        provider.queryAll(query);

        Assert.assertEquals(query.count, 7);
    }

    public static class CountQuery implements CommandQuery
    {
        int count = 0;

        @Override
        public void runQuery(Command c)
        {
            if(((FirstTypeCommand)c).aValue == 3)
                count++;
        }
    }

    public static class FirstTypeCommand extends Command
    {
        int aValue;

        public FirstTypeCommand()
        {
        }

        public FirstTypeCommand(int aValue)
        {
            this.aValue = aValue;
        }

        @Override
        public String logSummary()
        {
            return "The val: "+ aValue;
        }

        @Override
        public boolean same(Command command)
        {
            return false;
        }

        @Override
        public void callCommand(Context context) throws TransientException, PermanentException
        {
            System.out.println("Doing things: "+ aValue);
        }
    }

    @Test
    public void testPriorityOrder() throws StorageException, InterruptedException
    {
        MemoryPersistenceProvider provider = new MemoryPersistenceProvider(new NullBusLog());

        provider.put(null, new DefaultPriorityCommand(Command.DEFAULT_PRIORITY));
        Thread.sleep(100);
        DefaultPriorityCommand lowest = new DefaultPriorityCommand(Command.LOWER_PRIORITY);
        provider.put(null, lowest);
        Thread.sleep(100);
        provider.put(null, new DefaultPriorityCommand(Command.DEFAULT_PRIORITY));
        Thread.sleep(100);
        provider.put(null, new DefaultPriorityCommand(Command.HIGHER_PRIORITY));
        Thread.sleep(100);
        provider.put(null, new DefaultPriorityCommand(Command.DEFAULT_PRIORITY));
        Thread.sleep(100);
        provider.put(null, new DefaultPriorityCommand(Command.MUCH_HIGHER_PRIORITY));
        Thread.sleep(100);
        provider.put(null, new DefaultPriorityCommand(Command.DEFAULT_PRIORITY));

        int lastPriority = Integer.MAX_VALUE;
        long lastTime = 0l;

        Command command;

        while((command = provider.stageCurrent()) != null)
        {
            int priority = command.getPriority();

            Assert.assertTrue("Priority in wrong order", priority <= lastPriority);

            Assert.assertTrue("Timestamps out of order", priority != lastPriority || lastTime <= command.getAdded());

//            System.out.println("lastPriority: "+ lastPriority +"/priority: "+ priority +"/lastTime: "+ lastTime +"/added: "+ command.getAdded());

            lastPriority = priority;
            lastTime = command.getAdded();

            provider.removeCurrent(command);
        }

    }

    public static class DefaultPriorityCommand extends Command
    {
        public DefaultPriorityCommand(int priority)
        {
            setPriority(priority);
        }

        @Override
        public String logSummary()
        {
            return "priority: "+ getPriority() +"/added: "+ getAdded();
        }

        @Override
        public boolean same(Command command)
        {
            return false;
        }

        @Override
        public void callCommand(Context context) throws TransientException, PermanentException
        {

        }
    }
}
