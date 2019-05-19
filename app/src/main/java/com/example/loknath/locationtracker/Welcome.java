package com.example.loknath.locationtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

public class Welcome extends AppCompatActivity {

    LinearLayout l1,l2;
    Animation uptodown,downtoup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        l1= findViewById(R.id.l1);
        l2 = findViewById(R.id.l2);

        uptodown = AnimationUtils.loadAnimation(this, R.anim.uptodown);
        l1.setAnimation(uptodown);

        downtoup = AnimationUtils.loadAnimation(this,R.anim.downtoup);
        l2.setAnimation(downtoup);

        final Intent i = new Intent(this, Registration.class);

        Thread timer = new Thread(){
          public void run(){
              try{
                    sleep(5000);
              }catch (InterruptedException e){
                  e.printStackTrace();
              }
              finally {
                startActivity(i);
                finish();
              }
          }
        };
        timer.start();
    }
}
