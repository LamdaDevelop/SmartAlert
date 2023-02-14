package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;


public class NotifyActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    TextView timestamp_info_text_view,location_info_text_view;
    Timestamp timestamp;
    EditText comments_edit_text;
    Spinner dropdown_spinner;
    Button submit_button;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;

    Incidents incident;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        dropdown_spinner = findViewById(R.id.dropdown_spinner);
        timestamp_info_text_view = findViewById(R.id.timestamp_info_text_view);
        location_info_text_view = findViewById(R.id.location_info_text_view);
        comments_edit_text = findViewById(R.id.comments_edit_text);
        submit_button = findViewById(R.id.submit_button);
        //Spinner for categories drop down
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.dropdown_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        dropdown_spinner.setAdapter(adapter);

        //Current timestamp
        timestamp = new Timestamp(System.currentTimeMillis());
        timestamp_info_text_view.setText(timestamp.toString());

        //Get current Location
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        submit_button.setOnClickListener(v -> createIncident());
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        location_info_text_view.setText(location.getLatitude()+","+location.getLongitude());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void createIncident(){
        String emergency="";
        switch (dropdown_spinner.getSelectedItemPosition()){
            case 0:
                emergency = "Earthquake";
                break;
            case 1:
                emergency = "Flood";
                break;
            case 2:
                emergency = "Fire";
                break;
            case 3:
                emergency = "Hurricane";
                break;
            case 4:
                emergency = "Tsunami";
                break;
            case 5:
                emergency = "Terrorist Attack";
                break;
            case 6:
                emergency = "Chemical Spills";
                break;
            case 7:
                emergency = "Other";
                break;
        }
        String location = location_info_text_view.getText().toString();
        String timestamp = timestamp_info_text_view.getText().toString();
        String comments = comments_edit_text.getText().toString();

        boolean isValidated = validateData();
        if(!isValidated){
            return;
        }
        incident = new Incidents(emergency,location,timestamp,comments);
        createIncidentInFirebase(incident);

    }

    boolean validateData(){
        if(location_info_text_view.getText().toString().equals("")){
            location_info_text_view.setError(getString(R.string.Couldnt_track_location));
            return false;
        }
        if(timestamp_info_text_view.getText().toString()==""){
            timestamp_info_text_view.setError(getString(R.string.Couldnt_get_timestamp));
            return false;
    }
        if(comments_edit_text.getText().toString().equals("")){
        comments_edit_text.setError(getString(R.string.Please_add_some_comments));
        return false;
    }
        return true;
    }


    void createIncidentInFirebase(Incidents incident){

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        Map<String,Object> incidentInfo = new HashMap<>();
        incidentInfo.put("Emergency",incident.getEmergency());
        incidentInfo.put("Locations",incident.getLocation());
        incidentInfo.put("Timestamp",incident.getTimestamp());
        incidentInfo.put("Comments",incident.getComments());

        String document =incident.getEmergency() + firebaseUser.getUid();
        firestore.collection("incidents").document(document).set(incidentInfo);
        Toast.makeText(NotifyActivity.this, R.string.Incident_sent_successfully, Toast.LENGTH_SHORT).show();
        finish();


    }

}