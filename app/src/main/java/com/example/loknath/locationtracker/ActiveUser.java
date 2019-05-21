package com.example.loknath.locationtracker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.example.loknath.locationtracker.adaper.UserAdaper;
import com.example.loknath.locationtracker.dto.UserDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ActiveUser extends AppCompatActivity {

    public static String message = "My Name is User";

    private RecyclerView recyclerView;
    private ArrayList<UserDto> myArrayList;
    DatabaseReference mfirebase, senderRequestDb,recivererRequestDb;
    private String authID;
    private OkHttpClient mClient = new OkHttpClient();
    private String name = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_user);

        recyclerView = findViewById(R.id.rvUserList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        mfirebase = FirebaseDatabase.getInstance().getReference().child("User");

        Query query =  mfirebase.orderByChild("status").equalTo(true);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myArrayList = new ArrayList<UserDto>();
                authID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    // myArrayList.add(dataSnapshot1.getValue(UserAdaper.class));
                    UserDto userDto;//=new UserDto();
                    userDto = dataSnapshot1.getValue(UserDto.class);
                    userDto.key = dataSnapshot1.getKey().toString();
                    if (!authID.equals(userDto.key))
                        myArrayList.add(userDto);

                }

                UserAdaper userAdaper = new UserAdaper(myArrayList);
                userAdaper.setOnclickListener(new UserAdaper.OnclickListener() {
                    @Override
                    public void onItemClick(int position) {
                        // Create new post at /user-posts/$userid/$postid and at
                        // /posts/$postid simultaneously

                        senderRequestDb = FirebaseDatabase.getInstance().getReference().child("User").child(authID).child("Request").child(myArrayList.get(position).key);
                        recivererRequestDb = FirebaseDatabase.getInstance().getReference().child("User").child(myArrayList.get(position).key).child("Request").child(authID);

                        Map<String, Object> request = new HashMap<>();
                        request.put("isAccepted", false);
                        request.put("receiver", myArrayList.get(position).key);
                        request.put("sender", authID);
                        request.put("timestamp", ServerValue.TIMESTAMP);
                        senderRequestDb.updateChildren(request);
                        recivererRequestDb.updateChildren(request).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                AlertDialog alertDialog = new AlertDialog.Builder(ActiveUser.this).create();
                                alertDialog.setCancelable(false);
                                alertDialog.setCanceledOnTouchOutside(false);
                                // Setting Dialog Title
                                alertDialog.setTitle("Alert Dialog");

                                // Setting Dialog Message
                                alertDialog.setMessage("Request successfully");

                                // Setting OK Button
                                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Write your code here to execute after dialog closed
                                        onBackPressed();
                                    }
                                });

                                // Showing Alert Message
                                alertDialog.show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(ActiveUser.this, "Request Failure", Toast.LENGTH_SHORT).show();
                            }
                        });

                        final JSONArray jsonArray = new JSONArray();
                        jsonArray.put(myArrayList.get(position).token);
                        mfirebase.child(authID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                name = (String) dataSnapshot.child("Name").getValue();
                                sendMessage(jsonArray, getString(R.string.app_name), name + " request to track you Location ", "Http:\\google.com", message);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                });


                recyclerView.setAdapter(userAdaper);

                System.out.println(myArrayList.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                    data.put("mRequestkey", authID);
                    data.put("isSender", false);

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
                    Toast.makeText(ActiveUser.this, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActiveUser.this, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
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