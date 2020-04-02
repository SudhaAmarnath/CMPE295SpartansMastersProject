package com.spartans.grabon.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.spartans.grabon.R;
import com.spartans.grabon.model.ItemSummary;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

    private ArrayList<ItemSummary>  itemSummaryList;
    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView title;
        public TextView price;

        SearchViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.layout_items_image);
            title = itemView.findViewById(R.id.layout_items_text);
            price = itemView.findViewById(R.id.layout_items_price);
        }
    }


    public SearchAdapter(ArrayList<ItemSummary> itemSummaryList){
        this.itemSummaryList = itemSummaryList;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View searchView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_items_list, parent, false);
        SearchViewHolder sva = new SearchViewHolder(searchView);
        return sva;
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            ItemSummary currentitemSummary = itemSummaryList.get(position);

            Picasso.get().load(currentitemSummary.getImage().getImageUrl()).resize(160,140).error(R.mipmap.ic_launcher).into(holder.imageView);
            holder.title.setText(currentitemSummary.getTitle());
            holder.price.setText(currentitemSummary.getPrice().getCurrency()+currentitemSummary.getPrice().getValue());
    }

    @Override
    public int getItemCount() {

        return itemSummaryList.size();
    }
}
