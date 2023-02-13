package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class ChooseRoleActivity extends AppCompatActivity {

    ImageButton userBtn, employeeBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        userBtn = findViewById(R.id.userBtn);
        employeeBtn = findViewById(R.id.employeeBtn);

        userBtn.setOnClickListener((v) -> startActivity(new Intent(ChooseRoleActivity.this, LoginActivity.class).putExtra("isUser","1")));

        employeeBtn.setOnClickListener((v) -> startActivity(new Intent(ChooseRoleActivity.this, LoginActivity.class).putExtra("isUser","0")));
    }
}