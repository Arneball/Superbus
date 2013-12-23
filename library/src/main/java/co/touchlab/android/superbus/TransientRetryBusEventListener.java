package co.touchlab.android.superbus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import co.touchlab.android.superbus.provider.CommandPersistenceProvider;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 12/22/13
 * Time: 6:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class TransientRetryBusEventListener implements SuperbusEventListener
{
    public static final int THIRTY_MINUTES = 30 * 60000;
    public static final int FIVE_MINUTES = 5 * 60000;
    public static final int THIRTY_SECONDS = 30000;
    public static final int FIVE_SECONDS = 5000;
    int incompleteRetires = 0;

    @Override
    public void onBusStarted(Context context, CommandPersistenceProvider provider)
    {
        cancelAlarm(context);
    }

    private void cancelAlarm(Context context)
    {
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.cancel(fireUpService(context));
    }

    @Override
    public void onBusFinished(Context context, CommandPersistenceProvider provider, boolean complete)
    {
        if(!complete)
        {
            AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long wakeUpTime;

            if(incompleteRetires == 0)
                wakeUpTime = System.currentTimeMillis() + FIVE_SECONDS;
            else if(incompleteRetires < 2)
                wakeUpTime = System.currentTimeMillis() + THIRTY_SECONDS;
            else if(incompleteRetires < 5)
                wakeUpTime = System.currentTimeMillis() + FIVE_MINUTES;
            else
                wakeUpTime = System.currentTimeMillis() + THIRTY_MINUTES;

            mgr.set(AlarmManager.RTC, wakeUpTime, fireUpService(context));
            incompleteRetires++;
        }
        else
        {
            cancelAlarm(context);
            incompleteRetires = 0;
        }
    }

    private PendingIntent fireUpService(Context context)
    {
        return SuperbusService.pendingNotifyStart(context);
    }

}
