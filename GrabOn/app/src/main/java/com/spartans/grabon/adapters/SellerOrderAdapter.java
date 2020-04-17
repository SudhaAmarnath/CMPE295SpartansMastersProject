package com.spartans.grabon.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.ClickListnerOrder;
import com.spartans.grabon.model.Order;
import com.spartans.grabon.utils.DateUtilities;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Author : Sudha Amarnath on 2020-03-28
 */
public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Order> orders;
    private ClickListnerOrder orderClickListener;
    private FirebaseFirestore db = Singleton.getDb();


    public SellerOrderAdapter(ArrayList<Order> orders, Context context, ClickListnerOrder orderClickListener) {
        this.context = context;
        this.orders = orders;
        this.orderClickListener = orderClickListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_sellerorder_list,
                viewGroup, false);
        return new ViewHolder(v);
    }

    public ArrayList<Order> getItems() {
        return orders;
    }


    public int getItemCount() {
        return orders.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bindModel(orders.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Order order;
        TextView orderId;
        TextView orderTotal;
        TextView orderTotalItems;
        TextView orderStatus;
        TextView orderTime;
        TextView orderModify;
        RecyclerView orderItemsList;

        ViewHolder(View v) {
            super(v);
            orderId = itemView.findViewById(R.id.layout_sellerorder_id);
            orderTotalItems = itemView.findViewById(R.id.layout_sellerorder_totalitems);
            orderTotal = itemView.findViewById(R.id.layout_sellerorder_total);
            orderItemsList = itemView.findViewById(R.id.layout_sellerorder_itemslist);
            orderStatus = itemView.findViewById(R.id.layout_sellerorder_status);
            orderTime = itemView.findViewById(R.id.layout_sellerorder_time);
            orderModify = itemView.findViewById(R.id.layout_sellerorder_modify);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL, false);

            orderItemsList.setLayoutManager(linearLayoutManager);
            orderItemsList.setHasFixedSize(true);

            v.setOnClickListener(this);


        }

        void bindModel(Order order) {
            this.order = order;
            ArrayList orderItems = order.getItems();
            orderId.setText(order.getOrderID());
            if (orderItems.size() != 0) {
                OrderItemAdapter orderItemAdapter = new OrderItemAdapter(orderItems, context);
                orderItemsList.setAdapter(orderItemAdapter);
                orderTotalItems.setText(String.valueOf(orderItems.size()));
                orderTotal.setText("$"+String.format("%.2f", order.getOrderTotal()));
                orderStatus.setText(order.getOrderStatus());
                if (order.getOrderStatus().matches("In Progress")) {
                    if (new DateUtilities().orderExpired(order.getOrderTime())) {
                        order.setOrderStatus("Expired");
                        String expiredtime = new DateUtilities().getPostTimeInMillis(order.getOrderTime(), 7);
                        order.setOrderModifyTime(expiredtime);
                        Map<String, Object> dbitem = new HashMap<>();
                        dbitem.put("orderstatus",order.getOrderStatus());
                        dbitem.put("ordermodifytime",order.getOrderModifyTime());
                        DocumentReference updateOrder = db.collection("orders")
                                .document(order.getOrderID());
                        updateOrder.update(dbitem)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.v("updateOrder", "Update Order Success:");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v("updateOrder", "Update Order Failed");
                            }
                        });
                    } else {
                        orderModify.setText("Pickup Order?");
                    }
                }else if (order.getOrderStatus().matches("Picked Up") ||
                        order.getOrderStatus().matches("Delivered")) {
                    String ordermodifytime = new DateUtilities().getDateAndTime(order.getOrderModifyTime());
                    orderStatus.setText(order.getOrderStatus() + " - " + ordermodifytime);
                    orderStatus.setTextColor(Color.parseColor("#4CAF50"));
                    String cancelallowedtill = new DateUtilities().getPostTimeInMillis(order.getOrderTime(), 30);
                    cancelallowedtill = new DateUtilities().getDateAndTime(cancelallowedtill);
                    orderModify.setText("Cancel Order? Eligible till " + cancelallowedtill);
                } else if (order.getOrderStatus().matches("Cancelled")) {
                    String ordermodifytime = new DateUtilities().getDateAndTime(order.getOrderModifyTime());
                    orderStatus.setText(order.getOrderStatus() + " - " + ordermodifytime);
                    orderStatus.setTextColor(Color.parseColor("#F44336"));
                    orderModify.setVisibility(View.GONE);
                } else if (order.getOrderStatus().matches("Expired")) {
                    String ordermodifytime = new DateUtilities().getDateAndTime(order.getOrderModifyTime());
                    orderStatus.setText(order.getOrderStatus() + " - " + ordermodifytime + " Order Not picked in 7 days. Refund In Progress");
                    orderStatus.setTextColor(Color.parseColor("#F44336"));
                    orderModify.setVisibility(View.GONE);
                }
                orderTime.setText(new DateUtilities().getDateAndTime(order.getOrderTime()));
            }
        }

        public void onClick(View view) {
            orderClickListener.onClick(view, order);
        }


    }

}
