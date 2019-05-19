package com.example.loknath.locationtracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class MyMessingService extends FirebaseMessagingService {
    public static final String TAG=MyMessingService.class.getName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        try
        {
            Map<String, String> params = remoteMessage.getData();
            JSONObject object = new JSONObject(params);
            Log.e("JSON OBJECT", object.getString("mRequestkey"));
            if(object.getBoolean("isSender"))
            {
                showNotificationforSender(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), object.getString("mRequestkey"));
            }else {
                showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), object.getString("mRequestkey"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showNotificationforSender(String title, String message, String mRequestkey){

        Log.e("mRequestkey", mRequestkey);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyNotification")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(message)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setColor(Color.BLUE)
                //.setContentIntent(contentIntent)
                .setOnlyAlertOnce(true)
                .setStyle( new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(111,builder.build());
    }


    public void showNotification(String title, String message, String mRequestkey){

        Log.e("mRequestkey", mRequestkey);

        Intent acceptbroadcustIntent = new Intent(this,NotificationReciver.class);
        acceptbroadcustIntent.putExtra("toastMessage",ActiveUser.message);
        acceptbroadcustIntent.putExtra("isAccepted","Accept");
        acceptbroadcustIntent.putExtra("mRequestkey",mRequestkey);

       PendingIntent acceptIntent = PendingIntent.getBroadcast(this,0,acceptbroadcustIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent rejectbroadcustIntent = new Intent(this,NotificationReciver.class);
        rejectbroadcustIntent.putExtra("toastMessage",ActiveUser.message);
        rejectbroadcustIntent.putExtra("isAccepted","Denied");
        rejectbroadcustIntent.putExtra("mRequestkey",mRequestkey);

        PendingIntent rejectIntent = PendingIntent.getBroadcast(this,1,rejectbroadcustIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "MyNotification")
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentText(message)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setColor(Color.BLUE)
                //.setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setStyle( new NotificationCompat.BigTextStyle().bigText(message))
                .setDefaults(Notification.DEFAULT_ALL)
                .addAction(R.mipmap.ic_launcher,"Accept",acceptIntent)
                .addAction(R.mipmap.ic_launcher,"Reject",rejectIntent);  //.addAction(R.drawable.,"Accept",Activity); for adding buttons

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(111,builder.build());
    }
}