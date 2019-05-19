package com.example.loknath.locationtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationReciver extends BroadcastReceiver {
    private OkHttpClient mClient = new OkHttpClient();

    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getExtras().getString("tostMessage");
        boolean isAccept = intent.getExtras().getBoolean("isAccept");
        String mRequestkey = intent.getExtras().getString("mRequestkey");
       // Toast.makeText(context,ActiveUser.message,Toast.LENGTH_LONG).show();
        System.out.println(message+isAccept+"...........mRequestkey"+intent.getExtras().toString());
    }


    public void sendMessage(final JSONArray recipients, final String title, final String body, final String icon, final String message) {

        new AsyncTask<String, String, String>() {


            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("icon", icon);

                    JSONObject data = new JSONObject();
                    data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                    data.put("message", message);

                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("registration_ids", recipients);

                    String result = postToFCM(root.toString());
                    Log.d("Main Activity", "Result: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    String postToFCM(String bodyString) throws IOException {


        final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
        final MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + "AAAAf0mm85c:APA91bHtaUJdt0zjx_CzGmvXmnM8N4pmVdsX28VMDDYVR5lwZJ1uF4PZbNWNjBcjWRLdPXpJoMUr_71e-StDbGF6vUZtuK_c82yGA9lRPH4EkDlSW1q-S-1GXxNi4z2BjM8hO2diXbiQ")
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }
}
