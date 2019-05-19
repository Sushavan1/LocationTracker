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
            showNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(),object.getString("mRequestkey"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void showNotification(String title, String message, String mRequestkey){

        Log.e("mRequestkey", mRequestkey);

        Intent broadcustIntent = new Intent("my.action.string");
        Bundle bundle=new Bundle();
        bundle.putString("toastMessage",ActiveUser.message);
        bundle.putBoolean("isAccept",true);
        bundle.putString("mRequestkey",mRequestkey);
        broadcustIntent.putExtras(bundle);
        PendingIntent acceptIntent = PendingIntent.getBroadcast(this,0,broadcustIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Intent broadcustIntent2 = new Intent(this,NotificationReciver.class);
        broadcustIntent2.putExtra("toastMessage",ActiveUser.message);
        broadcustIntent2.putExtra("isAccept",false);
        broadcustIntent2.putExtra("mRequestkey",mRequestkey);

        PendingIntent rejectIntent = PendingIntent.getBroadcast(this,0,broadcustIntent2,PendingIntent.FLAG_UPDATE_CURRENT);

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