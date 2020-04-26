package com.spartans.grabon.user;


import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
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

        auth = FirebaseAuth.getInstance();
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
                            user.put("paypalid", "");
                            user.put("address", "");
                            user.put("apt", "");
                            user.put("phone", "");
                            user.put("latitude", "");
                            user.put("longitude", "");

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


                            DocumentReference documentReference2 = db.collection("preferences").document(uID);
                            Map<String, Object> dbpref = new HashMap<>();
                            dbpref.put("distance", 25);
                            dbpref.put("grabon", true);
                            dbpref.put("ebay", true);
                            dbpref.put("craigslist", true);
                            dbpref.put("priceMin", 0);
                            dbpref.put("priceMax", 2000);
                            dbpref.put("numberItems", 15);
                            dbpref.put("userAddress", "");
                            dbpref.put("userZipcode", "");
                            dbpref.put("userCity", "");
                            dbpref.put("userLatitude", "");
                            dbpref.put("userLongitude", "");
                            dbpref.put("currentUserLat", "");
                            dbpref.put("currentUserLon", "");
                            dbpref.put("currentUserCity", "");
                            dbpref.put("currentUserZipcode", "");

                            documentReference2.set(dbpref).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "onSuccess: User Preferences is created for "+ uID);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d(TAG, "onFailure: User Preferences not created for "+ uID);
                                            }
                                        });
                                    }
                                }
                            });

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    registerProgressBar.setVisibility(View.GONE);
                                    Log.d(TAG, "signInWithCredential:success");
                                    Toast.makeText(Register.this, "User Registered and logged in successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }
                            }, 2000);

                            //saveProfileImgToStorage(uID);
                        } else {
                            Toast.makeText(Register.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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

    private void saveProfileImgToStorage(final String uID) {

        final Uri imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + getResources().getResourcePackageName(R.drawable.grabonwallpaper)
                + '/' + getResources().getResourceTypeName(R.drawable.grabonwallpaper)
                + '/' + getResources().getResourceEntryName(R.drawable.grabonwallpaper) );

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        final StorageReference ref = storageRef.child("profileImages/" + uID);

        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.v("uploadProfileImage", ref.getDownloadUrl().toString());
                        UploadTask uploadTask = ref.putFile(imageUri);
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                Log.v("uploadProfileImage", ref.getDownloadUrl().toString());
                                return ref.getDownloadUrl();
                            }

                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    Log.v("uploadProfileImage", "During Upload Image "+downloadUri.toString());
                                } else {
                                    Toast.makeText(Register.this, "Error in fileDataImageStatus", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Register.this, "Image Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                    }
                });

    }

}