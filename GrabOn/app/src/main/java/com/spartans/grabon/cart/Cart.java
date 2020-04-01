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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.tinydb.TinyDB;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.CartItemAdapter;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.payment.PaypalPaymentClient;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Cart extends AppCompatActivity {

    private static String itemID;
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
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        FirebaseApp.initializeApp(this);

        cartToolbar = findViewById(R.id.CartToolbar);
        recyclerViewItems = findViewById(R.id.CartItemsList);
        db = Singleton.getDb();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

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
                setItemOrderedInDb();
                startActivity(new Intent(getApplicationContext(), PaypalPaymentClient.class));
                finish();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cart.this,
                LinearLayoutManager.VERTICAL,
                false);

        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.setHasFixedSize(true);

        displayCartItems();

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.MainItemPullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cartItemAdapter = null;
                itemsList = new ArrayList<>();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cart.this,
                        LinearLayoutManager.VERTICAL,
                        false);
                recyclerViewItems.setLayoutManager(linearLayoutManager);
                recyclerViewItems.setHasFixedSize(true);
                pullToRefresh.setRefreshing(false);
                displayCartItems();
            }
        });

    }


    private void getItems(final FileDataStatus fileDataStatus) {

        tinyDB = new TinyDB(Cart.this);

        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        ArrayList<Item> items = new ArrayList<>();
        for(Object objs : savedObjects){
            items.add((Item) objs);
        }
        itemsList = items;
        fileDataStatus.onSuccess(itemsList);

    }

    private void displayCartItems () {

        getItems(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (cartItemAdapter == null) {
                    cartItemAdapter = new CartItemAdapter(itemsList, Cart.this, new ClickListenerItem() {
                        @Override
                        public void onClick(View view, Item item) {
                                Intent itemPage = new Intent(Cart.this, ItemActivity.class);
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
                    recyclerViewItems.setAdapter(cartItemAdapter);
                    TextView cartsItemsTotal = findViewById(R.id.CartItemsTotal);
                    if (!itemsList.isEmpty()) {
                        Cart.totalPrice= 0;
                        for (int i = 0; i < itemsList.size(); i++) {
                            Cart.totalPrice += itemsList.get(i).getItemPrice();
                        }
                        cartsItemsTotal.setText("Total Price: $" + Cart.totalPrice);
                    } else {
                        cartsItemsTotal.setVisibility(View.GONE);
                    }
                } else {
                    cartItemAdapter.getItems().clear();
                    cartItemAdapter.getItems().addAll(itemsList);
                    cartItemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String e) {

            }
        });

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


    public void setItemOrderedInDb() {

        for(int i=0; i < itemsList.size(); i++) {

            Cart.itemID = itemsList.get(i).getItemID();
            DocumentReference updateItem = db.collection("items")
                    .document(Cart.itemID);

            updateItem.update("itemordered", true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v("itemordered", "Update Item Ordered flag Success : " + Cart.itemID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.v("itemordered", "Update Item Ordered flag Failure: " + Cart.itemID);
                }
            });

        }

    }



}
