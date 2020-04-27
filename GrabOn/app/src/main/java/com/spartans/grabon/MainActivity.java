package com.spartans.grabon;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.spartans.grabon.adapters.ItemAdapter;
import com.spartans.grabon.adapters.ItemCategoryAdapter;
import com.spartans.grabon.adapters.ProductListAdapter;
import com.spartans.grabon.cart.Cart;
import com.spartans.grabon.fragments.BottomSheetNavigationFragment;
import com.spartans.grabon.interfaces.ClickListenerItem;
import com.spartans.grabon.interfaces.ClickListenerItemCategory;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.Categories;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.model.ApplicationToken;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.model.ItemCategory;
import com.spartans.grabon.model.ItemSummary;
import com.spartans.grabon.model.SearchResponse;
import com.spartans.grabon.services.EbayAPI;
import com.spartans.grabon.utils.DistanceCalculator;
import com.spartans.grabon.utils.RetrieveFeedTask;
import com.spartans.grabon.utils.Singleton;

import org.w3c.dom.NodeList;

import java.util.ArrayList;
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
public class MainActivity extends AppCompatActivity implements RetrieveFeedTask.AsyncResponse {

    public static final String TAG = "TAG";
    private Toolbar toolbar;
    private RecyclerView recyclerViewItems;
    private RecyclerView recyclerViewItemCategories;
    private SwipeRefreshLayout pullToRefresh;
    private FirebaseFirestore db;
    private ProgressBar progressBar;

    private ArrayList<Item> itemsList = new ArrayList<>();
    private ItemAdapter itemAdapter;
    private ArrayList<ItemCategory> itemCategoriesList = new ArrayList<>();
    private ItemCategoryAdapter itemCategoryAdapter;
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

    private static String userZipcode = "";
    private static String userCity = "";
    private static String userAddress = "";
    private static String userLatitude = "";
    private static String userLongitude = "";
    private static String currentUserLat = "";
    private static String currentUserLon = "";
    private static String currentUserZipcode = "";
    private static String currentUserCity = "";

    private static int distance = 25;
    private static int priceMin=0;
    private static int priceMax=2000;
    private static boolean grabon = true;
    private static boolean craigslist = true;
    private static boolean ebay = true;
    private static int numberItems = 15;

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

    private NodeList craigslistItemsList;

    PlacesClient placesClient;
    private double userlat = 0;
    private double userlon = 0;
    private double usermiles = 25;

