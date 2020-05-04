package com.spartans.grabon.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.SellerOrderAdapter;
import com.spartans.grabon.interfaces.ClickListnerOrder;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.model.Order;
import com.spartans.grabon.order.OrdersActivity;
import com.spartans.grabon.utils.DateUtilities;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Author : Sudha Amarnath on 2020-03-28
 */
public class SellerOrders extends Fragment {


    private ArrayList<Order> orderArrayList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();
    private SellerOrderAdapter sellerOrderAdapter;
    RecyclerView recyclerView;
    TextView sellerOrderTextView;

    public SellerOrders() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        getSellerOrdersList(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (sellerOrderAdapter == null) {
                    sellerOrderAdapter = new SellerOrderAdapter(orderArrayList, SellerOrders.this.getContext(), new ClickListnerOrder(){
                        @Override
                        public void onClick(View view, final Order order) {
                            updateOrder(order);
                        }
                    });

                    recyclerView.setAdapter(sellerOrderAdapter);
                } else {
                    sellerOrderAdapter.getItems().clear();
                    sellerOrderAdapter.getItems().addAll(orderArrayList);
                    sellerOrderAdapter.notifyDataSetChanged();
                }

                if (list.size() > 0) {
                    sellerOrderTextView.setVisibility(View.INVISIBLE);
                } else {
                    sellerOrderTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String e) {

            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_orders, container, false);
        recyclerView = view.findViewById(R.id.SellerOrdersFragmentRecyclerView);
        sellerOrderTextView = view.findViewById(R.id.SellerOrdersHeader);
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        return view;
    }

    private void getSellerOrdersList(final FileDataStatus fileDataStatus) {

        db.collection("orders")
                .orderBy("ordertime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (user.getUid().matches(documentSnapshot.get("seller_id").toString())) {
                                    Map mMap;
                                    ArrayList<Map> orderItems = (ArrayList<Map>) documentSnapshot.get("items");
                                    ArrayList<Item> itemArrayList = new ArrayList<>();

                                    String orderID = documentSnapshot.getId();
                                    String user_id = documentSnapshot.get("user_id").toString();
                                    String seller_id = documentSnapshot.get("seller_id").toString();
                                    String orderstatus = documentSnapshot.get("orderstatus").toString();
                                    String ordertime = documentSnapshot.get("ordertime").toString();
                                    String ordermodifytime = documentSnapshot.get("ordermodifytime").toString();

                                    if (orderItems != null) {
                                        for (int i = 0; i < orderItems.size(); i++) {
                                            Item item = new Item();
                                             mMap = orderItems.get(i);
                                            item.setItemName(mMap.get("itemName").toString());
                                            item.setItemID(mMap.get("itemID").toString());
                                            Double price = 0.0;
                                            Object priceFromDB = mMap.get("itemPrice");
                                            if (priceFromDB.getClass() == Double.class) {
                                                price = (Double) mMap.get("itemPrice");
                                            }
                                            else if (priceFromDB.getClass() == Long.class) {
                                                price = ((Long) mMap.get("itemPrice")).doubleValue();
                                            }
                                            item.setItemPrice(price.floatValue());
                                            item.setItemDescription(mMap.get("itemDescription").toString());
                                            item.setItemImageList((ArrayList <String>) mMap.get("itemImageList"));
                                            item.setItemSellerUID(mMap.get("itemSellerUID").toString());
                                            itemArrayList.add(item);
                                        }
                                    }

                                    Double order_total = (Double) documentSnapshot.get("ordertotal");

                                    Order order = new Order(
                                            orderID,
                                            user_id,
                                            seller_id,
                                            itemArrayList,
                                            order_total,
                                            orderstatus,
                                            ordertime,
                                            ordermodifytime
                                    );

                                    orderArrayList.add(order);

                                }
                            }
                            fileDataStatus.onSuccess(orderArrayList);
                        } else {
                            Log.e("Order:", "Order fetch failed");
                        }
                    }
                });

    }
    private static String neworderstatus = null;
    private void updateOrder(final Order order) {

        String orderstatus = order.getOrderStatus();

        if (orderstatus.equals("In Progress")) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SellerOrders.this.getContext());
            alertDialogBuilder.setTitle("Order ID: " + order.getOrderID());
            alertDialogBuilder
                    .setMessage("Did Buyer Pickup Order?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            neworderstatus = "Picked Up";
                            alertUpdate(order);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

    }

    private void alertUpdate(final Order order) {

        String orderModifyTime = new DateUtilities().getCurrentTimeInMillis();

        order.setOrderStatus(neworderstatus);
        order.setOrderModifyTime(orderModifyTime);

        Map<String, Object> dbitem = new HashMap<>();
        dbitem.put("orderstatus",order.getOrderStatus());
        dbitem.put("ordermodifytime",order.getOrderModifyTime());

        DocumentReference updateOrder = db.collection("orders")
                .document(order.getOrderID());
        updateOrder.update(dbitem)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("updateOrder", "Update Order Success:" + order.getOrderID());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("updateOrder", "Update Order Failed" + order.getOrderID());
            }
        });

        if (neworderstatus.equals("Picked Up")) {
            ArrayList<Item>  itemArrayList = order.getItems();
            for (int i = 0; i < itemArrayList.size(); i++) {
                final Item item = itemArrayList.get(i);
                DocumentReference updateItem = db.collection("items")
                        .document(item.getItemID());
                updateItem.update("itempicked", true)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.v("itempicked", "Update Item Picked flag Success : " + item.getItemID());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("itempicked", "Update Item Picked flag Failure: " + item.getItemID());
                    }
                });
            }
        }

        Toast.makeText(SellerOrders.this.getContext(), "Order " + neworderstatus, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(SellerOrders.this.getContext(), OrdersActivity.class);
        SellerOrders.this.getActivity().finish();
        startActivity(intent);

    }

}
