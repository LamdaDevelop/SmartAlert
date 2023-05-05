package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.w3c.dom.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EmployeeMainActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    Incidents incident;
    private static final double r = 6372.8; // In kilometers
    private static final double RADIUS_IN_KM = 20.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);
        registerToTopic();

        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        getEmergencyDataFromFirebase();
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployeeMainActivity.this,ChooseRoleActivity.class));
                finish();
            }
        });

        // Add OnItemClickListener to ListView
        incidents_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] fields = arrayList.get(position).split("/");
                String emergency = fields[0].substring("Emergency: ".length());
                String timestamp = fields[1].substring("Timestamp: ".length());
                String location = fields[2].substring("Location: ".length());
                //getEmergencyDataFromFirebase(emergency);
                startActivity(new Intent(EmployeeMainActivity.this,EmergenciesActivity.class)
                        .putExtra("Emergency",emergency)
                        .putExtra("Timestamp",timestamp)
                        .putExtra("Location",location));
            }
        });


    }



    void getEmergencyDataFromFirebase(){
        firestore = FirebaseFirestore.getInstance();

        //Collection Reference to all incidents
        CollectionReference incidentsRef = firestore.collection("incidents");
        //Query to get the incidents in order
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Access the data in the document
                            String emergency = document.getString("Emergency");
                            String timestampStr = document.getString("Timestamp");
                            String location = document.getString("Locations");
                            String comments = document.getString("Comments");

                            // Convert timestamp string to Date object so we can calculate the difference between incident time and current time
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            Date timestamp = null;
                            try {
                                timestamp = format.parse(timestampStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            // Calculate time difference between incident time and current time
                            Date currentTime = new Date();
                            long timeDiff = currentTime.getTime() - timestamp.getTime();
                            long hourInMillis = 60 * 60 * 1000;
                            // Only add Emergency category if it hasn't been added before and occurred within the last hour
                            if (timeDiff <= hourInMillis) {
                                String incident = "Emergency: " + emergency +"/\nTimestamp: " + timestampStr + "/\nLocation: " + location+"/\nComments: " + comments;
                                arrayList.add(incident);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });
    }


    void registerToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("weather")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                        Log.d("TEST", msg);
                        Toast.makeText(EmployeeMainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}