    public static String category = "";
    /*
     *   Appliance = 20710
     *   Automobiles = 6024
     *   Electronics = 58058
     *   Fashion = 1059
     *   Furniture = 3197
     *   Home Garden = 159912
     *   Movies & Music = 11232
     *   Office = 58271
     *   Sports = 20863
     *   Toys & Games= 2540
     */
    private static Map<String, String> CategoryKeywordToCategoryIdMap = new HashMap<>();
    static {
        CategoryKeywordToCategoryIdMap.put("Appliances", "20710");
        CategoryKeywordToCategoryIdMap.put("Automobiles", "6024");
        CategoryKeywordToCategoryIdMap.put("Electronics", "58058");
        CategoryKeywordToCategoryIdMap.put("Fashion", "1059");
        CategoryKeywordToCategoryIdMap.put("Furniture", "3197");
        CategoryKeywordToCategoryIdMap.put("Home & Garden", "159912");
        CategoryKeywordToCategoryIdMap.put("Movies & Music", "11232");
        CategoryKeywordToCategoryIdMap.put("Office", "58271");
        CategoryKeywordToCategoryIdMap.put("Sports", "20863");
        CategoryKeywordToCategoryIdMap.put("Toys & Games", "2540");
        CategoryKeywordToCategoryIdMap.put("Other","172008");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpBottomAppBar();

        FirebaseApp.initializeApp(this);

        conversationId = "";
        toolbar = findViewById(R.id.toolbar);
        progressBar = (ProgressBar) findViewById(R.id.searchProgressBar);
        progressBar.setVisibility(View.GONE);
        recyclerViewItems = findViewById(R.id.HomeActivityItemsList);
        recyclerViewItemCategories = findViewById(R.id.HomeActivityItemCategoriesList);
        db = Singleton.getDb();

        firebaseAuth = FirebaseAuth.getInstance ();
        loggedinUser = firebaseAuth.getCurrentUser();

        if (loggedinUser == null) {
            Log.e("Login", "Logged in user is null");
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);

        final LinearLayout cityDistance = findViewById(R.id.LinearLayout1);
        final TextView locationCity = findViewById(R.id.MainActivityLocation);
        final TextView locationDistance = findViewById(R.id.MainActivityDistance);

        getPreferencesFromDb();

        Log.v("run", "currentUserCity:" + currentUserCity + "userCity:" + userCity);

        if (userCity.equals("") == false) {
            cityDistance.setVisibility(View.VISIBLE);
            locationCity.setText(userCity);
            locationDistance.setText(String.valueOf(distance) + " Miles");
            if (!userLatitude.equals("")) {
                userlat = Double.parseDouble(userLatitude);
            }
            if(!userLongitude.equals("")) {
                userlon = Double.parseDouble(userLongitude);
            }
            usermiles = distance;
        } else {
            if (currentUserCity.equals("") == false) {
                cityDistance.setVisibility(View.VISIBLE);
                locationCity.setText(currentUserCity);
                locationDistance.setText(String.valueOf(distance) + " Miles");
                if (!userLatitude.equals("")) {
                    userlat = Double.parseDouble(userLatitude);
                }
                if(!userLongitude.equals("")) {
                    userlon = Double.parseDouble(userLongitude);
                }
                usermiles = distance;
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.v("run", "currentUserCity:" + currentUserCity + "userCity:" + userCity);
                if (userCity.equals("")) {
                    if (currentUserCity.equals("") == false) {
                        cityDistance.setVisibility(View.VISIBLE);
                        locationCity.setText(currentUserCity);
                        locationDistance.setText(String.valueOf(distance) + " Miles");
                        if (!userLatitude.equals("")) {
                            userlat = Double.parseDouble(userLatitude);
                        }
                        if(!userLongitude.equals("")) {
                            userlon = Double.parseDouble(userLongitude);
                        }
                        usermiles = distance;
                        refreshItems();
                    }
                } else {
                    cityDistance.setVisibility(View.VISIBLE);
                    locationCity.setText(userCity);
                    locationDistance.setText(String.valueOf(distance) + " Miles");
                    if (!userLatitude.equals("")) {
                        userlat = Double.parseDouble(userLatitude);
                    }
                    if(!userLongitude.equals("")) {
                        userlon = Double.parseDouble(userLongitude);
                    }
                    usermiles = distance;
                    refreshItems();
                }
            }
        }, 4000);

        //Context context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,false);
        recyclerViewItemCategories.setLayoutManager(linearLayoutManager);
        recyclerViewItemCategories.setHasFixedSize(true);
        displayCategories();

