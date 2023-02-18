package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;

public class UserMainActivity extends AppCompatActivity {

    ImageButton sosBtn,logoutBtn;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        sosBtn = findViewById(R.id.sosBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        sosBtn.setOnClickListener((v)->startActivity(new Intent(UserMainActivity.this,NotifyActivity.class)));

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(UserMainActivity.this,ChooseRoleActivity.class));
                finish();
            }
        });

    }
}