package com.galeos.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

public class NotifyActivity extends AppCompatActivity implements LocationListener {

    private TextView textView;
    private TextView textView2;
    private LocationManager locationManager;
    private ImageView imageView;
    Button imageBtn;
    String[] options = {"Earthquake", "Flood", "Fire", "Hurricane", "Tsunami", "Terrorist Attack", "Chemical Spills"};



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notify);
        textView = findViewById(R.id.timeStampTxt);
        textView2 = findViewById(R.id.locationTxt);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        imageView = findViewById(R.id.photo_image_view);
        imageBtn = findViewById(R.id.imageBtn);
        Spinner spinner = (Spinner) findViewById(R.id.dropdown_spinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode==1 && resultCode == RESULT_OK && null !=data){
            Uri selectedImage = data.getData();
            String[] filepath = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage, filepath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filepath[0]);
            String picturepath = cursor.getString(columnIndex);
            cursor.close();

            imageView.setImageBitmap(BitmapFactory.decodeFile(picturepath));
        }
    }

    public void GetTimestamp(View view) {
        Date dateCurrent = Calendar.getInstance().getTime();
        textView.setText(dateCurrent.toString());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        textView2.setText(location.getLatitude()+","+location.getLongitude());

    }
}