package com.spartans.grabon.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mukesh.tinydb.TinyDB;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.CartItemAdapter;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.DataCallback;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.order.OrdersActivity;
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
    private static String recepient = null;
    private static String recepientuid = null;
    private static CountDownLatch done;
    private static int i=0;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private static FancyButton backToItems, cartProceedForPayment;
    private static RadioGroup radioGroup;
    private static boolean pickup = true;
    private static int PAYMENT_REQUEST_CODE = 1;
    private static int PAYPAL_TO_CART_SUCCESS_CODE = 1;
    private static int PAYPAL_TO_CART_FAILURE_CODE = 0;
    private static String paymentID = null;
    private static boolean isCartItemsChanged = false;

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

        radioGroup.setVisibility(View.GONE);
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
                updateItemsInTinDB(new DataCallback() {
                    @Override
                    public void onCallback(String value) {
                        if (!Boolean.parseBoolean(value)) {
                            setItemOrderedInDb(true);
                            Intent paypal = new Intent(getApplicationContext(), PaypalPaymentClient.class);
                            paypal.putExtra("grandtotal", grandtotal);
                            paypal.putExtra("recepient", recepient);
                            startActivityForResult(paypal, PAYMENT_REQUEST_CODE);
                        } else {
                            tinyDB.remove(user.getUid());
                            for (i = 0; i < newItemList.size(); i++) {
                                Item item = newItemList.get(i);
                                addItemToTinyDB(item, tinyDB);
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
                    }
                });
            }
        });

        updateItemsInTinDB(new DataCallback() {
            @Override
            public void onCallback(String value) {
                if (Boolean.parseBoolean(value)) {

                    tinyDB.remove(user.getUid());
                    for (i = 0; i < newItemList.size(); i++) {
                        Item item = newItemList.get(i);
                        addItemToTinyDB(item, tinyDB);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYMENT_REQUEST_CODE) {
            if (resultCode == PAYPAL_TO_CART_SUCCESS_CODE) {
                paymentID = data.getStringExtra("paymentid");
                Log.v("Cart", "Payment successful. Paypal paymentId:" + paymentID);
                createOrderInDb();
                setItemOrderedInDb(true);
                Toast.makeText(getApplicationContext(), "Redirecting to the Orders Page", Toast.LENGTH_LONG).show();
            } else {
                setItemOrderedInDb(false);
                Log.v("Cart", "Payment Not done");
            }
        } else {
            setItemOrderedInDb(false);
            Log.v("Cart", "request not matched");
        }
    }

    private void getItems(final FileDataStatus fileDataStatus) {

        tinyDB = new TinyDB(Cart.this);

        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        ArrayList<Item> items = new ArrayList<>();
        for(Object objs : savedObjects){
            Item item = (Item) objs;
            items.add((Item) objs);
        }
        itemsList = items;
        fileDataStatus.onSuccess(itemsList);

    }

    private static ArrayList<Item> newItemList = new ArrayList<Item>();
    public void updateItemsInTinDB (final DataCallback dataCallback) {

        tinyDB = new TinyDB(Cart.this);
        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);

        final ArrayList<String> itemidList = new ArrayList<>();
        final ArrayList<Double> itempriceList = new ArrayList<>();
        for(Object objs : savedObjects) {
            Item tinydbitem = (Item) objs;
            String tinydbitemid = tinydbitem.getItemID();
            Double tinydbitemprice = Double.parseDouble(Float.toString(tinydbitem.getItemPrice()));
            itemidList.add(tinydbitemid);
            itempriceList.add(tinydbitemprice);
            Log.v("tinydb begin", tinydbitemid + ":"+ String.valueOf(tinydbitemprice));
        }

        db.collection("items")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    isCartItemsChanged = false;
                    newItemList = new ArrayList<Item>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> myMap = document.getData();
                        String itemid = (String) myMap.get("itemid");
                        int index = itemidList.indexOf(itemid);

                        if (index >= 0) {
                            Double tinydbitemprice = itempriceList.get(index);
                            boolean itemordered = (boolean) myMap.get("itemordered");
                            Double price = 0.0;
                            Object priceFromDB = myMap.get("itemprice");
                            if (priceFromDB.getClass() == Double.class) {
                                price = (Double) myMap.get("itemprice");
                            } else if (priceFromDB.getClass() == Long.class) {
                                price = ((Long) myMap.get("itemprice")).doubleValue();
                            }
                            ArrayList<String> imgs = new ArrayList<>();

                            for (Map.Entry<String, Object> entry : myMap.entrySet()) {
                                if (entry.getKey().equals("itemimagelist")) {
                                    for (Object s : (ArrayList) entry.getValue()) {
                                        imgs.add((String) s);
                                    }
                                    Log.v("TagImg", entry.getValue().toString());
                                }
                            }
                            Double pricediff = price - tinydbitemprice;

                            if (itemordered) {
                                isCartItemsChanged = true;
                                Log.v("tinydb", "itemordered true");
                            } else if (pricediff > 0.01 || pricediff < -0.01) {
                                isCartItemsChanged = true;
                                Log.v("tinydb", "price diff change");
                                Item item = new Item();
                                item.setItemID(itemid);
                                item.setItemSellerUID((String) myMap.get("selleruid"));
                                item.setItemName((String) myMap.get("itemname"));
                                item.setItemDescription((String) myMap.get("itemdesc"));
                                item.setItemPrice(Float.parseFloat(String.format("%.2f", price)));
                                item.setItemOrdered(itemordered);
                                item.setItemImageList(imgs);
                                item.setItemAddress((String) myMap.get("itemaddress"));
                                newItemList.add(item);
                            } else {
                                Item item = new Item();
                                item.setItemID(itemid);
                                item.setItemSellerUID((String) myMap.get("selleruid"));
                                item.setItemName((String) myMap.get("itemname"));
                                item.setItemDescription((String) myMap.get("itemdesc"));
                                item.setItemPrice(Float.parseFloat(String.format("%.2f", price)));
                                item.setItemOrdered(itemordered);
                                item.setItemImageList(imgs);
                                item.setItemAddress((String) myMap.get("itemaddress"));
                                newItemList.add(item);
                            }
                        }
                    }

                    if (newItemList.size() == 0) {
                        isCartItemsChanged = true;
                        Log.v("newitems", "is 0");
                    } else {
                        Log.v("newitems", "is not 0");
                        if (isCartItemsChanged == true) {
                            Toast.makeText(Cart.this, "Some items price changed or no longer available", Toast.LENGTH_LONG).show();
                        }
                    }
                    dataCallback.onCallback(String.valueOf(isCartItemsChanged));
                }
            }
        });

    }

    public void addItemToTinyDB(Item item, TinyDB tinyDB) {

        ArrayList<Object> savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        // Add item as object to the tinyDB
        savedObjects.add((Object) item);
        tinyDB.putListObject(user.getUid(), savedObjects);
        Log.v("TinDB Add", "Item:" + item.getItemID() + " is added");

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
                                itemPage.putExtra("itemordered", item.isItemOrdered());
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
                                recepientuid = item.getItemSellerUID();
                            }

                        }

                        db.collection("users")
                                .document(recepientuid)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            recepient = task.getResult().get("paypalid").toString();
                                            cartProceedForPayment.setClickable(true);
                                        }
                                    }
                                });
                        cartsItemsTotal.setText("$"+String.format("%.2f",totalPrice));
                        cartItemsShippingFees.setText("$"+String.format("%.2f",shippingTotal));
                        cartItemsTotalBeforeTax.setText("$"+String.format("%.2f",totalbeforetax));
                        cartItemsTotalTax.setText("$"+String.format("%.2f",totaltax));
                        grandtotal = totalbeforetax + totaltax;
                        cartItemsGrandTotal.setText("$"+String.format("%.2f",totalbeforetax + totaltax));

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


    public void setItemOrderedInDb(boolean value) {

        for(int i=0; i < itemsList.size(); i++) {

            Cart.itemID = itemsList.get(i).getItemID();
            itemsList.get(i).setItemOrdered(value);
            DocumentReference updateItem = db.collection("items")
                    .document(Cart.itemID);

            updateItem.update("itemordered", value)
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

    public void createOrderInDb() {


        String orderTime = new DateUtilities().getCurrentTimeInMillis();

        DocumentReference documentReference = db.collection("orders").document(paymentID);
        Map<String, Object> dborder = new HashMap<>();
        dborder.put("user_id", user.getUid());
        dborder.put("seller_id", recepientuid);
        dborder.put("items", itemsList);
        dborder.put("ordertotal", grandtotal);
        dborder.put("orderstatus", "In Progress");
        dborder.put("ordertime", orderTime);
        dborder.put("ordermodifytime", "");

        documentReference.set(dborder).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    task.addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v("Order:", paymentID + " successfully added");
                            tinyDB.remove(user.getUid());
                            startActivity(new Intent(getApplicationContext(), OrdersActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v("Order:", "Create failed");
                        }
                    });
                }
            }
        });

    }



}
