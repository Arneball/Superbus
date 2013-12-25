package co.touchlab.android.superbus;

import android.app.Notification;
import android.content.Context;
import co.touchlab.android.superbus.errorcontrol.CommandPurgePolicy;
import co.touchlab.android.superbus.errorcontrol.ConfigException;
import co.touchlab.android.superbus.errorcontrol.TransientMethuselahCommandPurgePolicy;
import co.touchlab.android.superbus.log.BusLog;
import co.touchlab.android.superbus.log.BusLogImpl;
import co.touchlab.android.superbus.storage.CommandPersistenceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/25/13
 * Time: 12:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class SuperbusConfig
{
    List<SuperbusEventListener> eventListeners = new ArrayList<SuperbusEventListener>();
    ForegroundNotificationManager foregroundNotificationManager;
    BusLog log;
    CommandPurgePolicy commandPurgePolicy;
    CommandPersistenceProvider commandPersistenceProvider;

    public static class Builder
    {
        SuperbusConfig config = new SuperbusConfig();

        private void checkState() throws ConfigException
        {
            if (config == null)
                throw new ConfigException("build already called");
        }

        public Builder addEventListener(SuperbusEventListener eventListener) throws ConfigException
        {
            checkState();
            config.eventListeners.add(eventListener);
            return this;
        }

        public Builder setForegroundNotificationManager(ForegroundNotificationManager f) throws ConfigException
        {
            checkState();
            config.foregroundNotificationManager = f;
            return this;
        }

        public Builder setLog(BusLog l) throws ConfigException
        {
            checkState();
            config.log = l;
            return this;
        }

        public Builder setCommandPurgePolicy(CommandPurgePolicy p) throws ConfigException
        {
            checkState();
            config.commandPurgePolicy = p;
            return this;
        }

        public Builder setCommandPersistenceProvider(CommandPersistenceProvider p) throws ConfigException
        {
            checkState();
            config.commandPersistenceProvider = p;
            return this;
        }

        public SuperbusConfig build() throws ConfigException
        {
            if (config.log == null)
                config.log = new BusLogImpl();
            if (config.commandPurgePolicy == null)
                config.commandPurgePolicy = new TransientMethuselahCommandPurgePolicy();
            if (config.foregroundNotificationManager == null)
                config.foregroundNotificationManager = new ForegroundNotificationManager()
                {
                    @Override
                    public boolean isForeground()
                    {
                        return false;
                    }

                    @Override
                    public Notification updateNotification(Context superbusService)
                    {
                        return null;
                    }

                    @Override
                    public int notificationId()
                    {
                        return -123;
                    }
                };

            if (config.commandPersistenceProvider == null)
                throw new ConfigException("Superbus needs a persistence provider");

            SuperbusConfig retConfig = config;
            config = null;
            return retConfig;
        }
    }

    public List<SuperbusEventListener> getEventListeners()
    {
        return eventListeners;
    }

    public ForegroundNotificationManager getForegroundNotificationManager()
    {
        return foregroundNotificationManager;
    }

    public BusLog getLog()
    {
        return log;
    }

    public CommandPurgePolicy getCommandPurgePolicy()
    {
        return commandPurgePolicy;
    }

    public CommandPersistenceProvider getCommandPersistenceProvider()
    {
        return commandPersistenceProvider;
    }
}
