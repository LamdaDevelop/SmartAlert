package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.List;

public class EmployeeMainActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button logoutBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    Incidents incident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);

        incidents_listview = findViewById(R.id.incidents_listview);
        logoutBtn = findViewById(R.id.logoutBtn);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        getIncidentsFromFirebase();
        logoutBtn.setOnClickListener((v)->startActivity(new Intent(EmployeeMainActivity.this,ChooseRoleActivity.class)));
        //rankIncidents(arrayList);
    }

    void getIncidentsFromFirebase(){
        firestore = FirebaseFirestore.getInstance();
        CollectionReference incidentsRef = firestore.collection("incidents");
        Query query = incidentsRef.orderBy("Emergency", Query.Direction.ASCENDING)
                .orderBy("Timestamp", Query.Direction.DESCENDING);
        query.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        //List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Access the data in the document
                            String emergency = document.getString("Emergency");
                            String comments = document.getString("Comments");
                            String location = document.getString("Locations");
                            String timestamp = document.getString("Timestamp");

                            // Add the data to the ArrayList
                            String incident = "Emergency: " + emergency + ", Comments: " + comments
                                    + ", Location: " + location + ", Timestamp: " + timestamp.toString();
                            arrayList.add(incident);
                        }
                        for (String incident : arrayList) {
                            Log.d("Test", "Incident: " + incident);
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



