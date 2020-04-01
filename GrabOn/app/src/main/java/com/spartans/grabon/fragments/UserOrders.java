package com.spartans.grabon.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.UserOrderAdapter;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.model.Order;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
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
                    userOrderAdapter = new UserOrderAdapter(list, getContext());
                    recyclerView.setAdapter(userOrderAdapter);
                } else {
                    userOrderAdapter.getItems().clear();
                    userOrderAdapter.getItems().addAll(list);
                    userOrderAdapter.notifyDataSetChanged();
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
        Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        return view;
    }

    private void getUserOrdersList(final FileDataStatus fileDataStatus) {

        db.collection("orders").get()
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
                                                order_total
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

}
