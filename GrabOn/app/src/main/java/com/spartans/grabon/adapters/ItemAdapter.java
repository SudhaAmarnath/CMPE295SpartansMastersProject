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
 * Author : Sudha Amarnath on 2020-02-22
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Item> items;
    private ClickListenerItem itemClickListener;

    public ItemAdapter(ArrayList<Item> products, Context context, ClickListenerItem itemClickListener) {
        this.context = context;
        this.items = items;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_items_list,
                viewGroup, false);
        return new ViewHolder(v);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    @Override
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
        ImageView itemImage;

        ViewHolder(View v) {
            super(v);
            itemName = itemView.findViewById(R.id.layout_items_text);
            itemPrice = itemView.findViewById(R.id.layout_items_price);
            itemImage = itemView.findViewById(R.id.layout_items_image);
            v.setOnClickListener(this);
        }

        void bindModel(Item items) {
            this.item = items;
            itemName.setText(item.getItemName());
            itemPrice.setText(String.valueOf(item.getItemPrice()));
            Glide.with(context).load(item.getItemImageList().get(0)).into(itemImage);
        }

        public void onClick(View view) {
            itemClickListener.onClick(view, item);
        }

    }

}
