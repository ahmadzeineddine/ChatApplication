package com.ahmed.chatapplication;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bilkoon.rooster.util.SessionManager;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Ahmed on 5/24/2017.
 */

public class GlobalUtil {
    private static final String TAG = "GlobalUtil";

    private static SessionManager sessionManager;

    public static NotificationManager mNotificationManager;
    public static Intent resultIntent;
    public static int notifyID;
    public static AtomicInteger at = new AtomicInteger(9001);

    public static void createMessageNotification(Context mApplicationContext, String title, String jid, String message){
        sessionManager = new SessionManager(mApplicationContext);
        resultIntent = new Intent(mApplicationContext, ChatActivity.class);

        Log.d(TAG, "Notification Status : ");
        resultIntent.putExtra("jid", jid);
        VCard vCard = sessionManager.loadVCardInfo(jid);
        resultIntent.putExtra("name", vCard.getFirstName());
        resultIntent.putExtra("ppic", vCard.getField("avatar"));

        Uri alarmSound = Uri.parse("android.resource://" + mApplicationContext.getPackageName() + "/" + R.raw.incoming);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(mApplicationContext, 0,
                resultIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotifyBuilder;
        mNotificationManager = (NotificationManager) mApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifyBuilder = new NotificationCompat.Builder(mApplicationContext)
                .setContentTitle(title)
                .setContentText(message)
                .setSound(alarmSound)
                .setSmallIcon(R.drawable.ic_stat_ic_notification);
        // Set pending intent
        mNotifyBuilder.setContentIntent(resultPendingIntent);
        // Set Vibrate, Sound and Light
        int defaults = 0;
        defaults = defaults | Notification.DEFAULT_LIGHTS;
        defaults = defaults | Notification.DEFAULT_VIBRATE;
        defaults = defaults | Notification.DEFAULT_SOUND;

        mNotifyBuilder.setDefaults(defaults);

        //if(isImage==1)    msg = title +" sent you an image";
        //mNotifyBuilder.setContentText(msg);
        // Set autocancel
        mNotifyBuilder.setAutoCancel(true);
        // Post a notification
        notifyID = at.getAndAdd(1);
        mNotificationManager.notify(notifyID, mNotifyBuilder.build());

        int count = sessionManager.loadNotifyCount(title);
        Log.d(TAG, "Notification count "+count);
        sessionManager.saveNotifyId(title, count, notifyID);
        sessionManager.saveNotifyCount(title, count+1);
    }
}
