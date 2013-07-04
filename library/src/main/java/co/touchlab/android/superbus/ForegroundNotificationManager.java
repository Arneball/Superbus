package co.touchlab.android.superbus;

import android.app.Notification;
import android.content.Context;

/**
 * Created with IntelliJ IDEA.
 * User: kgalligan
 * Date: 6/8/13
 * Time: 8:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ForegroundNotificationManager
{
    boolean isForeground();

    Notification updateNotification(Context superbusService);

    int notificationId();
}
