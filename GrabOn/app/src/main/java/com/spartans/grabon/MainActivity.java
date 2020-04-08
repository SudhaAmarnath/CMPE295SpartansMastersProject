package com.spartans.grabon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
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
import com.spartans.grabon.adapters.ProductListAdapter;
import com.spartans.grabon.cart.Cart;
import com.spartans.grabon.fragments.BottomSheetNavigationFragment;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.item.UpdateItem;
import com.spartans.grabon.model.ApplicationToken;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.model.ItemSummary;
import com.spartans.grabon.model.SearchResponse;
import com.spartans.grabon.services.EbayAPI;
import com.spartans.grabon.utils.DistanceCalculator;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kommunicate.KmConversationBuilder;
import io.kommunicate.KmConversationHelper;
import io.kommunicate.KmException;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;
import io.kommunicate.users.KMUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    private Toolbar toolbar;
    private RecyclerView recyclerViewItems;
//    private RecyclerView.adapter
    private RecyclerView.LayoutManager mLayoutManager;
    private FirebaseFirestore db;

    private ArrayList<Item> itemsList = new ArrayList<>();
    private ItemAdapter itemAdapter;
    ArrayAdapter<String> adapter;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton fabChat, fabCart;
    private FirebaseUser loggedinUser;
    private FirebaseAuth firebaseAuth;
    private String conversationId = "";
    private static final String APPLICATION_TOKEN_KEY = "application_token_key";
    private static final String signInURL = "https://auth.ebay.com/oauth2/authorize";
    private static final String clientID = "Thirumal-GrapOn-PRD-e69ec6afc-fc19e172";
    private static final String clientSecret = "PRD-69ec6afc372e-74d0-4674-8d9b-b933";
    private static final String redirectURI = "Thirumalai_Namb-Thirumal-GrapOn-fgiiylk";
    private static final String scope = "https://api.ebay.com/oauth/api_scope";

    private String applictionToken;
    List<ItemSummary> itemList;

    String base = clientID + ":" + clientSecret;
    final String basicAuthToken = "Basic " + Base64.encodeToString(base.getBytes(), Base64.NO_WRAP);
    public MainActivity() {
        itemList = new ArrayList<ItemSummary>();
    }

    private RecyclerView searchRecycleView;
    private ProductListAdapter searchRecycleViewAdapter;
    private RecyclerView.LayoutManager searchRecycleViewLayoutManager;

    private SearchView searchView;

    PlacesClient placesClient;
    private double userlat = 0;
    private double userlon = 0;
    private double usermiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpBottomAppBar();

        FirebaseApp.initializeApp(this);

        conversationId = "";
        toolbar = findViewById(R.id.toolbar);
        recyclerViewItems = findViewById(R.id.HomeActivityItemsList);
        db = Singleton.getDb();
        final AutocompleteSupportFragment address;
        address = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.MainActivityLocation);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);

        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(MainActivity.this, apiKey);
        }
        placesClient = Places.createClient(this);
        address.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        ));

        address.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.v("search", place.getLatLng().toString());
                Log.v("search", place.getAddress());
                Log.v("search", place.getName());
                final LatLng latLng = place.getLatLng();
                userlat = latLng.latitude;
                userlon = latLng.longitude;
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });

        Spinner distance = (Spinner) findViewById(R.id.MainActivityDistance);
        distance.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object miles = parent.getItemAtPosition(position);
                if (miles.toString().equals("mi")) {
                    usermiles =  10000;
                } else {
                    usermiles = Double.parseDouble(miles.toString());
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // show 2 items in grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);

        fabChat = findViewById(R.id.fabChat);
        fabCart = findViewById(R.id.fabCart);
        firebaseAuth = FirebaseAuth.getInstance ();
        loggedinUser = firebaseAuth.getCurrentUser();

        fabChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startChat();
            }
        });

        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), Cart.class));
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
                                String itemlat = (String) myMap.get("itemlatitude");
                                String itemlon = (String) myMap.get("itemlongitude");
                                String itemaddress = (String) myMap.get("itemaddress");
                                double lat = Double.parseDouble(itemlat);
                                double lon = Double.parseDouble(itemlon);
                                double distance = 0.0;
                                if (userlat != 0 && userlon != 0) {
                                    distance = new DistanceCalculator().distance(userlat, userlon,
                                            lat, lon, 'M');
                                }
                                if ((boolean) myMap.get("itemordered") == false
                                        && distance <= usermiles) {
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
                                    item.setItemID(document.getId());
                                    item.setItemSellerUID((String) myMap.get("selleruid"));
                                    item.setItemName((String) myMap.get("itemname"));
                                    item.setItemDescription((String) myMap.get("itemdesc"));
                                    Object priceFromDB = myMap.get("itemprice");
                                    if (priceFromDB.getClass() == Double.class) {
                                        price = (Double) myMap.get("itemprice");
                                    }
                                    else if (priceFromDB.getClass() == Long.class) {
                                        price = ((Long) myMap.get("itemprice")).doubleValue();
                                    }
                                    item.setItemPrice(price.floatValue());
                                    item.setItemImageList(imgs);
                                    item.setItemAddress(itemaddress);
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
                                updateItemPage.putExtra("itemaddress", item.getItemAddress());
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
                                itemPage.putExtra("itemaddress", item.getItemAddress());
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
        final MenuItem item = menu.findItem(R.id.menuSearch);
        searchView = (SearchView) item.getActionView();
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                recyclerViewItems.setVisibility(View.GONE);
                getSearchResultFromEbay(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i("newText ",newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                recyclerViewItems.setVisibility(View.VISIBLE);
                searchRecycleView.setVisibility(View.GONE);
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

        if(conversationId == "") {
            new KmConversationBuilder(MainActivity.this)
                    .setKmUser(user)
                    .setSingleConversation(false)
                    .launchConversation(new KmCallback() {
                        @Override
                        public void onSuccess(Object message) {
                            Log.d("Conversation", "Success : " + message);
                            conversationId = message.toString();
                            Map<String, String> metadata = new HashMap<>();
                            metadata.put("email", loggedinUser.getEmail());
                            metadata.put("uID", loggedinUser.getUid());

                            if (Kommunicate.isLoggedIn(MainActivity.this)) { // Pass application context
                                Kommunicate.updateChatContext(MainActivity.this, metadata);
                            }
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.d("Conversation", "Failure : " + error);
                        }
                    });
        } else {
            try {
                KmConversationHelper.openConversation(MainActivity.this,
                        true,
                        Integer.parseInt(conversationId),
                        new KmCallback() {
                            @Override
                            public void onSuccess(Object message) {
                                Map<String, String> metadata = new HashMap<>();
                                metadata.put("email", loggedinUser.getEmail());
                                metadata.put("uID", loggedinUser.getUid());

                                if (Kommunicate.isLoggedIn(MainActivity.this)) { // Pass application context
                                    Kommunicate.updateChatContext(MainActivity.this, metadata);
                                }
                            }

                            @Override
                            public void onFailure(Object error) {

                            }
                        });
            } catch (KmException e) {
                e.printStackTrace();
            }
        }
    }

    private List<ItemSummary> getSearchResultFromEbay(final String query) {
        Call<ApplicationToken> call = EbayAPI.getClientTokenService().getApplicationToken(basicAuthToken, "client_credentials",
                EbayAPI.RuName, EbayAPI.APPLICATION_ACCESS_TOKEN_SCOPE);
        call.enqueue(new Callback<ApplicationToken>() {
            @Override
            public void onResponse(Call<ApplicationToken> call, Response<ApplicationToken> response) {
                applictionToken = response.body().getAccess_token();
                String authorization = "Bearer " + applictionToken;
                Call<SearchResponse> callComputer = EbayAPI.getService().getSearchResult(authorization, query, 15);
                callComputer.enqueue(new Callback<SearchResponse>() {
                    @Override
                    public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                        SearchResponse searchResponse = response.body();
                        itemList = searchResponse.getItemSummaries();
                        searchRecycleView = findViewById(R.id.searchRecycleView);
                        searchRecycleView.setVisibility(View.VISIBLE);
                        searchRecycleView.setHasFixedSize(true);
                        searchRecycleViewLayoutManager = new LinearLayoutManager(getApplicationContext());
                        searchRecycleViewAdapter = new ProductListAdapter(itemList);
                        searchRecycleView.setLayoutManager(searchRecycleViewLayoutManager);
                        searchRecycleView.setAdapter(searchRecycleViewAdapter);
                        searchRecycleViewAdapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {
                            @Override
                            public void OnItemClickListener(int position) {
                                itemList.get(position);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                browserIntent.setData(Uri.parse(itemList.get(position).getItemWebUrl()));
                                startActivity(new Intent(browserIntent));
                            }
                        });
                    }
                    @Override
                    public void onFailure(Call<SearchResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "call fail", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Call<ApplicationToken> call, Throwable t) {
                Toast.makeText(MainActivity.this, "fail", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "fail: " + t.getCause().getLocalizedMessage());
            }
        });
        return itemList;
    }

}
