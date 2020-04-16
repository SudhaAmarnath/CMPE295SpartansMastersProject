package com.spartans.grabon.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.ClickListnerOrder;
import com.spartans.grabon.model.Order;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-03-28
 */
public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Order> orders;
    private ClickListnerOrder orderClickListener;


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
                    orderModify.setText("Pickup Order?");
                }else if (order.getOrderStatus().matches("Picked Up") ||
                        order.getOrderStatus().matches("Delivered")) {
                    orderStatus.setText(order.getOrderStatus() + " - " + order.getOrderModifyTime());
                    orderStatus.setTextColor(Color.parseColor("#4CAF50"));
                    orderModify.setText("Cancel Order? Return Eligible till ..");
                } else if (order.getOrderStatus().matches("Expired") ||
                        order.getOrderStatus().matches("Cancelled")) {
                    orderStatus.setText(order.getOrderStatus() + " - " + order.getOrderModifyTime());
                    orderStatus.setTextColor(Color.parseColor("#F44336"));
                    orderModify.setVisibility(View.GONE);
                }
                orderTime.setText(order.getOrderTime());
            }
        }

        public void onClick(View view) {
            orderClickListener.onClick(view, order);
        }


    }

}
