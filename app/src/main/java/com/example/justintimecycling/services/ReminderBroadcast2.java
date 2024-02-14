package com.example.justintimecycling.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.justintimecycling.R;

import java.util.Random;

public class ReminderBroadcast2 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("MUIE", "muie2");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyUser");
        builder.setAutoCancel(true)
                .setSmallIcon(R.drawable.two_min)
                .setContentTitle("Train departure")
                .setContentText("Your first train will depart in 2 minutes")
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                .setContentInfo("Info")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(1, builder.build());
    }
}