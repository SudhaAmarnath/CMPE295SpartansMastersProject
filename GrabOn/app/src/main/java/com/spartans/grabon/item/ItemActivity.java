package com.spartans.grabon.item;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.tinydb.TinyDB;
import com.smarteist.autoimageslider.DefaultSliderView;
import com.smarteist.autoimageslider.SliderLayout;
import com.smarteist.autoimageslider.SliderView;
import com.spartans.grabon.R;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Iterator;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-03-09
 */
public class ItemActivity extends AppCompatActivity {

    private String itemID;
    private String itemSellerUID;
    private String itemName;
    private String itemDesc;
    private float  itemPrice;
    private String itemImage;
    private String itemAddress;
    private ArrayList itemImageList;
    private TextView viewItemName;
    private TextView viewItemDesc;
    private TextView viewItemPrice;
    private TextView sellerEmail;
    private TextView sellerPh;
    private TextView sellerAddress;
    private FancyButton viewAddToCart;
    private boolean cartClicked;
    private SliderLayout sliderLayout;
    private String TAG = "ITEM";
    private TinyDB tinyDB;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        tinyDB = new TinyDB(this);
        itemID = (String) getIntent().getSerializableExtra("itemid");
        itemSellerUID = (String) getIntent().getSerializableExtra("selleruid");
        itemName = (String) getIntent().getSerializableExtra("itemname");
        itemDesc = (String) getIntent().getSerializableExtra("itemdesc");
        itemPrice = (float) getIntent().getSerializableExtra("itemprice");
        itemImageList = (ArrayList) getIntent().getSerializableExtra("itemimagelist");
        itemAddress = (String) getIntent().getSerializableExtra("itemaddress");

        viewItemName = findViewById(R.id.ItemName);
        viewItemDesc = findViewById(R.id.ItemDescription);
        viewItemPrice = findViewById(R.id.ItemPrice);
        sellerEmail = findViewById(R.id.ItemSellerID);
        sellerPh = findViewById(R.id.ItemSellerPhone);
        sellerAddress = findViewById(R.id.ItemSellerAddress);
        viewAddToCart = findViewById(R.id.ItemToCart);
        sliderLayout = findViewById(R.id.ItemImage);
        sliderLayout.setScrollTimeInSec(5);

        viewItemName.setText(itemName);
        viewItemDesc.setText(itemDesc);
        viewItemPrice.setText("$"+ itemPrice);

        db.collection("users")
                .document(itemSellerUID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            sellerEmail.setText(task.getResult().get("email").toString());
                            sellerPh.setText(task.getResult().get("phone").toString());
                            sellerAddress.setText(task.getResult().get("address").toString());
                        }
                    }
                });

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
                item.setItemAddress(itemAddress);
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

    public void addItemToTinyDB(Item item, TinyDB tinyDB) {

        ArrayList<Object> savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
        // Add item as object to the tinyDB
        savedObjects.add((Object) item);
        tinyDB.putListObject(user.getUid(), savedObjects);
        Log.v("TinDB Add", "Item:" + item.getItemID() + " is added");

    }

    public void removeItemFromTinyDB(Item item, TinyDB tinyDB) {

        if (itemInTinyDB(item.getItemID(), tinyDB)) {
            ArrayList<Object> savedObjects;
            savedObjects = tinyDB.getListObject(user.getUid(), Item.class);
            Iterator itr = savedObjects.iterator();
            while (itr.hasNext()) {
                Item nextItem = (Item) itr.next();
                if (item.getItemID().equals(nextItem.getItemID())) {
                    itr.remove();
                    Log.v("TinDB Remove", "Item:" + item.getItemID() + " is removed");
                }
            }
            tinyDB.putListObject(user.getUid(), savedObjects);
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
