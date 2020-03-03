package com.spartans.grabon;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spartans.grabon.utils.Singleton;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class Register extends AppCompatActivity {

    private EditText registerFirstName, registerLastName, registerEmail, registerPassword;
    private FancyButton registerSignUp;
    private TextView registerAlreadyLogin;
    private ProgressBar registerProgressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String uID;
    public static final String TAG = "TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerFirstName = findViewById(R.id.RegisterFirstName);
        registerLastName = findViewById(R.id.RegisterLastName);
        registerEmail = findViewById(R.id.RegisterEmail);
        registerPassword = findViewById(R.id.RegisterPassword);
        registerSignUp = findViewById(R.id.RegisterSignUp);
        registerAlreadyLogin = findViewById(R.id.RegisterAlreadyLogin);
        registerProgressBar = findViewById(R.id.RegisterProgressBar);

        auth = Singleton.getAuth();
        db = Singleton.getDb();

        /*
        if(auth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finish();
        }
        */

        registerSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email = registerEmail.getText().toString().trim();
                final String password = registerPassword.getText().toString().trim();
                final String firstName = registerFirstName.getText().toString().trim();
                final String lastName = registerLastName.getText().toString().trim();

                if(TextUtils.isEmpty(firstName)) {
                    registerFirstName.setError("Enter First Name");
                    return;
                }

                if(TextUtils.isEmpty(lastName)) {
                    registerLastName.setError("Enter Last Name");
                    return;
                }

                if(TextUtils.isEmpty(email)) {
                    registerEmail.setError("Enter Email ID");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    registerPassword.setError("Enter Password");
                    return;
                }

                if(password.length() < 8) {
                    registerPassword.setError("Password must be atleast 8 characters");
                    return;
                }

                registerProgressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            uID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = db.collection("users").document(uID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("firstname", firstName);
                            user.put("lastname", lastName);
                            user.put("email", email);
                            user.put("password", password);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "onSuccess: User Profile is created for "+ uID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.toString());
                                }
                            });
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            Toast.makeText(Register.this,"User Registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            registerProgressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });

        registerAlreadyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Login.class));
            }
        });

    }
}
