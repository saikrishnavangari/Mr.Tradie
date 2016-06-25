package au.com.appscore.mrtradie;

/**
 * Created by adityathakar on 11/09/15.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Service used for receiving GCM messages. When a message is received this service will log it.
 */
public class GcmService extends GcmListenerService {

    //private LoggingService.Logger logger;

    public GcmService() {
      //  logger = new LoggingService.Logger(this);
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d("Debug","Notification: "+data.toString());
        Log.d("Debug","Notification: "+data.getString("message"));

        sendNotification(data.getString("message"));
    }

    @Override
    public void onDeletedMessages() {
        sendNotification("Deleted messages on server");
    }

    @Override
    public void onMessageSent(String msgId) {
        sendNotification("Upstream message sent. Id=" + msgId);
    }

    @Override
    public void onSendError(String msgId, String error) {
        sendNotification("Upstream message send error. Id=" + msgId + ", error" + error);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        int icon = R.drawable.app_icon;
//        CharSequence notiText = msg;
//        long meow = System.currentTimeMillis();
//
//        Notification notification = new Notification(icon, notiText, meow);
//
//        Context context = getApplicationContext();
//        CharSequence contentTitle = "Mr. Tradie";
//        CharSequence contentText = msg;
//        Intent notificationIntent = new Intent(this, MainScreen.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
//
//        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
//
//        int SERVER_DATA_RECEIVED = 1;
//        notificationManager.notify(SERVER_DATA_RECEIVED, notification);
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle("Mr. Tradie")
                        .setContentText(msg)
                        .setAutoCancel(true)
                        .setTicker(msg)
                        .setSound(notificationSound);


        Intent resultIntent = new Intent(this, MainScreen.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}