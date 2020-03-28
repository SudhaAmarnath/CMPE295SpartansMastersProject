package com.spartans.grabon.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.model.Item;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-03-26
 */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Item> items;
    private ClickListenerItem itemClickListener;

    public OrderItemAdapter(ArrayList<Item> items, Context context) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_cartitems_list,
                viewGroup, false);
        return new ViewHolder(v);
    }

    public ArrayList<Item> getItems() {
        return items;
    }


    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        viewHolder.bindModel(items.get(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        Item item;
        TextView itemName;
        TextView itemPrice;
        ImageView itemImage;

        ViewHolder(View v) {
            super(v);
            itemName = itemView.findViewById(R.id.layout_cartitems_text);
            itemPrice = itemView.findViewById(R.id.layout_cartitems_price);
            itemImage = itemView.findViewById(R.id.layout_cartitems_image);
        }

        void bindModel(Item items) {
            this.item = items;
            itemName.setText(item.getItemName());
            itemPrice.setText("$"+String.format("%.2f", item.getItemPrice()));
            Glide.with(context)
                    .load(item.getItemImageList().get(0))
                    .into(itemImage);
        }


    }

}
