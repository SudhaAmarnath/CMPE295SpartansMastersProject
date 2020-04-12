package com.spartans.grabon.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.ClickListenerItemCategory;
import com.spartans.grabon.model.ItemCategory;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-04-11
 */
public class ItemCategoryAdapter extends RecyclerView.Adapter<ItemCategoryAdapter.CategoryViewHolder> {

    private Context context;
    private ArrayList<ItemCategory> itemCategories;
    private ClickListenerItemCategory itemCategoryClickListener;
    int selected_position = 0;

    public ItemCategoryAdapter(ArrayList<ItemCategory> itemCategories, Context context, ClickListenerItemCategory itemCategoryClickListener) {
        this.context = context;
        this.itemCategories = itemCategories;
        this.itemCategoryClickListener = itemCategoryClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_item_category,
                viewGroup, false);
        return new CategoryViewHolder(v);
    }

    public ArrayList<ItemCategory> getItemCategories() {
        return itemCategories;
    }


    public int getItemCount() {
        return itemCategories.size();
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder viewHolder, int position) {
        ItemCategory itemCategory = itemCategories.get(position);
        if (MainActivity.category.equals("") || selected_position != position) {
            viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#CABBEB"));
        }
        viewHolder.bindModel(itemCategory);
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemCategory itemCategory;
        TextView itemCategoryName;
        ImageView itemCategoryImage;

        CategoryViewHolder(View v) {
            super(v);
            itemCategoryName = itemView.findViewById(R.id.layout_items_category_text);
            itemCategoryImage = itemView.findViewById(R.id.layout_items_category_image);
            v.setOnClickListener(this);
        }

        void bindModel(ItemCategory itemCategory) {
            this.itemCategory = itemCategory;
            itemCategoryName.setText(itemCategory.getCategoryName());
            itemCategoryImage.setBackgroundResource(itemCategory.getCategoryIconColor());
            itemCategoryImage.setImageResource(itemCategory.getCategoryIconId());
        }

        public void onClick(View view) {
            itemCategoryClickListener.onClick(view, itemCategory);
            //if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);
        }

    }

}
