package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;

public class UserMainActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    ImageButton sosBtn,logoutBtn, statistics_btn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        // Start the LocationService
        //startLocationService();
        //createNotificationChannel();
        sosBtn = findViewById(R.id.sosBtn);
        logoutBtn = findViewById(R.id.logoutBtn);
        statistics_btn = findViewById(R.id.statistics_btn);
        sosBtn.setOnClickListener((v)->startActivity(new Intent(UserMainActivity.this,NotifyActivity.class)));

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserMainActivity.this,ChooseRoleActivity.class));
                finish();
            }
        });

        statistics_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserMainActivity.this,StatisticsActivity.class));
                finish();
            }
        });

    }
     /*
    private void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        startService(serviceIntent);
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "location_channel";
            CharSequence channelName = "Location Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }*/
/*
    private void startLocationThread() {
        Thread helloThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(10000); // Sleep for 10 seconds
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // Print "Hello" on the main UI thread
                                System.out.println("Hello");
                            }
                        });
                    } catch (InterruptedException e) {
                        // Handle any exceptions or cleanup if needed
                        e.printStackTrace();
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }
            }
        });

        helloThread.start();
    }
 */
}