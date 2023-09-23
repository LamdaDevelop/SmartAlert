package com.galeos.smartalert;
import android.Manifest;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import java.util.List;
import java.util.Random;

public class EmergenciesActivity extends AppCompatActivity implements LocationListener {
    ListView incidents_listview;
    Button logoutBtn, messageBtn;
    ArrayList<String> arrayList;
    ArrayAdapter<String> adapter;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    private static final double r = 6372.8; // In kilometers
    String curEmergency, curTimestamp, curLocation;
    String alertPoint;
    TextView emergency_info_text_view, location_info_text_view;

    //Geo
    private static final String TAG = "EmergenciesActivity";
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private float GEOFENCE_RADIUS = 2000;
    private String GEOFENCE_ID = ""+new Random().nextInt()+"";
    LocationManager locationManager;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergencies);

        logoutBtn = findViewById(R.id.logoutBtn);
        messageBtn = findViewById(R.id.messageBtn);
        emergency_info_text_view = findViewById(R.id.emergency_info_text_view);
        location_info_text_view = findViewById(R.id.location_info_text_view);

        arrayList = new ArrayList<>();
        //location instantiate
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //Geo
        geofencingClient = LocationServices.getGeofencingClient(this);
        geofenceHelper = new GeofenceHelper(this);

        Intent intent = getIntent();
        curEmergency = intent.getStringExtra("Emergency");
        String[] splitCurEmergency = curEmergency.split("\\,", 0);
        curEmergency = splitCurEmergency[0];
        double lat = Double.parseDouble(splitCurEmergency[1]);
        double lon = Double.parseDouble(splitCurEmergency[2]);

        emergency_info_text_view.setText(curEmergency);
        location_info_text_view.setText(lat + "," + lon);

        //Get current Location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            finish();
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            finish();
            Toast.makeText(EmergenciesActivity.this, getString(R.string.Turnon_location_message), Toast.LENGTH_SHORT).show();
        }

        messageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: ADD LAT LON RADIUS FROM PREVIOUS ACTIVITY
                    addGeofence(lat, lon, GEOFENCE_RADIUS);
            }
        });
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EmergenciesActivity.this, ChooseRoleActivity.class));
                finish();
            }
        });
    }

    private void addGeofence(double lat, double lon, float radius) {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID, lat, lon, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Geofence Added...");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                        Log.d(TAG, "onFailure " + errorMessage);
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have the permission
                Toast.makeText(this, "You can add geofences...", Toast.LENGTH_SHORT).show();
            } else {
                //We do not have the permission..
                Toast.makeText(this, "Background location access is neccessary for geofences to trigger...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

}