package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class EmergenciesActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencies);

        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        getEmergencyDataFromFirebase();
    }

    void getEmergencyDataFromFirebase(){
        firestore = FirebaseFirestore.getInstance();

        //Collection Reference to all incidents
        CollectionReference incidentsRef = firestore.collection("incidents");
        Intent intent = getIntent();
        Query query = incidentsRef.whereEqualTo("Emergency", intent.getStringExtra("Emergency"));
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Use a HashSet to keep track of unique Emergency categories
                        //HashSet<String> uniqueEmergencies = new HashSet<>();
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
                            if (timeDiff <= hourInMillis /*&& !uniqueEmergencies.contains(emergency)*/) {
                                //uniqueEmergencies.add(emergency);
                                String incident = "Emergency: " + emergency +", Timestamp" + timestampStr + ", Location: " + location+", Comments: " + comments;
                                //String incident = "Emergency: " + emergency;
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
}