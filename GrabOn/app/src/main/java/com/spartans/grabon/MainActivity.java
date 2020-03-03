package com.spartans.grabon;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.cart.Cart;
import com.spartans.grabon.item.AddItem;
import com.spartans.grabon.maps.MapsActivity;
import com.spartans.grabon.user.Profile;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private ListenerRegistration documentRefRegistration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView mBottomNav = findViewById(R.id.MainBottomNavigation);
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.map_button:
                        startActivity(new Intent(getApplicationContext(),MapsActivity.class));
                        break;
                    case R.id.add_item:
                        startActivity(new Intent(getApplicationContext(),AddItem.class));
                        break;
                    case R.id.navigation_cart:
                        startActivity(new Intent(getApplicationContext(),Cart.class));
                        break;
                    case R.id.profile_button:
                        startActivity(new Intent(getApplicationContext(),Profile.class));
                        break;
                }
                return true;
            }
        });


    }
}
