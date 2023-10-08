package com.galeos.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class CreateAccountActivity extends AppCompatActivity {
    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    Button createAccountBtn;
    ProgressBar progressBar;
    TextView loginBtnTextView;
    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore firestore;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        setReferences();
    }

    //Set the References of each object
    private void setReferences(){
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
        //Get the email,password and confirmPassword text
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        //Call the validateData method to check if the data are correct
        boolean isValidated = validateData(email,password,confirmPassword);
        if(!isValidated){
            return;
        }

        //If the data are correct call the createAccountInFirebase method
        createAccountInFirebase(email,password);
    }

    void createAccountInFirebase(String email, String password){
        //ProgressBar method
        changeInProgress(true);

        //Get the instances for the authentication and the firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        //Create user
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CreateAccountActivity.this,
                new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        changeInProgress(false);
                        //If the user is created
                        if(task.isSuccessful()){
                            //Get the current User
                            firebaseUser = firebaseAuth.getCurrentUser();
                            //Notify with a Toast that the account was created with success
                            Toast.makeText(CreateAccountActivity.this,getString(R.string.succ_creat_acc), Toast.LENGTH_SHORT).show();

                            //Set the role of the user in firestore
                            Intent intent = getIntent();
                            String isUser = intent.getStringExtra("isUser");
                            Users users = new Users();
                            users.setIsUser(isUser);
                            firestore.collection("users").document(firebaseUser.getUid()).set(users);

                            //Send an Email Verification and signOut
                            firebaseAuth.getCurrentUser().sendEmailVerification();
                            firebaseAuth.signOut();
                            finish();
                        }else{
                            Toast.makeText(CreateAccountActivity.this, R.string.Emailused, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //Just a progressBar
    void changeInProgress(boolean inProgress){
        if(inProgress){
            progressBar.setVisibility(View.VISIBLE);
            createAccountBtn.setVisibility(View.GONE);
        }else{
            progressBar.setVisibility(View.GONE);
            createAccountBtn.setVisibility(View.VISIBLE);
        }
    }

    //Validates the data with some simple if statements
    boolean validateData(String email, String password, String confirmPassowrd){
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            emailEditText.setError(getString(R.string.email_invalid));
            return false;
        }
        if(password.length()<6){
            passwordEditText.setError(getString(R.string.password_invalid));
            return false;
        }
        if(!password.equals(confirmPassowrd)){
            confirmPasswordEditText.setError(getString(R.string.password_not_match));
            return false;
        }
        return true;
    }
}