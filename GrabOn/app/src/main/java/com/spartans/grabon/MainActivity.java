package com.spartans.grabon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.adapters.ItemAdapter;
import com.spartans.grabon.cart.Cart;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.AddItem;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.maps.MapsActivity;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.user.Profile;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private Toolbar toolbar;
    private RecyclerView recyclerViewItems;
    private FirebaseFirestore db;
    private ArrayList<Item> itemsList = new ArrayList<>();
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        toolbar = findViewById(R.id.toolbar);
        recyclerViewItems = findViewById(R.id.HomeActivityItemsList);
        db = Singleton.getDb();

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

        // show 2 items in grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);

        displayItems();

    }

    private void getItems(final FileDataStatus fileDataStatus) {

        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> imgs = new ArrayList<>();
                                Map<String, Object> myMap = document.getData();
                                for (Map.Entry<String, Object> entry : myMap.entrySet()) {
                                    if (entry.getKey().equals("itemimagelist")) {
                                        for (Object s : (ArrayList) entry.getValue()) {
                                            imgs.add((String) s);
                                        }
                                        Log.v("TagImg", entry.getValue().toString());

                                    }
                                }

                                Item item = new Item();
                                Double price;
                                item.setItemID(document.getId());
                                item.setItemSellerUID((String) myMap.get("selleruid"));
                                item.setItemName((String) myMap.get("itemname"));
                                item.setItemDescription((String) myMap.get("itemdesc"));
                                price = (Double) myMap.get("itemprice");
                                item.setItemPrice(price.floatValue());
                                item.setItemImageList(imgs);
                                itemsList.add(item);
                            }
                            fileDataStatus.onSuccess(itemsList);
                        } else {
                            fileDataStatus.onError("Error getting data");
                            Log.w(TAG, "Error getting data.", task.getException());
                        }

                    }
                });
    }

    private void displayItems () {

        getItems(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (itemAdapter == null) {
                    itemAdapter = new ItemAdapter(itemsList, MainActivity.this, new ClickListenerItem() {
                        @Override
                        public void onClick(View view, Item item) {
                            Intent itemPage = new Intent(MainActivity.this, ItemActivity.class);
                            itemPage.putExtra("itemid", item.getItemID());
                            itemPage.putExtra("selleruid",item.getItemSellerUID());
                            itemPage.putExtra("itemname",item.getItemName());
                            itemPage.putExtra("itemdesc", item.getItemDescription());
                            itemPage.putExtra("itemprice", item.getItemPrice());
                            itemPage.putExtra("itemimage", item.getItemImage());
                            itemPage.putExtra("itemimagelist", item.getItemImageList());
                            startActivity(itemPage);
                        }
                    });
                    recyclerViewItems.setAdapter(itemAdapter);
                } else {
                    itemAdapter.getItems().clear();
                    itemAdapter.getItems().addAll(itemsList);
                    itemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String e) {

            }
        });

    }

}
