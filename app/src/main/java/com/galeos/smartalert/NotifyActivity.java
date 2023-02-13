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
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.sql.Timestamp;



public class NotifyActivity extends AppCompatActivity implements LocationListener {

    LocationManager locationManager;
    TextView timestamp_info_text_view,location_info_text_view;
    Timestamp timestamp;
    Spinner dropdown_spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        dropdown_spinner = findViewById(R.id.dropdown_spinner);
        timestamp_info_text_view = findViewById(R.id.timestamp_info_text_view);
        location_info_text_view = findViewById(R.id.location_info_text_view);

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


    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        location_info_text_view.setText(location.getLatitude()+","+location.getLongitude());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}