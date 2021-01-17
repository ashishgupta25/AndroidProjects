package com.example.deepakk.ola;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.example.deepakk.ola.activity.DriverMapActivity;

public class NotificationUtil {

    Context context;

    PendingIntent pendingIntent;
    NotificationCompat.Builder localNotification;

    NotificationCompat.Builder summaryNotification;
    NotificationManagerCompat notifyObj;

    public NotificationUtil(Context context){
        this.context = context;

        Intent intentActivity = new Intent(context, DriverMapActivity.class);

        //intentActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);//Or in manifest set android:launchMode="singleTop"
        //pendingIntent = PendingIntent.getActivity(context, 1, intentActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        pendingIntent = PendingIntent.getActivity(context, 1, intentActivity, 0);


        NotificationChannel channel = new NotificationChannel("my_channel_01", "Customer Request Notification", NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        notifyObj = NotificationManagerCompat.from(context);

    }

    public NotificationManagerCompat getNotificationObj(){
        return notifyObj;
    }

    public void notificationStarter(int notId, String notificationMessage){

        localNotification = new NotificationCompat.Builder(context, "my_channel_01")
                .setContentTitle("Ashish")
                .setContentText(notificationMessage)
                .setContentIntent(pendingIntent)
                .setSmallIcon(android.R.drawable.sym_action_email)
                .setGroup("my_group")
                .setAutoCancel(true); //This makes sure all notifications which have groupname="my_group" are shown in notification under one tree.
        notifyObj.notify(notId, localNotification.build());


        summaryNotification = new NotificationCompat.Builder(context, "my_channel_01")
                .setSmallIcon(android.R.drawable.sym_action_chat)
                .setContentIntent(pendingIntent)
                .setGroup("my_group")   //This makes sure all notifications which have groupname="my_group" are shown in notification under one tree.
                .setGroupSummary(true)
                .setStyle(new NotificationCompat.InboxStyle()
                        .setSummaryText(notId + " Passenger Requests")
                )
                .setAutoCancel(true);
        notifyObj.notify(100, summaryNotification.build());  //Same notId=100 will override summary notification(which is desired) every time with new Summary title(passenger req count)

        }

}
