package com.spartans.grabon.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.spartans.grabon.R;
import com.spartans.grabon.model.ItemSummary;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<ItemSummary> itemSummaries;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void OnItemClickListener(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewName;
        public TextView textViewPrice;
        public ImageView ebayImageView;

        public ViewHolder(View itemView, final OnItemClickListener onClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            ebayImageView = itemView.findViewById(R.id.ebay_view);
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (onClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            onClickListener.OnItemClickListener(position);
                        }
                    }
                }
            });
        }
    }

    public ProductListAdapter(List<ItemSummary> itemSummariesList) {
        itemSummaries = itemSummariesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate((R.layout.item_layout), parent, false);
        ViewHolder viewHolder = new ViewHolder(v, mListener);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemSummary currentItem = itemSummaries.get(position);
        Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160,140).error(R.mipmap.ic_launcher).into(holder.imageView);
        holder.textViewName.setText(currentItem.getTitle());
        holder.textViewPrice.setText(currentItem.getPrice().getValue() + " " + currentItem.getPrice().getCurrency());
        holder.ebayImageView.setImageResource(R.drawable.ic_launcher);

    }

    @Override
    public int getItemCount() {
        return itemSummaries.size();
    }
}
