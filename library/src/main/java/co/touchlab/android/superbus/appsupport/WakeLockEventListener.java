package co.touchlab.android.superbus.appsupport;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import co.touchlab.android.superbus.SuperbusEventListener;
import co.touchlab.android.superbus.storage.PersistenceProvider;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 5/6/13
 * Time: 10:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class WakeLockEventListener implements SuperbusEventListener
{

    private PowerManager.WakeLock wl;

    @Override
    public void onBusStarted(Context context, PersistenceProvider provider)
    {
        Log.d(WakeLockEventListener.class.getSimpleName(), "WakeLong-onBusStarted");
        PowerManager pm = (PowerManager)context.getSystemService(
                                                  Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WakeLockEventListener.class.getSimpleName());
        wl.acquire();
    }

    @Override
    public void onBusFinished(Context context, PersistenceProvider provider, boolean complete)
    {
        Log.d(WakeLockEventListener.class.getSimpleName(), "WakeLong-onBusFinished");
        wl.release();
    }
}
