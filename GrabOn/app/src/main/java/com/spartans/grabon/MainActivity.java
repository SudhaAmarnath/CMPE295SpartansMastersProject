package com.spartans.grabon;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.adapters.ItemAdapter;
import com.spartans.grabon.fragments.BottomSheetNavigationFragment;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.item.UpdateItem;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Map;

import io.kommunicate.KmConversationBuilder;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private Toolbar toolbar;
    private RecyclerView recyclerViewItems;
    private FirebaseFirestore db;
    private ArrayList<Item> itemsList = new ArrayList<>();
    private ItemAdapter itemAdapter;
    ArrayAdapter<String> adapter;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabChat;
    private FirebaseUser loggedinUser;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpBottomAppBar();

        FirebaseApp.initializeApp(this);

        toolbar = findViewById(R.id.toolbar);
        recyclerViewItems = findViewById(R.id.HomeActivityItemsList);
        db = Singleton.getDb();

        setSupportActionBar(toolbar);

        // show 2 items in grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);

        fabChat = findViewById(R.id.fabChat);
        firebaseAuth = FirebaseAuth.getInstance();
        loggedinUser = firebaseAuth.getCurrentUser();

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChat();
            }
        });

        displayItems();

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.MainItemPullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                itemAdapter = null;
                itemsList = new ArrayList<>();
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                recyclerViewItems.setLayoutManager(gridLayoutManager);
                recyclerViewItems.setNestedScrollingEnabled(false);
                pullToRefresh.setRefreshing(false);
                displayItems();
            }
        });

    }

    private void getItems(final FileDataStatus fileDataStatus) {

        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> imgs = new ArrayList<>();
                                Map<String, Object> myMap = document.getData();
                                if ((boolean) myMap.get("itemordered") == false) {
                                    for (Map.Entry<String, Object> entry : myMap.entrySet()) {
                                        if (entry.getKey().equals("itemimagelist")) {
                                            for (Object s : (ArrayList) entry.getValue()) {
                                                imgs.add((String) s);
                                            }
                                            Log.v("TagImg", entry.getValue().toString());

                                        }
                                    }
                                    Item item = new Item();
                                    Double price;
                                    item.setItemID(document.getId());
                                    item.setItemSellerUID((String) myMap.get("selleruid"));
                                    item.setItemName((String) myMap.get("itemname"));
                                    item.setItemDescription((String) myMap.get("itemdesc"));
                                    price = (Double) myMap.get("itemprice");
                                    item.setItemPrice(price.floatValue());
                                    item.setItemImageList(imgs);
                                    itemsList.add(item);
                                }
                            }
                            fileDataStatus.onSuccess(itemsList);
                        } else {
                            fileDataStatus.onError("Error getting data");
                            Log.w(TAG, "Error getting data.", task.getException());
                        }

                    }
                });
    }

    private void displayItems () {

        getItems(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {
                if (itemAdapter == null) {
                    itemAdapter = new ItemAdapter(itemsList, MainActivity.this, new ClickListenerItem() {
                        @Override
                        public void onClick(View view, Item item) {
                            if (loggedinUser.getUid().matches(item.getItemSellerUID())) {
                                Intent updateItemPage = new Intent(MainActivity.this, UpdateItem.class);
                                updateItemPage.putExtra("itemid", item.getItemID());
                                updateItemPage.putExtra("selleruid",item.getItemSellerUID());
                                updateItemPage.putExtra("itemname",item.getItemName());
                                updateItemPage.putExtra("itemdesc", item.getItemDescription());
                                updateItemPage.putExtra("itemprice", item.getItemPrice());
                                updateItemPage.putExtra("itemimage", item.getItemImage());
                                updateItemPage.putExtra("itemimagelist", item.getItemImageList());
                                startActivity(updateItemPage);
                            } else {
                                Intent itemPage = new Intent(MainActivity.this, ItemActivity.class);
                                itemPage.putExtra("itemid", item.getItemID());
                                itemPage.putExtra("selleruid",item.getItemSellerUID());
                                itemPage.putExtra("itemname",item.getItemName());
                                itemPage.putExtra("itemdesc", item.getItemDescription());
                                itemPage.putExtra("itemprice", item.getItemPrice());
                                itemPage.putExtra("itemimage", item.getItemImage());
                                itemPage.putExtra("itemimagelist", item.getItemImageList());
                                startActivity(itemPage);
                            }
                        }
                    });
                    recyclerViewItems.setAdapter(itemAdapter);
                } else {
                    itemAdapter.getItems().clear();
                    itemAdapter.getItems().addAll(itemsList);
                    itemAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String e) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menuSearch);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Toast.makeText(MainActivity.this,"Searching for "+newText, Toast.LENGTH_SHORT).show();
                Log.i("newText ",newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * set up Bottom Bar
     */
    private void setUpBottomAppBar() {
        //find id
        bottomAppBar = findViewById(R.id.bar);

        //set bottom bar to Action bar as it is similar like Toolbar
        setSupportActionBar(bottomAppBar);

        //click event over navigation menu like back arrow or hamburger icon
        bottomAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open bottom sheet
                BottomSheetDialogFragment bottomSheetDialogFragment = BottomSheetNavigationFragment.newInstance();
                bottomSheetDialogFragment.show(getSupportFragmentManager(), "Bottom Sheet Dialog Fragment");
            }
        });
    }

    // GarbOn Chatbot
    private void startChat() {

        Kommunicate.init(getApplicationContext(), "12758b23184872aad1fafe101f6efc98b");
        KMUser user = new KMUser();
        user.setUserId(loggedinUser.getEmail());

        new KmConversationBuilder(getApplicationContext())
                .setKmUser(user)
                .launchConversation(new KmCallback() {
                    @Override
                    public void onSuccess(Object message) {
                        Log.d("Conversation", "Success : " + message);
                    }

                    @Override
                    public void onFailure(Object error) {
                        Log.d("Conversation", "Failure : " + error);
                    }
                });
    }

}
