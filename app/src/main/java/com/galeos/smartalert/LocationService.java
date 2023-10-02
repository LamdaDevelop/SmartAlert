package com.galeos.smartalert;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class LocationService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int NOTIFICATION_ID = 123;
    private static final String CHANNEL_ID = "location_channel";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createLocationRequest();

        // Create a notification to make this service a foreground service
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Running in the background")
                .setSmallIcon(R.drawable.ic_launcher_foreground);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, builder.build());

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createLocationRequest() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle location updates here
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Log.d("Location Update", "Latitude: " + latitude + ", Longitude: " + longitude);
                System.out.println(latitude);
                // You can perform any actions with the received location data here
                // For example, send it to a server or update your UI.
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };

        // Request location updates every 10 seconds (10,000 milliseconds)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                10000, // 10 seconds
                0,     // 0 meters (no minimum distance)
                locationListener
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop location updates and release resources when the service is destroyed
        locationManager.removeUpdates(locationListener);
    }
}
