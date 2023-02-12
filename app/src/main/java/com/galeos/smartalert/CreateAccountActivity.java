package com.galeos.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    Button createAccountBtn;
    ProgressBar progressBar;
    TextView loginBtnTextView;

    FirebaseUser firebaseuser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;
    DocumentReference df;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        createAccountBtn = findViewById(R.id.create_account_btn);
        progressBar = findViewById(R.id.progress_bar);
        loginBtnTextView = findViewById(R.id.login_text_view_btn);

        createAccountBtn.setOnClickListener( v->createAccount() );
        loginBtnTextView.setOnClickListener( v->finish() );
    }

    void createAccount(){
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        boolean isValidated = validateData(email,password,confirmPassword);
        if(!isValidated){
            return;
        }

        createAccountInFirebase(email,password);
    }

    void createAccountInFirebase(String email, String password){
        changeInProgress(true);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CreateAccountActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        changeInProgress(false);
                        if(task.isSuccessful()){
                            firebaseuser = firebaseAuth.getCurrentUser();
                            Toast.makeText(CreateAccountActivity.this,"@string/succ_creat_acc", Toast.LENGTH_SHORT).show();
                            Intent intent = getIntent();
                            String isUser = intent.getStringExtra("isUser");

                            Log.d("User",firebaseuser.getUid());
                            Users users = new Users();
                            users.setIsUser(isUser);
                            firestore.collection("users").document(firebaseuser.getUid()).set(users);
                            //Map<String,Object> userInfo = new HashMap<>();

                            //userInfo.put("isUser",users);
                            //df.set(userInfo);

                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            firebaseAuth.signOut();
                            finish();
                        }else{
                            Toast.makeText(CreateAccountActivity.this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            createAccountBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            createAccountBtn.setVisibility(View.VISIBLE);
        }
    }

    boolean validateData(String email, String password, String confirmPassowrd){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError("@string/email_invalid");
            return false;
        }
        if(password.length()<6){
            passwordEditText.setError("@string/password_invalid");
            return false;
        }
        if(!password.equals(confirmPassowrd)){
            confirmPasswordEditText.setError("@string/password_not_match");
            return false;
        }
        return true;
    }
}