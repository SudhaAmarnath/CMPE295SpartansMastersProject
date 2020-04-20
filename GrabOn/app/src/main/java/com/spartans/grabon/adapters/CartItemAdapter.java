package com.spartans.grabon.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.internal.VisibilityAwareImageButton;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.model.Item;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-03-21
 */
public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Item> items;
    private ClickListenerItem itemClickListener;

    public CartItemAdapter(ArrayList<Item> items, Context context, ClickListenerItem itemClickListener) {
        this.context = context;
        this.items = items;
        this.itemClickListener = itemClickListener;
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Item item;
        TextView itemName;
        TextView itemPrice;
        TextView itemPriceDB;
        ImageView itemImage;
        TextView itemSoldMsg;

        ViewHolder(View v) {
            super(v);
            itemName = itemView.findViewById(R.id.layout_cartitems_text);
            itemPrice = itemView.findViewById(R.id.layout_cartitems_price);
            itemPriceDB = itemView.findViewById(R.id.layout_cartitems_price_DB);
            itemImage = itemView.findViewById(R.id.layout_cartitems_image);
            itemSoldMsg = itemView.findViewById(R.id.itemAlreadySold);
            v.setOnClickListener(this);
        }

        void bindModel(Item items) {
            this.item = items;
            itemName.setText(item.getItemName());
            if (items.getItemPrice() != items.getItemPriceFromDB()) {
                itemPriceDB.setVisibility(View.VISIBLE);
                itemPrice.setText("$" + String.format("%.2f", item.getItemPrice()));
                itemPrice.setTextColor(Color.RED);
                itemPrice.setPaintFlags(itemPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                itemPriceDB.setText("$" + String.format("%.2f", item.getItemPriceFromDB()));
            } else {
                itemPrice.setText("$" + String.format("%.2f", item.getItemPrice()));
                itemPriceDB.setVisibility(View.GONE);
            }
            Glide.with(context).load(item.getItemImageList().get(0)).into(itemImage);
            if (item.isItemOrderedFromDB()) {
                itemSoldMsg.setVisibility(View.VISIBLE);
            } else {
                itemSoldMsg.setVisibility(View.INVISIBLE);
            }
        }

        public void onClick(View view) {
            itemClickListener.onClick(view, item);
        }

    }

}
