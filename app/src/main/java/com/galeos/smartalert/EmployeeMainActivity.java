package com.galeos.smartalert;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeMainActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseFirestore firestore;

    public static final double r = 6372.8;// In kilometers


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);

        setReferences();
        getLatestIncidentsFromFirebase();


        // Add OnItemClickListener to ListView
        incidents_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String emergency = arrayList.get(position);
                String[] splitEmergency = emergency.split("\\:",0);
                startActivity(new Intent(EmployeeMainActivity.this,EmergenciesActivity.class).putExtra("Emergency",splitEmergency[0]));
                finish();
            }
        });

    }

    private void setReferences(){
        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmployeeMainActivity.this,ChooseRoleActivity.class));
                finish();
            }
        });
    }

    void getLatestIncidentsFromFirebase() {
        //initialize a reference to the Firestore database
        firestore = FirebaseFirestore.getInstance();

        //initialize an ArrayList to store incident data
        ArrayList<Incidents> incidentArrayList = new ArrayList<>();


        // create a reference to the "incidents" collection in Firestore
        CollectionReference incidentsRef = firestore.collection("incidents");

        // Query to get the incidents in order
        //
        //    It orders the incidents by "Timestamp" in descending order, meaning the most recent incidents come first.
        //    It then orders the incidents by "Emergency" in ascending order.
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);

        // Εxecute the query and add a success listener to handle the results when the query is successful.
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // Iterate through the documents and group them by category and location
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Access the data in the document
                            String emergency = document.getString("Emergency");
                            String location = document.getString("Locations");
                            String timestampStr = document.getString("Timestamp");
                            String comments = document.getString("Comments");
                            Boolean declined = document.getBoolean("Declined");
                            System.out.println(declined);
                            if(!declined) {
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

                                // Only add Emergency category if it occurred within the last hour
                                if (timeDiff <= hourInMillis) {
                                    incidentArrayList.add(new Incidents(emergency, location, timestampStr, comments, declined));
                                }
                            }
                        }
                        // Create a map to group incidents by emergency type
                        Map<String, Map<String, List<Incidents>>> groupedIncidents = groupIncidents(incidentArrayList);
                        List<String> categories = new ArrayList();
                        for (Map.Entry<String, Map<String, List<Incidents>>> emergencyEntry : groupedIncidents.entrySet()) {
                            String emergencyType = emergencyEntry.getKey();
                            Map<String, List<Incidents>> locationMap = emergencyEntry.getValue();

                            //It counts the number of incidents in each location and creates a string
                            // containing the emergency type, location, and the count of incidents.
                            for (Map.Entry<String, List<Incidents>> locationEntry : locationMap.entrySet()) {
                                String location = locationEntry.getKey();
                                List<Incidents> incidentsAtLocation = locationEntry.getValue();
                                System.out.println("Location: " + location);
                                int count = 0;
                                for (Incidents incident : incidentsAtLocation) {
                                    count+=1;
                                }
                                String subcategory = emergencyType + ","+location+ ":" + count;
                                categories.add(subcategory);
                            }
                        }
                        // It sorts the categories list based on the incident count
                        // in descending order using a custom comparator.
                        Collections.sort(categories, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                int count1 = Integer.parseInt(o1.substring(o1.lastIndexOf(":") + 1));
                                int count2 = Integer.parseInt(o2.substring(o2.lastIndexOf(":") + 1));
                                return Integer.compare(count2, count1);
                            }
                        });
                        // adds the sorted categories to an existing arrayList and
                        // notifies the adapter to update the UI.
                        arrayList.addAll(categories);
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.getMessage());
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static Map<String, Map<String, List<Incidents>>> groupIncidents(List<Incidents> incidentsList) {
        // Create a map to group incidents by emergency type and location
        Map<String, Map<String, List<Incidents>>> groupedIncidents = new HashMap<>();
        // Create a map to hold ArrayLists of incidents for each location
        Map<String, List<Incidents>> incidentsByLocation = new HashMap<>();

        // Iterate through the incidents
        for (Incidents incident : incidentsList) {
            String emergencyType = incident.emergency;
            String location = incident.location;
            String comments = incident.comments;
            // Get or create a map for the current emergency type
            Map<String, List<Incidents>> locationMap = groupedIncidents.getOrDefault(emergencyType, new HashMap<>());

            // Iterate through existing locations to find a nearby one
            boolean addedToExistingLocation = false;
            for (Map.Entry<String, List<Incidents>> entry : locationMap.entrySet()) {
                String existingLocation = entry.getKey();
                double[] coordinates1 = splitCoordinates(location);
                double[] coordinates2 = splitCoordinates(existingLocation);
                if (coordinates1 != null && coordinates2 != null) {
                    double distance = calculateDistance(coordinates1[0], coordinates1[1], coordinates2[0], coordinates2[1]);
                    if (distance <= 15.0) {
                        // Add to the existing location group
                        entry.getValue().add(incident);
                        addedToExistingLocation = true;
                        break;
                    }
                }
            }
            if (!addedToExistingLocation) {
                // Create a new location group
                List<Incidents> incidentsAtLocation = new ArrayList<>();
                incidentsAtLocation.add(incident);
                locationMap.put(location, incidentsAtLocation);
            }
            // Put the location map back into the emergency type map
            groupedIncidents.put(emergencyType, locationMap);
        }
        return groupedIncidents;
    }


    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Radius of the Earth in kilometers
        double radius = 6371;
        // Convert latitude and longitude from degrees to radians
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);
        // Haversine formula
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        // Calculate the distance
        double distance = radius * c;
        return distance;
    }


    // Function to split a coordinates string into latitude and longitude as doubles
    public static double[] splitCoordinates(String coordinates) {
        String[] parts = coordinates.split(",");
        if (parts.length == 2) {
            try {
                double lat = Double.parseDouble(parts[0]);
                double lon = Double.parseDouble(parts[1]);
                return new double[] { lat, lon };
            } catch (NumberFormatException e) {
                // Handle parsing errors
                return null;
            }
        } else {
            return null;
        }
    }

}