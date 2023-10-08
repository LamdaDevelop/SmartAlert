package com.galeos.smartalert;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    Button btnGoBack;
    FirebaseFirestore firestore;
    TextView earthquakeTextView,floodTextView,fireTextView,hurricaneTextView,tsunamiTextView,terroristAttackTextView,chemicalSpillsTextView,otherTextView,totalIncidentsTextView;
    int earthquakeCounter, floodCounter, fireCounter, hurricaneCounter, tsunamiCounter, terrAttackCounter, chemSpillsCounter, otherCounter, totalCounter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        setReferences();
        getStatistics();
    }

    private void getStatistics() {
        firestore = FirebaseFirestore.getInstance();
        CollectionReference incidentsRef = firestore.collection("incidents");
        ArrayList<Incidents> incidentArrayList = new ArrayList<>();
        // Query to get the incidents in order
        Query query = incidentsRef
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .orderBy("Emergency", Query.Direction.ASCENDING);
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
                            incidentArrayList.add(new Incidents(emergency, location, timestampStr));
                        }
                        earthquakeCounter =0;
                        fireCounter =0;
                        floodCounter=0;
                        hurricaneCounter=0;
                        tsunamiCounter=0;
                        terrAttackCounter=0;
                        chemSpillsCounter=0;
                        otherCounter=0;
                        totalCounter=0;
                        for (Incidents incident : incidentArrayList) {
                            switch (incident.getEmergency()) {
                                case "Earthquake":
                                    earthquakeCounter+=1;
                                    break;
                                case "Flood":
                                    floodCounter+=1;
                                    break;
                                case "Fire":
                                    fireCounter+=1;
                                    break;
                                case "Hurricane":
                                    hurricaneCounter+=1;
                                    break;
                                case "Tsunami":
                                    tsunamiCounter+=1;
                                    break;
                                case "Terrorist Attack":
                                    terrAttackCounter+=1;
                                    break;
                                case "Chemical Spills":
                                    chemSpillsCounter+=1;
                                    break;
                                case "Other":
                                    otherCounter+=1;
                                    break;
                                default:
                                    break;
                            }
                        }
                        totalCounter = earthquakeCounter+fireCounter+floodCounter+hurricaneCounter+tsunamiCounter+terrAttackCounter+chemSpillsCounter+otherCounter;
                        earthquakeTextView.setText(getString(R.string.StatisticsActEarthquake)+" "+ earthquakeCounter);
                        floodTextView.setText(getString(R.string.StatisticsActFlood)+" "+ floodCounter);
                        fireTextView.setText(getString(R.string.StatisticsActFire)+" "+ fireCounter);
                        hurricaneTextView.setText(getString(R.string.StatisticsActHurricane)+" "+ hurricaneCounter);
                        tsunamiTextView.setText(getString(R.string.StatisticsActTsunami)+" "+ tsunamiCounter);
                        terroristAttackTextView.setText(getString(R.string.StatisticsActTerrorist_Attack)+" "+ terrAttackCounter);
                        chemicalSpillsTextView.setText(getString(R.string.StatisticsActChemical_Spills)+" "+ chemSpillsCounter);
                        otherTextView.setText(getString(R.string.StatisticsActOther)+" "+ otherCounter);
                        totalIncidentsTextView.setText(getString(R.string.StatisticsActTotal_Incidents)+" "+ totalCounter);


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                    }
                });


    }

    private void setReferences(){
        btnGoBack = findViewById(R.id.goBackButton);
        earthquakeTextView = findViewById(R.id.earthquake);
        floodTextView = findViewById(R.id.flood);
        fireTextView = findViewById(R.id.fire);
        hurricaneTextView = findViewById(R.id.hurricane);
        tsunamiTextView = findViewById(R.id.tsunami);
        chemicalSpillsTextView = findViewById(R.id.chemicalSpills);
        terroristAttackTextView = findViewById(R.id.terroristAttack);
        otherTextView = findViewById(R.id.other);
        totalIncidentsTextView = findViewById(R.id.totalIncidents);

        btnGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(StatisticsActivity.this,UserMainActivity.class));
                finish();
            }
        });
    }



}
