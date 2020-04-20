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
import com.spartans.grabon.adapters.UserOrderAdapter;
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
 * Author : Sudha Amarnath on 2020-03-27
 */
public class UserOrders extends Fragment {


    private ArrayList<Order> orderArrayList = new ArrayList<>();
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();
    private UserOrderAdapter userOrderAdapter;
    RecyclerView recyclerView;
    TextView userOrderTextView;

    public UserOrders() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        getUserOrdersList(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (userOrderAdapter == null) {
                    userOrderAdapter = new UserOrderAdapter(orderArrayList, UserOrders.this.getContext(), new ClickListnerOrder() {
                        @Override
                        public void onClick(View view, final Order order) {
                            updateOrder(order);
                        }
                    });
                    recyclerView.setAdapter(userOrderAdapter);
                } else {
                    userOrderAdapter.getItems().clear();
                    userOrderAdapter.getItems().addAll(orderArrayList);
                    userOrderAdapter.notifyDataSetChanged();
                }

                if (list.size() > 0) {
                    userOrderTextView.setVisibility(View.INVISIBLE);
                } else {
                    userOrderTextView.setVisibility(View.VISIBLE);
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
        View view = inflater.inflate(R.layout.fragment_user_orders, container, false);
        recyclerView = view.findViewById(R.id.UserOrdersFragmentRecyclerView);
        userOrderTextView = view.findViewById(R.id.UserOrdersHeader);
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        return view;
    }

    private void getUserOrdersList(final FileDataStatus fileDataStatus) {

        db.collection("orders")
                .orderBy("ordertime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                                if (user.getUid().matches(documentSnapshot.get("user_id").toString())) {
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
                                            Double price = (Double) mMap.get("itemPrice");
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

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserOrders.this.getContext());
            alertDialogBuilder.setTitle("Order ID: " + order.getOrderID());
            alertDialogBuilder
                    .setMessage("Modify this order?")
                    .setCancelable(false)
                    .setPositiveButton("Cancel Order?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            neworderstatus = "Cancelled";
                            alertUpdate(order);

                        }
                    })
                    .setNeutralButton("Pickup Order?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            neworderstatus = "Picked Up";
                            alertUpdate(order);

                        }
                    })
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();


        } else if (orderstatus.equals("Picked Up") &&
                new DateUtilities().orderCanBeCancelled(order.getOrderTime())) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(UserOrders.this.getContext());
            alertDialogBuilder.setTitle("Order ID: " + order.getOrderID());
            alertDialogBuilder
                    .setMessage("Cancel this order?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            neworderstatus = "Cancelled";
                            alertUpdate(order);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
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

        Toast.makeText(UserOrders.this.getContext(), "Order " + neworderstatus, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(UserOrders.this.getContext(), OrdersActivity.class);
        UserOrders.this.getActivity().finish();
        startActivity(intent);
    }

}
