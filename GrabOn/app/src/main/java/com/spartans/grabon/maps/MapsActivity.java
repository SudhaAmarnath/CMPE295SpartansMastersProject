package com.spartans.grabon.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.FileDataStatus;
import com.spartans.grabon.item.ItemActivity;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.DateUtilities;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int PERMISSION_CODE = 99;
    private static final int LOCATION_REQUEST = 500;
    private GoogleMap mMap;
    private View mapView;
    private GoogleApiClient mGoogleApiClient;
    //private ClusterManager<MapItem> mClusterManager;
    private ArrayList<Item> itemsList = new ArrayList<>();
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseFirestore db = Singleton.getDb();
    private FancyButton allItems;
    private FancyButton pickupItems;
    private static boolean showAllItems = true;
    private static MarkerOptions markerOptions;
    private ClusterManager<StringClusterItem> mClusterManager;
    public static String markerColor = "HUE_VIOLET";
    private FusedLocationProviderClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_CODE);
        }

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        mapView = mapFragment.getView();

        allItems = findViewById(R.id.MapShowAllButtons);
        pickupItems = findViewById(R.id.MapShowPickupButtons);


        allItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                mClusterManager.clearItems();
                showAllItems = true;
                markerColor = "HUE_VIOLET";
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        cluster(googleMap);
                    }
                });
                mMap.addMarker(markerOptions);
                Toast.makeText(MapsActivity.this, "Please zoom map to view all Grabon items", Toast.LENGTH_LONG).show();
            }
        });

        pickupItems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                mClusterManager.clearItems();
                showAllItems = false;
                markerColor = "HUE_RED";
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        cluster(googleMap);
                    }
                });
                mMap.addMarker(markerOptions);
                Toast.makeText(MapsActivity.this, "Please zoom map to view your pickup items", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if(ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    buildGoogleApiClient();
                } else {
                    Toast.makeText(this, "Location permission needed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission required to process", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        cluster(googleMap);

    }

    private void cluster(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.setIndoorEnabled(true);
        mMap.setTrafficEnabled(true);
        UiSettings mUiSettings = mMap.getUiSettings();
        mUiSettings.setZoomControlsEnabled(true);
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setMyLocationButtonEnabled(true);
        mUiSettings.setScrollGesturesEnabled(true);
        mUiSettings.setZoomGesturesEnabled(true);
        mUiSettings.setTiltGesturesEnabled(true);
        mUiSettings.setRotateGesturesEnabled(true);

        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.setMargins(30, 0, 0, 40);

        mClusterManager = new ClusterManager<>(this, mMap);

        final CustomClusterRenderer renderer = new CustomClusterRenderer(this, mMap, mClusterManager);

        mClusterManager.setRenderer(renderer);

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<StringClusterItem>() {
            @Override
            public boolean onClusterClick(Cluster<StringClusterItem> cluster) {
                Log.v("cluster", "Cluster clicked");
                return false;
            }
        });

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<StringClusterItem>() {
            @Override
            public boolean onClusterItemClick(StringClusterItem stringClusterItem) {
                Log.v("cluster", "Cluster item clicked");
                return false;
            }
        });

        mClusterManager.getMarkerCollection()
                .setInfoWindowAdapter(new CustomInfoViewAdapter(LayoutInflater.from(this)));

        mClusterManager.setOnClusterItemInfoWindowClickListener(
                new ClusterManager.OnClusterItemInfoWindowClickListener<StringClusterItem>() {
                    @Override public void onClusterItemInfoWindowClick(StringClusterItem stringClusterItem) {
                        int itemIndex = stringClusterItem.itemIndex;
                        Item item = itemsList.get(itemIndex);
                        Intent itemPage = new Intent(MapsActivity.this, ItemActivity.class);
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

        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(mClusterManager);
        displayItemsOnMap(showAllItems);
        mClusterManager.cluster();


    }

    private void getItems(final FileDataStatus fileDataStatus) {

        db.collection("items")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (!user.getUid().matches(document.get("selleruid").toString())) {

                                    ArrayList<String> imgs = new ArrayList<>();
                                    Map<String, Object> myMap = document.getData();

                                    boolean itempicked = (boolean) myMap.get("itempicked");

                                    if (!itempicked) {
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
                                        item.setItemAddress((String) myMap.get("itemaddress"));
                                        item.setLatitude((String) myMap.get("itemlatitude"));
                                        item.setLongitude((String) myMap.get("itemlongitude"));
                                        item.setItemOrdered((boolean) myMap.get("itemordered"));
                                        item.setItemPicked((boolean) myMap.get("itempicked"));
                                        String itemCreateTime = "";
                                        if (myMap.get("itemcreatetime") == null) {
                                            itemCreateTime = "1586590726600";
                                        } else {
                                            itemCreateTime = myMap.get("itemcreatetime").toString();
                                        }
                                        item.setItemCreateTime(itemCreateTime);
                                        String itembuyerid = "";
                                        if (myMap.get("itembuyerid") != null) {
                                            itembuyerid = (String) myMap.get("itembuyerid");
                                        }
                                        item.setItemBuyerId(itembuyerid);
                                        itemsList.add(item);

                                    }
                                }
                            }
                            fileDataStatus.onSuccess(itemsList);
                        } else {
                            fileDataStatus.onError("Error getting data");
                            Log.w("Posted Items:", "Error getting data.", task.getException());
                        }

                    }
                });
    }


    private void displayItemsOnMap (final boolean allItems) {

        itemsList = new ArrayList<>();
        final String userid = user.getUid();

        getItems(new FileDataStatus() {
            @Override
            public void onSuccess(ArrayList list) {

                for (int i = 0; i < itemsList.size(); i++) {
                    Item item = itemsList.get(i);
                    String itemname = item.getItemName();
                    float itemprice = item.getItemPrice();
                    String itembuyerid = item.getItemBuyerId();
                    boolean itemordered = item.isItemOrdered();
                    double itemlat = Double.parseDouble(item.getLatitude());
                    double itemlon = Double.parseDouble(item.getLongitude());
                    final LatLng latLng = new LatLng(itemlat, itemlon);
                    if (allItems) {
                        if (itemordered) {
                            if (userid.equals(itembuyerid)) {
                                mClusterManager.addItem(new StringClusterItem(
                                        "Item Name: " + itemname
                                        +"\n" + "Price: $"
                                        + String.valueOf(itemprice)
                                        + "\n" + "Click for Item details"
                                        , latLng, i));
                            }
                        } else {
                            mClusterManager.addItem(new StringClusterItem(
                                    "Item Name: " + itemname
                                    +"\n" + "Price: $"
                                    + String.valueOf(itemprice)
                                    + "\n" + "Click for Item details"
                                    , latLng, i));
                        }
                        Log.v("allitems", item.getItemName());
                    } else {
                        if (itemordered && userid.equals(itembuyerid)) {
                            String itemCreateTime = item.getItemCreateTime();
                            String itemPickupTime = new DateUtilities().getPostTimeInMillis(itemCreateTime,7);
                            itemPickupTime = new DateUtilities().getDateAndTime(itemPickupTime);
                            Log.v("pickupitems", item.getItemName() + "buyer:" + itembuyerid);
                            mClusterManager.addItem(new StringClusterItem(
                                    "Item Name: " + itemname
                                    +"\n" + "Price: $"
                                    + String.valueOf(itemprice)
                                    + "\n" + "Pickup By: "
                                    + itemPickupTime
                                    + "\n" + "Click for Item details"
                                    , latLng, i));
                        }
                    }

                }

            }

            @Override
            public void onError(String e) {

            }
        });

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        requestPermission();
        if(ActivityCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
            client.getLastLocation().addOnSuccessListener(MapsActivity.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        markerOptions.title("Your Location");
                        markerOptions.draggable(true);
                        mMap.addMarker(markerOptions);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
                    }
                }
            });
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Please check internet", Toast.LENGTH_SHORT).show();

    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

    static class StringClusterItem implements ClusterItem {

        final String title;
        //final String snippet;
        final LatLng latLng;
        int itemIndex = 0;

        public StringClusterItem(String title, LatLng latLng, int itemIndex) {
            this.itemIndex = itemIndex;
            this.title = title;
            this.latLng = latLng;
            //this.snippet = snippet;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getSnippet() {
            return null;
        }

        @Override public LatLng getPosition() {
            return latLng;
        }
    }


}
