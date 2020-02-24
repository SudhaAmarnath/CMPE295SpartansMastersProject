package com.spartans.grabon;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spartans.grabon.utils.Singleton;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class Login extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FancyButton loginButton, loginGmailButton, loginFacebookButton;
    private ProgressBar loginProgressBar;
    private FirebaseAuth auth;
    private TextView loginCreateUser;
    private FirebaseFirestore db;
    private String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.LoginEmail);
        loginPassword = findViewById(R.id.LoginPassword);
        loginButton = findViewById(R.id.LoginButton);
        loginProgressBar = findViewById(R.id.LoginProgressBar);
        loginCreateUser = findViewById(R.id.LoginCreateUser);

        auth = Singleton.getAuth();
        db = Singleton.getDb();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    loginEmail.setError("Enter Email ID");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    loginPassword.setError("Enter Password");
                    return;
                }

                loginProgressBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                        }else {
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loginProgressBar.setVisibility(View.GONE);
                        }

                    }
                });

            }
        });


        loginCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });


    }
}
