package com.spartans.grabon.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.Login;
import com.spartans.grabon.R;

import javax.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Profile extends AppCompatActivity {

    public static final String TAG = "TAG";
    private ListenerRegistration documentRefRegistration;
    private FirebaseUser loggedinUser;
    private String providerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final EditText firstname, lastname, email, phone;
        final Button userLogout;

        final FirebaseAuth firebaseAuth;
        FirebaseFirestore firebaseFirestore;
        String uID;

        firstname = findViewById(R.id.Firstname);
        lastname = findViewById(R.id.Lastname);
        email = findViewById(R.id.Email);
        phone = findViewById(R.id.Phone);
        userLogout = findViewById(R.id.UserLogout);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loggedinUser = firebaseAuth.getCurrentUser();

        if (loggedinUser != null) {
            providerID = loggedinUser.getProviders().get(0);
            uID = loggedinUser.getUid();

            if (providerID.equals("google.com")) {
                firstname.setText(loggedinUser.getDisplayName());
                lastname.setText("");
                email.setText(loggedinUser.getEmail());
                phone.setText(loggedinUser.getPhoneNumber());
            } else {
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uID);
                documentRefRegistration = documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        firstname.setText(documentSnapshot.getString("firstname"));
                        lastname.setText(documentSnapshot.getString("lastname"));
                        email.setText(documentSnapshot.getString("email"));
                        phone.setText("+1123456789");
                    }
                });
            }
        }

        userLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (documentRefRegistration!=null) {
                        documentRefRegistration.remove();
                    }
                    FirebaseAuth.getInstance().signOut();//logout

                    if (providerID.equals("google.com")) {
                        Login.mGoogleSignInClient.signOut();
                    }
                } catch (Exception logoutException) {
                    Log.d(TAG, "logoutException: " + logoutException.toString());
                }
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

    }
}
