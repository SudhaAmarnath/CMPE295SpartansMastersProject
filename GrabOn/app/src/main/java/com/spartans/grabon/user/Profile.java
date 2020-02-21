package com.spartans.grabon.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.Login;
import com.spartans.grabon.R;

import javax.annotation.Nullable;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Profile extends AppCompatActivity {

    public static final String TAG = "TAG";
    private ListenerRegistration documentRefRegistration;

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

        uID = firebaseAuth.getCurrentUser().getUid();

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

        userLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    documentRefRegistration.remove();
                    FirebaseAuth.getInstance().signOut();//logout
                } catch (Exception logoutException) {
                    Log.d(TAG, "logoutException: " + logoutException.toString());
                }
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

    }
}
