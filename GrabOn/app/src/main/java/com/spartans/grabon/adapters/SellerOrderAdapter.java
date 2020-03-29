package com.spartans.grabon.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spartans.grabon.R;
import com.spartans.grabon.model.Order;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-03-28
 */
public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Order> orders;

    public SellerOrderAdapter(ArrayList<Order> orders, Context context) {
        this.context = context;
        this.orders = orders;
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        Order order;
        TextView orderId;
        TextView orderTotal;
        TextView orderTotalItems;
        RecyclerView orderItemsList;

        ViewHolder(View v) {
            super(v);
            orderId = itemView.findViewById(R.id.layout_sellerorder_id);
            orderTotalItems = itemView.findViewById(R.id.layout_sellerorder_totalitems);
            orderTotal = itemView.findViewById(R.id.layout_sellerorder_total);
            orderItemsList = itemView.findViewById(R.id.layout_sellerorder_itemslist);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,
                    LinearLayoutManager.VERTICAL, false);

            orderItemsList.setLayoutManager(linearLayoutManager);
            orderItemsList.setHasFixedSize(true);

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
            }
        }

    }

}
