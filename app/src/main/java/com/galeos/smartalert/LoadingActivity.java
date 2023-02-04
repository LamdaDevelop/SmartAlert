package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(currentUser==null){
                    startActivity(new Intent(LoadingActivity.this,LoginActivity.class));
                }else{
                    startActivity(new Intent(LoadingActivity.this,MainActivity.class));
                }
                // Βαζουμε το finish για να μην μπορεί να επιστρέψει στο Loading screen πατώντας το κουμπί της επιστροφής
                finish();
            }
        },2000);

    }
}