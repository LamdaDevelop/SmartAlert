package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EmergenciesActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private static final double r = 6372.8; // In kilometers
    String curEmergency, curTimestamp, curLocation;
    String alertPoint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencies);


        logoutBtn = findViewById(R.id.logoutBtn);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        //incidents_listview.setAdapter(adapter);
        Intent intent = getIntent();
        curEmergency = intent.getStringExtra("Emergency");
        curTimestamp = intent.getStringExtra("Timestamp");
        curLocation = intent.getStringExtra("Location");

        getEmergencyDataFromFirebase();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmergenciesActivity.this,ChooseRoleActivity.class));
                finish();
            }
        });




    }

    void getEmergencyDataFromFirebase(){
        firestore = FirebaseFirestore.getInstance();
        //Collection Reference to all incidents
        CollectionReference incidentsRef = firestore.collection("incidents");
        Query query = incidentsRef.whereEqualTo("Emergency", curEmergency);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int counter = 0;
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String emergency = document.getString("Emergency");
                            String location = document.getString("Locations");
                            String timestamp = document.getString("Timestamp");
                            if(!timestamp.equals(curTimestamp) && emergency.equals(curEmergency)&& distance(location,curLocation)<=20){
                                // Convert timestamp string to Date object so we can calculate the difference between incident time and current time
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                Date dateTimestamp = null;
                                try {
                                    dateTimestamp = format.parse(timestamp);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                // Calculate time difference between incident time and current time
                                Date currentTime = new Date();
                                long timeDiff = currentTime.getTime() - dateTimestamp.getTime();
                                long hourInMillis = 60 * 60 * 1000;
                                // Only add Emergency category if it hasn't been added before and occurred within the last hour
                                if (timeDiff <= hourInMillis) {
                                    counter+=1;
                                    Log.d("TEST",timestamp);
                                }
                            }
                        }

                        alertPoint = alert(counter);
                        Log.d("TEST",alertPoint+"-"+counter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }

    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return r * c;
    }

    public static double distance(String loc1, String loc2) {
        String[] parts1 = loc1.split(",");
        String[] parts2 = loc2.split(",");
        double lat1 = Double.parseDouble(parts1[0]);
        double lon1 = Double.parseDouble(parts1[1]);
        double lat2 = Double.parseDouble(parts2[0]);
        double lon2 = Double.parseDouble(parts2[1]);
        return haversine(lat1, lon1, lat2, lon2);
    }

    String alert(int counter){
        if(counter==1){
            return "Minor Emergency";
        }else if(counter>=2 && counter <=3){
            return "Major Emergency";
        }else if(counter>=4 && counter <=10){
            return "Critical Emergency";
        }else{
            return "Catastrophic Emergency";
        }
    }

    void sendAlertMessage(){
        // Obtain the FirebaseMessaging instance
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();

        // Create a Notification message to send
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Title of the notification")
                .setContentText("Message text of the notification")
                .setSmallIcon(R.drawable.icons8_alert)
                .build();

        // Send the message using the FirebaseMessaging instance
        //messaging.send(notification);
    }

}