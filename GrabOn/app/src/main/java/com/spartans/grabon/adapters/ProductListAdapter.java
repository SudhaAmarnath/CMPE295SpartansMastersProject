package com.spartans.grabon.adapters;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.spartans.grabon.R;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.model.ItemImage;
import com.spartans.grabon.model.ItemPrice;
import com.spartans.grabon.model.ItemSummary;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<ItemSummary> itemSummaries;
    private OnItemClickListener mListener;

    private static int GRAB_ON = 1;
    private static int EBAY = 2;
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
        public ImageView vendorImageView;

        public ViewHolder(View itemView, final OnItemClickListener onClickListener) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            vendorImageView = itemView.findViewById(R.id.vendor_view);
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

    public ProductListAdapter(List<ItemSummary> itemSummariesList, List<Item> grabOnItems) {
        if (grabOnItems != null)
        convertGrabOnToEbayFormat(itemSummariesList, grabOnItems);
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
    public int getItemViewType(int position) {
        if (position >= 15) {
            return GRAB_ON;
        } else {
            return EBAY;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (getItemViewType(position) == GRAB_ON) {
            Log.v("onBindViewHolder","onBindViewHolder called for grabon"+itemSummaries.get(position));
            ItemSummary currentItem = itemSummaries.get(position);
            if(currentItem!= null && currentItem.getImage()!= null && currentItem.getImage().getImageUrl()!= null) {
                Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160, 140).error(R.mipmap.ic_launcher).into(holder.imageView);
            }
            holder.textViewName.setText(currentItem.getTitle());
            holder.textViewPrice.setText(currentItem.getPrice().getValue() + " " + currentItem.getPrice().getCurrency());
            holder.vendorImageView.setImageResource(R.drawable.ic_launcher_grabon);
        } else {
            Log.v("onBindViewHolder","onBindViewHolder called for ebay"+itemSummaries.get(position));
            ItemSummary currentItem = itemSummaries.get(position);
            if(currentItem!= null && currentItem.getImage()!= null && currentItem.getImage().getImageUrl()!= null) {
                Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160,140).error(R.mipmap.ic_launcher).into(holder.imageView);
            }
            holder.textViewName.setText(currentItem.getTitle());
            holder.textViewPrice.setText(currentItem.getPrice().getValue() + " " + currentItem.getPrice().getCurrency());
            holder.vendorImageView.setImageResource(R.drawable.ic_launcher_ebay);
        }

    }

    @Override
    public int getItemCount() {
        return itemSummaries.size();
    }

    private void convertGrabOnToEbayFormat(List<ItemSummary> itemSummariesList, List<Item> grabOnItems) {
        Log.v("convert","convertGrabOnToEbayFormat called");
        for (Item item : grabOnItems) {
            ItemSummary itemSummary = new ItemSummary(item.getItemName(), new ItemImage(item.getItemImageList().get(0).toString()), new ItemPrice(String. valueOf(item.getItemPrice()),"USD"));
            itemSummariesList.add(itemSummary);
        }
    }
}
