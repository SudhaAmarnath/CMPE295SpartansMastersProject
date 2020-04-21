package com.spartans.grabon.item;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.PostedItemAdapter;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Map;

/**
 * Author : Sudha Amarnath on 2020-03-30
 */
public class PostedItems extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private ArrayList<Item> postedItemsList = new ArrayList<>();
    private PostedItemAdapter postedItemAdapter;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore db = Singleton.getDb();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posted_items);

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        recyclerViewItems = findViewById(R.id.PostedItemsList);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);

        displayItems();

    }

    private void getItems(final FileDataStatus fileDataStatus) {

        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (user.getUid().matches(document.get("selleruid").toString())) {
                                    ArrayList<String> imgs = new ArrayList<>();
                                    Map<String, Object> myMap = document.getData();
                                    for (Map.Entry<String, Object> entry : myMap.entrySet()) {
                                        if (entry.getKey().equals("itemimagelist")) {
                                            for (Object s : (ArrayList) entry.getValue()) {
                                                imgs.add((String) s);
                                            }
                                            Log.v("TagImg", entry.getValue().toString());
                                        }
                                    }

                                    Item item = new Item();
                                    Double price = 0.0;
                                    Object priceFromDB = myMap.get("itemprice");
                                    if (priceFromDB.getClass() == Double.class) {
                                        price = (Double) myMap.get("itemprice");
                                    }
                                    else if (priceFromDB.getClass() == Long.class) {
                                        price = ((Long) myMap.get("itemprice")).doubleValue();
                                    }
                                    item.setItemID(document.getId());
                                    item.setItemSellerUID((String) myMap.get("selleruid"));
                                    item.setItemName((String) myMap.get("itemname"));
                                    item.setItemDescription((String) myMap.get("itemdesc"));
                                    item.setItemPrice(price.floatValue());
                                    item.setItemImageList(imgs);
                                    item.setItemCategory((String) myMap.get("itemcategory"));
                                    postedItemsList.add(item);
                                }
                            }
                            fileDataStatus.onSuccess(postedItemsList);
                        } else {
                            fileDataStatus.onError("Error getting data");
                            Log.w("Posted Items:", "Error getting data.", task.getException());
                        }

                    }
                });
    }

    private void displayItems () {

        getItems(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (postedItemAdapter == null) {
                    postedItemAdapter = new PostedItemAdapter(postedItemsList, PostedItems.this, new ClickListenerItem() {
                        @Override
                        public void onClick(View view, Item item) {
                            Intent updateItemPage = new Intent(PostedItems.this, UpdateItem.class);
                            updateItemPage.putExtra("itemid", item.getItemID());
                            updateItemPage.putExtra("selleruid",item.getItemSellerUID());
                            updateItemPage.putExtra("itemname",item.getItemName());
                            updateItemPage.putExtra("itemdesc", item.getItemDescription());
                            updateItemPage.putExtra("itemprice", item.getItemPrice());
                            updateItemPage.putExtra("itemimage", item.getItemImage());
                            updateItemPage.putExtra("itemimagelist", item.getItemImageList());
                            updateItemPage.putExtra("itemcategory", item.getItemCategory());
                            startActivity(updateItemPage);
                        }
                    });
                    recyclerViewItems.setAdapter(postedItemAdapter);
                } else {
                    postedItemAdapter.getItems().clear();
                    postedItemAdapter.getItems().addAll(postedItemsList);
                    postedItemAdapter.notifyDataSetChanged();
                }

                if (list.size() > 0) {
                    findViewById(R.id.NoPostedItems).setVisibility(View.INVISIBLE);
                } else {
                    findViewById(R.id.NoPostedItems).setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String e) {

            }
        });

    }
}
