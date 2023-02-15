package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

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
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class EmployeeMainActivity extends AppCompatActivity {
    ListView incidents_listview;
    Button statistics_btn;
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
        statistics_btn = findViewById(R.id.statistics_btn);

        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,arrayList);
        incidents_listview.setAdapter(adapter);
        getIncidentsFromFirebase();
    }

    void getIncidentsFromFirebase(){
        firestore = FirebaseFirestore.getInstance();
        CollectionReference incidentsRef = firestore.collection("incidents");

        incidentsRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<DocumentSnapshot> documents = queryDocumentSnapshots.getDocuments();
                        // Use the documents list as needed
                        for(int i=0;i<documents.size();i++){
                            arrayList.add(String.valueOf(documents.get(i).getData()));
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