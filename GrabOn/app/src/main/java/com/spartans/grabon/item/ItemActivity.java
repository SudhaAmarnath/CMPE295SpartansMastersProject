package com.spartans.grabon.item;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.tinydb.TinyDB;
import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;
import com.spartans.grabon.R;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;

import mehdi.sakout.fancybuttons.FancyButton;

public class ItemActivity extends AppCompatActivity {

    private String itemID;
    private String itemSellerUID;
    private String itemName;
    private String itemDesc;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;
    private TextView viewItemName;
    private TextView viewItemDesc;
    private TextView viewItemPrice;
    private FancyButton viewAddToCart;
    private boolean cartClicked;
    private SliderLayout sliderLayout;
    private String TAG = "ITEM";
    private TinyDB tinyDB;

    private FirebaseUser user = Singleton.getUser();
    private FirebaseFirestore db = Singleton.getDb();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        tinyDB = new TinyDB(this);
        itemID = (String) getIntent().getSerializableExtra("itemid");
        itemSellerUID = (String) getIntent().getSerializableExtra("selleruid");
        itemName = (String) getIntent().getSerializableExtra("itemname");
        itemDesc = (String) getIntent().getSerializableExtra("itemdesc");
        itemPrice = (float) getIntent().getSerializableExtra("itemprice");
        itemImageList = (ArrayList) getIntent().getSerializableExtra("itemimagelist");

        viewItemName = findViewById(R.id.ItemName);
        viewItemDesc = findViewById(R.id.ItemDescription);
        viewItemPrice = findViewById(R.id.ItemPrice);
        viewAddToCart = findViewById(R.id.ItemToCart);
        sliderLayout = findViewById(R.id.ItemImage);
        sliderLayout.setScrollTimeInSec(5);

        viewItemName.setText(itemName);
        viewItemDesc.setText(itemDesc);
        viewItemPrice.setText("$"+ itemPrice);

        if (itemInTinyDB(itemID, tinyDB)) {
            cartClicked = true;
            viewAddToCart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            cartClicked = false;
            viewAddToCart.setBackgroundColor(getResources().getColor(R.color.black));
        }

        viewAddToCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = new Item(itemID, itemName, itemDesc, itemSellerUID, itemPrice, itemImageList);
                if(cartClicked == false) {
                    addItemToTinyDB(item, tinyDB);
                    viewAddToCart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    cartClicked = true;
                    Toast.makeText(ItemActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                } else {
                    removeItemFromTinyDB(item, tinyDB);
                    viewAddToCart.setBackgroundColor(getResources().getColor(R.color.black));
                    cartClicked = false;
                    Toast.makeText(ItemActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        slideImageViews();

    }

    public void addItemToTinyDBOld(ArrayList<Item> items, TinyDB tinyDB) {

        items = new ArrayList<Item>();
        ArrayList<Object> itemObjects = new ArrayList<Object>();

        for(Item a : items){
            itemObjects.add((Object)a);
        }

        tinyDB = new TinyDB(getApplicationContext());
        tinyDB.putListObject(user.getUid(), itemObjects);

    }

    public void addItemToTinyDB(Item item, TinyDB tinyDB) {

        ArrayList<Object> objects = new ArrayList<Object>();
        objects.add((Object)item);
        tinyDB = new TinyDB(getApplicationContext());
        tinyDB.putListObject(user.getUid(), objects);

    }

    public void removeItemFromTinyDB(Item item, TinyDB tinyDB) {

        ArrayList<Object> objects = new ArrayList<Object>();
        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        if (itemInTinyDB(item.getItemID(), tinyDB)) {
            savedObjects.remove(objects.add((Object)item));
        }

    }

    public boolean itemInTinyDB(String itemID, TinyDB tinyDB) {

        ArrayList<Object> savedObjects;
        savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        ArrayList<Item> items = new ArrayList<Item>();
        for(Object objs : savedObjects){
            items.add((Item) objs);
        }
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItemID().equalsIgnoreCase(itemID)) {
                return true;
            }
        }

        return false;

    }

    private void slideImageViews() {

        for (int i = 0; i < itemImageList.size(); i++ ) {

            DefaultSliderView defaultSliderView = new DefaultSliderView(this);

            defaultSliderView.setImageUrl(itemImageList.get(i).toString());
            defaultSliderView.setImageScaleType(ImageView.ScaleType.CENTER_INSIDE);

            defaultSliderView.setOnSliderClickListener(new SliderView.OnSliderClickListener() {
                @Override
                public void onSliderClick(SliderView sliderView) {

                }
            });

            sliderLayout.addSliderView(defaultSliderView);

        }


    }


}
