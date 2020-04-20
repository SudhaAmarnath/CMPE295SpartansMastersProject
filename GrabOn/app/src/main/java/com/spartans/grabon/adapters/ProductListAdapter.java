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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.ViewHolder> {

    private List<ItemSummary> itemSummaries;
    private OnItemClickListener mListener;

    public static int GRAB_ON = 1;
    public static int EBAY = 0;
    public static  int CRAIGSLIST = 2;
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

    public ProductListAdapter(List<ItemSummary> itemSummariesList, List<Item> grabOnItems, NodeList craigslistItems) {
        if (grabOnItems != null)
            convertGrabOnToEbayFormat(itemSummariesList, grabOnItems);

        if (craigslistItems != null)
            convertCraigslistToEbayFormat(itemSummariesList, craigslistItems);

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
        if (currentItem != null) {
            if (currentItem.getVendorID() == GRAB_ON) {
                Log.v("onBindViewHolder", "onBindViewHolder called for grabon" + itemSummaries.get(position));
                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage().getImageUrl() != null) {
                    Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160, 140).error(R.mipmap.ic_launcher).into(holder.imageView);
                }
                holder.textViewName.setText(currentItem.getTitle());
                holder.textViewPrice.setText(currentItem.getPrice().getValue() + " " + currentItem.getPrice().getCurrency());
                holder.vendorImageView.setImageResource(R.drawable.ic_launcher_grabon);
            } else if (currentItem.getVendorID() == EBAY) {
                Log.v("onBindViewHolder", "onBindViewHolder called for ebay" + itemSummaries.get(position));

                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage().getImageUrl() != null) {
                    Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160, 140).error(R.mipmap.ic_launcher).into(holder.imageView);
                }
                holder.textViewName.setText(currentItem.getTitle());
                holder.textViewPrice.setVisibility(View.VISIBLE);
                holder.textViewPrice.setText(currentItem.getPrice().getValue() + " " + currentItem.getPrice().getCurrency());
                holder.vendorImageView.setImageResource(R.drawable.ic_launcher_ebay);
            } else if (currentItem.getVendorID() == CRAIGSLIST) {
                Log.v("onBindViewHolder", "onBindViewHolder called for Craigslist" + itemSummaries.get(position));
                if (currentItem != null && currentItem.getImage() != null && currentItem.getImage().getImageUrl() != null) {
                    Picasso.get().load(currentItem.getImage().getImageUrl()).resize(160, 140).error(R.mipmap.ic_launcher).into(holder.imageView);
                }
                holder.textViewName.setText(currentItem.getTitle());
                holder.textViewPrice.setVisibility(View.INVISIBLE);
                holder.vendorImageView.setImageResource(R.drawable.craigslist_logo);
            }
        }
    }

    @Override
    public int getItemCount() {
        return itemSummaries.size();
    }

    private void convertGrabOnToEbayFormat(List<ItemSummary> itemSummariesList, List<Item> grabOnItems) {
        Log.v("convert","convertGrabOnToEbayFormat called");
        for (Item item : grabOnItems) {
            ItemSummary itemSummary = new ItemSummary(GRAB_ON, item.getItemName(), new ItemImage(item.getItemImageList().get(0).toString()),
                    new ItemPrice(String. valueOf(item.getItemPrice()),"USD"), item.getItemID());
            itemSummariesList.add(itemSummary);
        }
    }

    private void convertCraigslistToEbayFormat(List<ItemSummary> itemSummariesList, NodeList nodeList) {
        Log.v("convert","convertCraigslistToEbayFormat called");

        int numberOfItems = nodeList.getLength() > 15 ? 15 : nodeList.getLength();
        for (int i = 0; i < numberOfItems; i++) {

            Node node = nodeList.item(i);

            Element itemElement = (Element) node;
            String itemName = "", itemLink = "", itemImage = "";

            Node itemNameNode = itemElement.getElementsByTagName("title").item(0);
            if (itemNameNode != null) {
                itemName = ((Element) itemNameNode).getTextContent();
                itemName = itemName.replace("&#x0024;", "$");
            }

            Node itemLinkNode = itemElement.getElementsByTagName("link").item(0);
            if (itemLinkNode != null) {
                itemLink = ((Element) itemLinkNode).getTextContent();
            }

            Node itemImageNode = itemElement.getElementsByTagName("enc:enclosure").item(0);
            if (itemImageNode != null) {
                itemImage = ((Element) itemImageNode).getAttribute("resource");
            }

            ItemSummary itemSummary = null;
            if (itemImage != null && itemImage != "") {
                itemSummary = new ItemSummary(CRAIGSLIST, itemName, new ItemImage(itemImage), itemLink);
            } else {
                itemSummary = new ItemSummary(CRAIGSLIST, itemName, itemLink);
            }

            if (itemSummary != null) {
                itemSummariesList.add(itemSummary);
            }
        }
    }
}
