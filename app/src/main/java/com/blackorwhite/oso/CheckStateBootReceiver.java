package com.blackorwhite.oso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// Permet de démarrer le service au démarrage
public class CheckStateBootReceiver  extends BroadcastReceiver {
    CheckStateAlarmReceiver alarm = new CheckStateAlarmReceiver();

    @Override
    public void onReceive(Context context, Intent intent){
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            alarm.setAlarm(context);
        }
    }
}
