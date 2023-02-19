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
        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        //getLatestIncidentsFromFirebase();
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
                //Toast.makeText(EmployeeMainActivity.this, emergency+timestamp+location, Toast.LENGTH_SHORT).show();
                //getEmergencyDataFromFirebase(emergency);
                startActivity(new Intent(EmployeeMainActivity.this,EmergenciesActivity.class)
                        .putExtra("Emergency",emergency)
                        .putExtra("Timestamp",timestamp)
                        .putExtra("Location",location));
            }
        });

    }


    /*  Retrieves the latest incidents data from a Firebase Firestore database
        and groups them by category and location. It then calculates the number
        of incidents that occurred within the last hour for each category and
        location and displays them in a list.*/
    void getLatestIncidentsFromFirebase() {
        firestore = FirebaseFirestore.getInstance(); //Get an instance of the Firestore database

        // Create a map to store the counts of incidents for each category
        Map<String, Map<String, Integer>> categoryMap = new HashMap<>();

        //Get a reference to the "incidents" collection in the Firestore database.
        CollectionReference incidentsRef = firestore.collection("incidents");

        //Create a query to get the incidents in descending order by timestamp and ascending order by emergency type
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);

        //Execute the query and handle the result using a success listener
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Iterate through the documents and group them by category and location
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Access the data in the document
                            String emergency = document.getString("Emergency");
                            String location = document.getString("Locations");
                            String timestampStr = document.getString("Timestamp");

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

                            /* Only add Emergency category if it occurred within the last hour
                                by calculating the time difference between the current time and the incident time,
                                which is stored in the Timestamp field of the Firestore document.
                                If the time difference is less than or equal to 1 hour (hourInMillis),
                                the incident is considered to be recent and it will be added to the categoryMap*/
                            if (timeDiff <= hourInMillis ) {
                                /*The category string is composed of the emergency and location fields of
                                the Firestore document, and is used as the key in the categoryMap*/
                                String category = emergency + " (" + location + ")";
                                /*The locationMap is a HashMap that stores the counts
                                of incidents for each location within a category.*/
                                Map<String, Integer> locationMap = categoryMap.get(category);

                                /*If the locationMap for the given category does not exist yet,
                                a new HashMap is created and added to the categoryMap*/
                                if (locationMap == null) {
                                    locationMap = new HashMap<>();
                                    categoryMap.put(category, locationMap);
                                }
                                /* If the location does not exist yet in the locationMap,
                                 a count of 0 is assigned to count*/
                                Integer count = locationMap.get(location);
                                if (count == null) {
                                    count = 0;
                                }
                                /*Then the count for the given location is incremented by 1 in the locationMap*/
                                locationMap.put(location, count + 1);
                            }
                        }
                        /* creates a list of strings called "categories" that represents
                        each emergency category with their respective counts.*/
                        List<String> categories = new ArrayList<>();
                        /*It iterates over each key (i.e., category) in the "categoryMap"
                        and retrieves the corresponding location map.*/
                        for (String category : categoryMap.keySet()) {
                            Map<String, Integer> locationMap = categoryMap.get(category);
                            /* for each key (i.e., location) in the location map,
                            it retrieves the count and concatenates it with the category
                            name to form a subcategory string*/
                            for (String location : locationMap.keySet()) {
                                int count = locationMap.get(location);
                                String subcategory = category + ":" + count;
                                //adds the subcategory string to the "categories" list
                                categories.add(subcategory);
                            }
                        }
                        // Sort the categories by count in descending order
                        Collections.sort(categories, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                int count1 = Integer.parseInt(o1.substring(o1.lastIndexOf(":") + 1));
                                int count2 = Integer.parseInt(o2.substring(o2.lastIndexOf(":") + 1));
                                return Integer.compare(count2, count1);

                            }
                        });

                        // Add the categories to the adapter and notify it of the data set changes
                        arrayList.addAll(categories);
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



    ArrayList<String> groupedEmergencies(){
        ArrayList<String> grouped = new ArrayList<>();
        ArrayList<String> all = new ArrayList<>();

        //Get a reference to the "incidents" collection in the Firestore database.
        CollectionReference incidentsRef = firestore.collection("incidents");

        //Create a query to get the incidents in descending order by timestamp and ascending order by emergency type
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);
        //Execute the query and handle the result using a success listener
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Iterate through the documents and group them by category and location
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Access the data in the document
                            String emergency = document.getString("Emergency");
                            String location = document.getString("Locations");
                            String timestampStr = document.getString("Timestamp");
                            int id = 1;
                            String category = emergency + id;
                            for (DocumentSnapshot document2 : queryDocumentSnapshots){
                                String location2 = document.getString("Locations");
                                if(distance(location, location2)<20.0){

                                }
                            }
                            grouped.add(category);
                        }
                    }
                });

        return grouped;
    }

    void test(){
        firestore = FirebaseFirestore.getInstance();
        //Get a reference to the "incidents" collection in the Firestore database.
        CollectionReference incidentsRef = firestore.collection("incidents");

        //Create a query to get the incidents in descending order by timestamp and ascending order by emergency type
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);
        //Execute the query and handle the result using a success listener
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        HashMap<String, DocumentSnapshot> matchingIncidents = new HashMap<>();
                        for (DocumentSnapshot document1 : queryDocumentSnapshots) {
                            String emergency1 = document1.getString("Emergency");
                            String location1 = document1.getString("Locations");
                            String timestamp1 = document1.getString("Timestamp");
                            int counter=0;
                            Log.d("TEST","OK4");
                            // Convert timestamp string to Date object so we can calculate the difference between incident time and current time
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                            Date timestamp = null;
                            try {
                                timestamp = format.parse(timestamp1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            // Calculate time difference between incident time and current time
                            Date currentTime = new Date();
                            long timeDiff = currentTime.getTime() - timestamp.getTime();
                            long hourInMillis = 60 * 60 * 1000;
                            // Only add Emergency category if it hasn't been added before and occurred within the last hour
                            if (timeDiff <= hourInMillis) {
                                Log.d("TEST","OK3");
                                for (DocumentSnapshot document2 : queryDocumentSnapshots) {
                                    String emergency2 = document2.getString("Emergency");
                                    String location2 = document2.getString("Locations");
                                    String timestamp2 = document2.getString("Timestamp");
                                    Log.d("TEST","OK2");
                                    if(!timestamp1.equals(timestamp2) && emergency1.equals(emergency2)&& distance(location1,location2)<=20) {

                                        Log.d("TEST","OK1");
                                        // Convert timestamp string to Date object so we can calculate the difference between incident time and current time
                                        SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                        Date timestampTemp = null;
                                        try {
                                            timestampTemp = format2.parse(timestamp2);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        // Calculate time difference between incident time and current time
                                        Date currentTime2 = new Date();
                                        long timeDiff2 = currentTime2.getTime() - timestampTemp.getTime();
                                        long hourInMillis2 = 60 * 60 * 1000;
                                            if (timeDiff2 <= hourInMillis2) {
                                                Log.d("TEST","OK");
                                                matchingIncidents.put(String.valueOf(counter), document2);
                                                /*if(!arrayList.contains(timestamp2)){
                                                    arrayList.add(timestamp2);
                                                    adapter.notifyDataSetChanged();
                                                }*/
                                            }
                                    }
                                }
                            }
                            counter+=1;
                        }
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



