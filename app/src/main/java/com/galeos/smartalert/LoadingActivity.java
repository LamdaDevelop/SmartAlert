package com.galeos.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoadingActivity extends AppCompatActivity {

    FirebaseFirestore firestore;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //A delay for the loading animation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser==null){
                    startActivity(new Intent(LoadingActivity.this,ChooseRoleActivity.class));
                }else{
                    firestore = FirebaseFirestore.getInstance();
                    checkUserAccessLevel(firebaseUser);
                }
            }
        },2000);

    }
    // This method is designed to check the access level of a user in
    // Firebase Firestore and perform actions based on their role
    void checkUserAccessLevel(FirebaseUser firebaseuser){
        // pointing to "users" collection within Firestore
        DocumentReference df = firestore.collection("users").document(firebaseuser.getUid());
        df.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.getString("isUser").equals("1")){
                    startActivity(new Intent(LoadingActivity.this, UserMainActivity.class));
                    finish();
                }else if (documentSnapshot.getString("isUser").equals("0")){
                    startActivity(new Intent(LoadingActivity.this, EmployeeMainActivity.class));
                    finish();
                }
            }
        });
    }
}