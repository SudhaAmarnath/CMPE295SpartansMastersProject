package com.spartans.grabon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.spartans.grabon.payment.PaypalPaymentClient;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText mainName, mainEmail;
        Button mainLogout, mainProceedForPayment;

        final FirebaseAuth firebaseAuth;
        TextView loginCreateUser;
        FirebaseFirestore firebaseFirestore;
        String uID;

        mainName = findViewById(R.id.MainName);
        mainEmail = findViewById(R.id.MainEmail);
        mainLogout = findViewById(R.id.MainLogout);
        mainProceedForPayment = findViewById(R.id.MainProceedForPayment);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        uID = firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(uID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                mainName.setText(documentSnapshot.getString("fullName"));
                mainEmail.setText(documentSnapshot.getString("email"));

            }
        });

        mainProceedForPayment.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(getApplicationContext(), PaypalPaymentClient.class));
                 finish();
             }
         }
        );

        mainLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();//logout
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }
        });

    }
}
