package com.spartans.grabon.user;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.R;
import com.spartans.grabon.utils.Singleton;

import javax.annotation.Nullable;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMLogoutHandler;

/**
 * Author : Sudha Amarnath on 2020-04-05
 */
public class Logout extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();;
    private ListenerRegistration documentRefRegistration = null;
    private String uID;
    private String providerID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logout);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = auth.getCurrentUser().getUid();
        providerID = user.getProviderData().get(1).getProviderId();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        try {

            if (user != null) {
                DocumentReference documentReference = firebaseFirestore.collection("users").document(uID);
                documentRefRegistration = documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                    }
                });
                Log.v("Logout", "Document Reference called");

                if (documentRefRegistration != null) {
                    documentRefRegistration.remove();
                    Log.v("Logout", "Document Reference unregister successful");
                }
                Log.v("Logout", "Now logging out user");
                FirebaseAuth.getInstance().signOut();
                if (providerID.equals("google.com")) {
                    Login.mGoogleSignInClient.signOut();
                } else if (providerID.equals("facebook.com")) {
                    LoginManager.getInstance().logOut();
                }
                Kommunicate.logout(getApplicationContext(), new KMLogoutHandler() {
                    @Override
                    public void onSuccess(Context context) {
                        Log.v("Logout", "Kommunicate Logout Success");
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.v("Logout", "Kommunicate Logout Failed");

                    }
                });
                Toast toast = Toast.makeText(this, "Logged Out Successfully", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (Exception logoutException) {
            Log.d("Logout", "LogoutException: " + logoutException.toString());
        }
        startActivity(new Intent(getApplicationContext(), Login.class));
        finish();

    }
}
