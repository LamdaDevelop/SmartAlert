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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class EmployeeMainActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    Incidents incident;
    public static final double r = 6372.8;// In kilometers


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);
        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        getLatestIncidentsFromFirebase();
        logoutBtn.setOnClickListener((v)->startActivity(new Intent(EmployeeMainActivity.this,ChooseRoleActivity.class)));

        // Add OnItemClickListener to ListView
        incidents_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String emergency = arrayList.get(position).substring(arrayList.get(position).indexOf(":") + 2);
                //Toast.makeText(EmployeeMainActivity.this, emergency, Toast.LENGTH_SHORT).show();
                //getEmergencyDataFromFirebase(emergency);
                startActivity(new Intent(EmployeeMainActivity.this,EmergenciesActivity.class).putExtra("Emergency",emergency));
            }
        });
        //rankBasedOnNumberOfIncidents();
    }

    //Get the Incidents that occurred within the last hour
    void getLatestIncidentsFromFirebase(){
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
                        // Use a HashSet to keep track of unique Emergency categories
                        HashSet<String> uniqueEmergencies = new HashSet<>();
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
                            if (timeDiff <= hourInMillis && !uniqueEmergencies.contains(emergency)) {
                                uniqueEmergencies.add(emergency);
                                //String incident = "Emergency: " + emergency +", Timestamp" + timestampStr + ", Location: " + location+", Comments: " + comments;
                                String incident = "Emergency: " + emergency;
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



    void getEmergencyDataFromFirebase(String emergency) {
        firestore = FirebaseFirestore.getInstance();
        CollectionReference incidentsRef = firestore.collection("incidents");
        Query query = incidentsRef.whereEqualTo("Emergency", emergency);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        StringBuilder sb = new StringBuilder();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String comments = document.getString("Comments");
                            String location = document.getString("Locations");
                            String timestamp = document.getString("Timestamp");
                            sb.append("Location: ").append(location).append(", Timestamp: ").append(timestamp).append(", Comments: ").append(comments).append("\n");
                        }
                        String toastMessage = "Emergency: " + emergency + "\n" + sb.toString();
                        Toast.makeText(EmployeeMainActivity.this, toastMessage, Toast.LENGTH_LONG).show();
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

    void rankBasedOnNumberOfIncidents(){
        HashMap<String, Integer> emergencyCounts = new HashMap<>();
        for (String incident : arrayList) {
            String emergency = incident.substring(11); // Assumes that "Emergency: " is always 11 characters
            if (emergencyCounts.containsKey(emergency)) {
                emergencyCounts.put(emergency, emergencyCounts.get(emergency) + 1);
            } else {
                emergencyCounts.put(emergency, 1);
            }
        }
        for (Map.Entry<String, Integer> entry : emergencyCounts.entrySet()) {
            String emergency = entry.getKey();
            int count = entry.getValue();
            Toast.makeText(EmployeeMainActivity.this, "Emergency " + emergency + " occurred " + count + " times.", Toast.LENGTH_LONG).show();
        }
    }



/*
    void rankIncidents(ArrayList<String> arrayList){
        int numberOfIncidents = 0;
        Location startPoint=new Location("locationA");
        startPoint.setLatitude(17.372102);
        startPoint.setLongitude(78.484196);

        Location endPoint=new Location("locationA");
        endPoint.setLatitude(17.375775);
        endPoint.setLongitude(78.469218);

        double distance=startPoint.distanceTo(endPoint);
        if(distance<50000){
            //OK
            Toast.makeText(EmployeeMainActivity.this, String.valueOf(distance), Toast.LENGTH_SHORT).show();
        }else{
            //NOT OK
            Toast.makeText(EmployeeMainActivity.this, "too far", Toast.LENGTH_SHORT).show();
        }



    }*/

}



