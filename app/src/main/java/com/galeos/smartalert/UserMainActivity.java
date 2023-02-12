package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class UserMainActivity extends AppCompatActivity {

    ImageButton sosBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_main);

        sosBtn = findViewById(R.id.sosBtn);

        sosBtn.setOnClickListener((v)->startActivity(new Intent(UserMainActivity.this,NotifyActivity.class)));
    }
}