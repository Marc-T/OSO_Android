package com.blackorwhite.oso;


import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;


/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class CheckStateSchedulingService extends IntentService {
    public CheckStateSchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            OSOClient Cli;
            Cli = new OSOClient((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE));

            if (!Cli.CheckConnectivity())
            {
                return;
            }

            String State = Cli.downloadUrl("GetState");

            if (State.equals("2")){
                sendNotification(getString(R.string.alarm_salon_fired), true);
            }
            if (State.equals("3")){
                sendNotification(getString(R.string.alarm_garage_fired), true);
            }
            if (State.equals("5")){
                sendNotification(getString(R.string.alarmsilenced_salon_fired), true);
            }
            if (State.equals("6")){
                sendNotification(getString(R.string.alarmsilenced_garage_fired), true);
            }

        } catch (IOException e) {
            sendNotification(getString(R.string.alarm_unreachable), false);
        }
        finally {
            CheckStateAlarmReceiver.completeWakefulIntent(intent);
        }
    }

    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg, Boolean bAlarm) {
        NotificationManager mNotificationManager;
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);


        Uri alarmSound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.smb_flagpole);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle(getString(R.string.alarm_info))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setContentText(msg);

        if (bAlarm)
        {
            long[] pattern = {500,500,500,500,500,500,500,500,500};
            mBuilder.setLights(Color.RED, 500, 500);
            mBuilder.setVibrate(pattern);
            mBuilder.setSound(alarmSound);
        }

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }
}