        // show 2 items in grid layout
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);
        displayItems();

        pullToRefresh = findViewById(R.id.MainItemPullToRefresh);
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

        fabChat = findViewById(R.id.fabChat);
        fabCart = findViewById(R.id.fabCart);

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

        searchRecycleView = findViewById(R.id.searchRecycleView);


    }

    private void displayCategories () {

        itemCategoriesList = new ArrayList<>();
        ArrayList<String> catlist = new Categories().getItemCategoryList();
        for (int i = 0; i < catlist.size(); i++) {
            String catname = catlist.get(i);
            ArrayList<String> catvalues = new Categories().getItemCategoryResource(catname);
            int drawimg = getResources().getIdentifier(catvalues.get(0), "drawable", "com.spartans.grabon");
            int colres = getResources().getIdentifier(catvalues.get(1), "color", "com.spartans.grabon");
            ItemCategory ic = new ItemCategory(catname, drawimg, colres);
            itemCategoriesList.add(ic);
        }
        if (itemCategoryAdapter == null) {
            itemCategoryAdapter = new ItemCategoryAdapter(itemCategoriesList, MainActivity.this, new ClickListenerItemCategory() {
                @Override
                public void onClick(View view, ItemCategory itemCategory) {
                    if (category == itemCategory.getCategoryName()) {
                        Log.v("category", "unselect:" + category);
                        category = "";
                        searchRecycleView.setVisibility(View.GONE);
                        recyclerViewItems.setVisibility(View.VISIBLE);
                    } else {
                        category = itemCategory.getCategoryName();
                        Log.v("category", "select:" + category);
                        recyclerViewItems.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        getSearchResultFromThirdParty( category, false);
                    }

                    itemAdapter = null;
                    itemsList = new ArrayList<>();
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
                    recyclerViewItems.setLayoutManager(gridLayoutManager);
                    recyclerViewItems.setNestedScrollingEnabled(false);
                    displayItems();

                }
            });
            recyclerViewItemCategories.setAdapter(itemCategoryAdapter);
        } else {
            itemCategoryAdapter.getItemCategories().clear();
            itemCategoryAdapter.getItemCategories().addAll(itemCategoriesList);
            itemCategoryAdapter.notifyDataSetChanged();
        }
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
                                String itemcategory = (String) myMap.get("itemcategory");
                                boolean itemordered = (boolean) myMap.get("itemordered");

                                double lat = Double.parseDouble(itemlat);
                                double lon = Double.parseDouble(itemlon);
                                double curdistance = 0.0;
                                if (userlat != 0 && userlon != 0) {
                                    curdistance = new DistanceCalculator().distance(userlat, userlon,
                                            lat, lon, 'M');
                                }
                                Double price = 0.0;
                                Object priceFromDB = myMap.get("itemprice");
                                if (priceFromDB.getClass() == Double.class) {
                                    price = (Double) myMap.get("itemprice");
                                }
                                else if (priceFromDB.getClass() == Long.class) {
                                    price = ((Long) myMap.get("itemprice")).doubleValue();
                                }
                                if (!(boolean) myMap.get("itemordered")
                                        && curdistance <= usermiles
                                        && (category.equals("") || category.equals(itemcategory) || (category.equals("Freebies") && price == 0))) {

                                    for (Map.Entry<String, Object> entry : myMap.entrySet()) {
                                        if (entry.getKey().equals("itemimagelist")) {
                                            for (Object s : (ArrayList) entry.getValue()) {
                                                imgs.add((String) s);
                                            }
                                            Log.v("TagImg", entry.getValue().toString());
                                        }
                                    }
                                    Item item = new Item();
                                    item.setItemID(document.getId());
                                    item.setItemSellerUID((String) myMap.get("selleruid"));
                                    item.setItemName((String) myMap.get("itemname"));
                                    item.setItemDescription((String) myMap.get("itemdesc"));
                                    item.setItemPrice(price.floatValue());
                                    item.setItemImageList(imgs);
                                    item.setItemAddress(itemaddress);
                                    item.setItemCategory(itemcategory);
                                    item.setItemOrdered(itemordered);
                                    item.setLatitude(itemlat);
                                    item.setLongitude(itemlon);
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
                            Intent itemPage = new Intent(MainActivity.this, ItemActivity.class);
                            itemPage.putExtra("itemid", item.getItemID());
                            itemPage.putExtra("selleruid",item.getItemSellerUID());
                            itemPage.putExtra("itemname",item.getItemName());
                            itemPage.putExtra("itemdesc", item.getItemDescription());
                            itemPage.putExtra("itemprice", item.getItemPrice());
                            itemPage.putExtra("itemimage", item.getItemImage());
                            itemPage.putExtra("itemimagelist", item.getItemImageList());
                            itemPage.putExtra("itemaddress", item.getItemAddress());
                            itemPage.putExtra("itemcategory", item.getItemCategory());
                            itemPage.putExtra("itemordered", item.isItemOrdered());
                            itemPage.putExtra("itemlatitude", item.getLatitude());
                            itemPage.putExtra("itemlongitude", item.getLongitude());
                            startActivity(itemPage);
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
                pullToRefresh.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                getSearchResultFromThirdParty(query, true);
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
                if (searchRecycleView != null)
                searchRecycleView.setVisibility(View.GONE);
                pullToRefresh.setEnabled(true);
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

    private List<ItemSummary> getSearchResultFromThirdParty(final String query, final boolean searchByQuery) {
        searchInCraigsList(query);
        Call<ApplicationToken> call = EbayAPI.getClientTokenService().getApplicationToken(basicAuthToken, "client_credentials",
                EbayAPI.RuName, EbayAPI.APPLICATION_ACCESS_TOKEN_SCOPE);
        call.enqueue(new Callback<ApplicationToken>() {
            @Override
            public void onResponse(Call<ApplicationToken> call, Response<ApplicationToken> response) {
                applictionToken = response.body().getAccess_token();
                String authorization = "Bearer " + applictionToken;
                Call<SearchResponse> callComputer = null;
                if (searchByQuery) {
                    callComputer = EbayAPI.getService().getSearchResultByQuery(authorization, query, 15);
                } else {
                    callComputer = EbayAPI.getService().getSearchResultByCategory(authorization, CategoryKeywordToCategoryIdMap.get(query), 15);
                }
                callComputer.enqueue(new Callback<SearchResponse>() {
                    @Override
                    public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {
                        SearchResponse searchResponse = response.body();
                        itemList.clear();
                        searchRecycleView.setVisibility(View.VISIBLE);
                        searchRecycleView.setHasFixedSize(true);
                        searchRecycleViewLayoutManager = new LinearLayoutManager(getApplicationContext());
                        Log.v("itemsList","items"+itemsList);
                        ArrayList<Item> filteredList = null;
                        if (itemsList.isEmpty()) {
                            Log.v(TAG, "itemsList is empty: " + itemsList);
                            displayItems();
                            Log.v(TAG, "itemsList is: " + itemsList);
                        }
                        if (searchByQuery) {
                            filteredList = searchGrabOnItemsByQuery(itemsList, query);
                        } else {
                            filteredList = searchGrabOnItemsByCategory(itemsList, query);
                        }
                        searchRecycleViewAdapter = new ProductListAdapter(itemList, filteredList, craigslistItemsList);
                        searchRecycleView.setLayoutManager(searchRecycleViewLayoutManager);
                        searchRecycleView.setAdapter(searchRecycleViewAdapter);
                        for (ItemSummary itemEbay : searchResponse.getItemSummaries()) {
                            if (itemEbay != null) {
                                itemList.add(itemEbay);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        searchRecycleViewAdapter.setOnItemClickListener(new ProductListAdapter.OnItemClickListener() {
                            @Override
                            public void OnItemClickListener(int position) {
                                ItemSummary currentItem = itemList.get(position);
                                if (currentItem.getVendorID() == ProductListAdapter.GRAB_ON)
                                    for(Item itemGrabOn : itemsList) {
                                        if(itemGrabOn.getItemID().equals(currentItem.getItemId())) {
                                            openItemActivity(itemGrabOn);
                                            break;
                                        }
                                    }
                                else
                                    openBrowserActivity(currentItem);
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

    private void openItemActivity(Item item) {
        Intent itemPage = new Intent(MainActivity.this, ItemActivity.class);
        itemPage.putExtra("itemid", item.getItemID());
        itemPage.putExtra("selleruid",item.getItemSellerUID());
        itemPage.putExtra("itemname",item.getItemName());
        itemPage.putExtra("itemdesc", item.getItemDescription());
        itemPage.putExtra("itemprice", item.getItemPrice());
        itemPage.putExtra("itemimage", item.getItemImage());
        itemPage.putExtra("itemimagelist", item.getItemImageList());
        itemPage.putExtra("itemaddress", item.getItemAddress());
        itemPage.putExtra("itemcategory", item.getItemCategory());
        itemPage.putExtra("itemordered", item.isItemOrdered());
        itemPage.putExtra("itemlatitude", item.getLatitude());
        itemPage.putExtra("itemlongitude", item.getLongitude());
        startActivity(itemPage);
    }

    private void openBrowserActivity(ItemSummary item) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(item.getItemWebUrl()));
        startActivity(new Intent(browserIntent));
    }

    private ArrayList<Item> searchGrabOnItemsByQuery(ArrayList<Item> itemsList, final String query) {
        Log.v(TAG, "searchGrabOnItemsByQuery called with itemsList: " + itemsList);
        Log.v(TAG, "searchGrabOnItemsByQuery called with query: " + query);
        ArrayList<Item> filteredItems = new ArrayList<>();
        for (Item item : itemsList) {
            if (item.getItemName().toLowerCase().contains(query.toLowerCase()))
                filteredItems.add(item);
        }
        return filteredItems;
    }

    private ArrayList<Item> searchGrabOnItemsByCategory(ArrayList<Item> itemsList, final String query) {
        Log.v(TAG, "searchGrabOnItemsByCategory called with itemsList: " + itemsList);
        Log.v(TAG, "searchGrabOnItemsByCategory called with query: " + query);
        ArrayList<Item> filteredItems = new ArrayList<>();
        for (Item item : itemsList) {
            if (item.getItemCategory().toLowerCase().contains(query.toLowerCase()))
                filteredItems.add(item);
        }
        return filteredItems;
    }

    @Override
    public void processFinish(NodeList output) {
        craigslistItemsList = output;
    }

    private void getPreferencesFromDb() {

        db.collection("preferences").document(loggedinUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
                            if (task.getResult().get("distance") != null) {
                                distance = Integer.parseInt(task.getResult().get("distance").toString());
                                priceMin = Integer.parseInt(task.getResult().get("priceMin").toString());
                                priceMax = Integer.parseInt(task.getResult().get("priceMax").toString());
                                grabon = (boolean) task.getResult().get("grabon");
                                ebay = (boolean) task.getResult().get("ebay");
                                craigslist = (boolean) task.getResult().get("craigslist");
                                numberItems = Integer.parseInt(task.getResult().get("numberItems").toString());
                                userAddress = (String) task.getResult().get("userAddress");
                                userCity = (String) task.getResult().get("userCity");
                                userLatitude = (String) task.getResult().get("userLatitude");
                                userLongitude = (String) task.getResult().get("userLongitude");
                                userZipcode = (String) task.getResult().get("userZipcode");
                                currentUserCity = (String) task.getResult().get("currentUserCity");
                                currentUserLat = (String) task.getResult().get("currentUserLat");
                                currentUserLon = (String) task.getResult().get("currentUserLon");
                                currentUserZipcode = (String) task.getResult().get("currentUserZipcode");
                            }
                        }
                    }
                });

    }

    private void refreshItems() {
        itemAdapter = null;
        itemsList = new ArrayList<>();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerViewItems.setLayoutManager(gridLayoutManager);
        recyclerViewItems.setNestedScrollingEnabled(false);
        displayItems();
    }

    private void searchInCraigsList(String query) {
        //execute the async task
        String curdistance = Double.toString(usermiles);
        String postalCode = userZipcode;
        String queryURL = "https://sfbay.craigslist.org/search/sss?format=rss&query=";
        queryURL = queryURL + query;
        if(curdistance != "" && postalCode != "") {
            queryURL = queryURL + "&search_distance=" + curdistance + "&postal=" + postalCode;
        }
        String[] urlToRssFeed = {queryURL};
        new RetrieveFeedTask(MainActivity.this).execute(urlToRssFeed);
    }

}
