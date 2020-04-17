package com.spartans.grabon.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
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
import com.spartans.grabon.utils.DateUtilities;
import com.spartans.grabon.utils.SalesTaxCalculator;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static double shippingFees;
    private static double totalPrice;
    private static String state = null;
    private static double shippingTotal = 0;
    private static double totalbeforetax = 0;
    private static double totaltax = 0;
    private static double grandtotal = 0;
    private static CountDownLatch done;
    private static int i=0;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static FancyButton backToItems, cartProceedForPayment;
    private static RadioGroup radioGroup;
    private static boolean pickup = true;

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

        radioGroup = findViewById(R.id.CartItemsRadioGroup);
        setSupportActionBar(cartToolbar);
        backToItems = findViewById(R.id.CartBackToItems);
        cartProceedForPayment = findViewById(R.id.CartProceedForPayment);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cart.this,
                LinearLayoutManager.VERTICAL,
                false);

        recyclerViewItems.setLayoutManager(linearLayoutManager);
        recyclerViewItems.setHasFixedSize(true);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.CartItemsRadioPickup) {
                    pickup = true;
                } else {
                    pickup = false;
                }
                cartItemAdapter = null;
                itemsList = new ArrayList<>();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Cart.this,
                        LinearLayoutManager.VERTICAL,
                        false);
                recyclerViewItems.setLayoutManager(linearLayoutManager);
                recyclerViewItems.setHasFixedSize(true);
                displayCartItems();
            }
        });


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
                                itemPage.putExtra("itemaddress", item.getItemAddress());
                                startActivity(itemPage);
                        }
                    });
                    recyclerViewItems.setAdapter(cartItemAdapter);
                    TextView cartsItemsTotal = findViewById(R.id.CartItemsTotal);
                    TextView cartItemsShippingFees = findViewById(R.id.CartItemsShippingFees);
                    TextView cartItemsTotalBeforeTax = findViewById(R.id.CartItemsTotalBeforeTax);
                    TextView cartItemsTotalTax = findViewById(R.id.CartItemsTotalTax);
                    TextView cartItemsGrandTotal = findViewById(R.id.CartItemsGrandTotal);
                    if (!itemsList.isEmpty()) {
                        totalPrice= 0;
                        shippingTotal = 0;
                        totalbeforetax = 0;
                        totaltax=0;
                        if (pickup == true) {
                            shippingFees = 0;
                        } else {
                            shippingFees = 2;
                        }

                        for (i = 0; i < itemsList.size(); i++) {
                            Item item = itemsList.get(i);
                            String address = item.getItemAddress();
                            Log.v("sales", address);
                            String pattern = ", ([a-zA-Z]+) (\\d+),";
                            Pattern r = Pattern.compile(pattern);
                            Matcher m = r.matcher(address);
                            if (m.find()) {
                                state = m.group(1);
                                double itemprice = item.getItemPrice();
                                double itemshippingfees = shippingFees;
                                shippingTotal += shippingFees;
                                double itemtotal = itemprice + itemshippingfees;
                                totalbeforetax += itemtotal;
                                double taxrate = new SalesTaxCalculator().getSalesTax(state);
                                double itemtax = itemtotal * taxrate / 100;
                                totaltax += itemtax;
                                totalPrice += item.getItemPrice();
                            }

                        }
                        cartsItemsTotal.setText("$"+String.format("%.2f",totalPrice));
                        cartItemsShippingFees.setText("$"+String.format("%.2f",shippingTotal));
                        cartItemsTotalBeforeTax.setText("$"+String.format("%.2f",totalbeforetax));
                        cartItemsTotalTax.setText("$"+String.format("%.2f",totaltax));
                        grandtotal = totalbeforetax + totaltax;
                        cartItemsGrandTotal.setText("$"+String.format("%.2f",totalbeforetax + totaltax));
                        cartProceedForPayment.setClickable(true);
                    } else {
                        cartsItemsTotal.setVisibility(View.GONE);
                        cartItemsShippingFees.setVisibility(View.GONE);
                        cartItemsTotalBeforeTax.setVisibility(View.GONE);
                        cartItemsTotalTax.setVisibility(View.GONE);
                        cartItemsGrandTotal.setVisibility(View.GONE);
                        cartProceedForPayment.setClickable(false);
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


        String orderTime = new DateUtilities().getCurrentTimeInMillis();

        Map<String, Object> dborder = new HashMap<>();
        dborder.put("user_id", user.getUid());
        dborder.put("seller_id", itemsList.get(0).getItemSellerUID());
        dborder.put("items", itemsList);
        dborder.put("ordertotal", grandtotal);
        dborder.put("orderstatus", "In Progress");
        dborder.put("ordertime", orderTime);
        dborder.put("ordermodifytime", "");

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
