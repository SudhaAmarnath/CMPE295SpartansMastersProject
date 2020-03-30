package com.spartans.grabon.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.tinydb.TinyDB;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.CartItemAdapter;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.payment.PaypalPaymentClient;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Cart extends AppCompatActivity {

    private String itemID;
    private String itemSellerUID;
    private String itemName;
    private String itemDesc;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;
    private Toolbar cartToolbar;
    private RecyclerView recyclerViewItems;
    private FirebaseFirestore db;
    private ArrayList<Item> itemsList = new ArrayList<>();
    private CartItemAdapter cartItemAdapter;
    private TinyDB tinyDB;
    private static double totalPrice;

    private FirebaseUser user = Singleton.getUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        FirebaseApp.initializeApp(this);

        cartToolbar = findViewById(R.id.CartToolbar);
        recyclerViewItems = findViewById(R.id.CartItemsList);
        db = Singleton.getDb();
        setSupportActionBar(cartToolbar);
        FancyButton backToItems, cartProceedForPayment;
        backToItems = findViewById(R.id.CartBackToItems);
        cartProceedForPayment = findViewById(R.id.CartProceedForPayment);

        backToItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        cartProceedForPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createOrderInDb();
                startActivity(new Intent(getApplicationContext(), PaypalPaymentClient.class));
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cart.this,
                LinearLayoutManager.VERTICAL,
                false);

        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.setHasFixedSize(true);

        getItemsFromTinyDB();

    }


    private void getItemsFromTinyDB() {

        tinyDB = new TinyDB(Cart.this);

        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        ArrayList<Item> items = new ArrayList<>();
        for(Object objs : savedObjects){
            items.add((Item) objs);
        }
        itemsList = items;

        Collections.reverse(itemsList);

        displayCartItems(itemsList);

    }


    public void displayCartItems(ArrayList<Item> items) {
        TextView cartsItemsTotal = findViewById(R.id.CartItemsTotal);
        if (cartItemAdapter == null) {
            cartItemAdapter = new CartItemAdapter(items, Cart.this);
            recyclerViewItems.setAdapter(cartItemAdapter);
            if (!items.isEmpty()) {
                Cart.totalPrice= 0;
                for (int i = 0; i < items.size(); i++) {
                    Cart.totalPrice += items.get(i).getItemPrice();
                }
                cartsItemsTotal.setText("Total Price: $" + Cart.totalPrice);
            } else {
                cartsItemsTotal.setVisibility(View.GONE);
            }
        } else {
            cartItemAdapter.getItems().clear();
            cartItemAdapter.getItems().addAll(items);
            cartItemAdapter.notifyDataSetChanged();
        }
    }


    public void createOrderInDb() {

        Map<String, Object> dborder = new HashMap<>();
        dborder.put("user_id", user.getUid());
        dborder.put("seller_id", itemsList.get(0).getItemSellerUID());
        dborder.put("items", itemsList);
        dborder.put("ordertotal", Cart.totalPrice);

        db.collection("orders")
                .add(dborder)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.v("Order:", documentReference.getId() + " successfully added");
                        tinyDB.remove(user.getUid());
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.v("Order:", "Create failed");
                    }
                });

    }



}
