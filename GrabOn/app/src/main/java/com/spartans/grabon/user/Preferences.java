package com.spartans.grabon.user;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.utils.Singleton;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * Author : Sudha Amarnath on 2020-04-25
 */
public class Preferences extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private FirebaseAuth firebaseAuth;

    PlacesClient placesClient;
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

    private AutocompleteSupportFragment address;
    private TextView prefereceDistanceText;
    private SeekBar seekBar;
    private Switch grabonCheck;
    private Switch ebayCheck;
    private Switch craigslistCheck;
    private CrystalRangeSeekbar rangeSeekbar;
    private TextView preferencePriceMin;
    private TextView preferencePriceMax;
    private NumberPicker numberPicker;

    private FusedLocationProviderClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        db = Singleton.getDb();
        firebaseAuth = FirebaseAuth.getInstance ();
        user = firebaseAuth.getCurrentUser();

        requestPermission();
        if(ActivityCompat.checkSelfPermission(Preferences.this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            client = LocationServices.getFusedLocationProviderClient(Preferences.this);
            client.getLastLocation().addOnSuccessListener(Preferences.this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        currentUserLat = String.valueOf(location.getLatitude());
                        currentUserLon = String.valueOf(location.getLongitude());
                        Geocoder geocoder = new Geocoder(Preferences.this, Locale.ENGLISH);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && addresses.size() > 0) {
                                if(addresses.get(0).getPostalCode()!=null) {
                                    currentUserZipcode = addresses.get(0).getPostalCode();
                                }
                                if (addresses.get(0).getLocality() != null) {
                                    currentUserCity = addresses.get(0).getLocality();
                                }
                                Log.v("location", "currentUserCity:" + currentUserCity);
                                Log.v("location", "currentUserZipcode:" + currentUserZipcode);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        DocumentReference documentReference = db.collection("preferences").document(user.getUid());
                        documentReference.update("currentUserLat", currentUserLat,
                                "currentUserLon", currentUserLon,
                                "currentUserCity", currentUserCity,
                                "currentUserZipcode", currentUserZipcode)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.v("location:", "current location updated");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v("location:", "current location update failed");
                            }
                        });

                    }
                }
            });
        } else {
            Log.v("LocationServices:", "Couldn't get permissions");
        }

        getPreferencesFromDb();
        updatePreferences();

        address = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.PreferencesLocation);
        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(Preferences.this, apiKey);
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

                final LatLng latLng = place.getLatLng();

                userAddress = place.getAddress();
                userLatitude = String.valueOf(latLng.latitude);
                userLongitude = String.valueOf(latLng.longitude);

                Geocoder geocoder = new Geocoder(Preferences.this, Locale.ENGLISH);
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        if(addresses.get(0).getPostalCode()!=null){
                            userZipcode = addresses.get(0).getPostalCode();
                        }
                        if (addresses.get(0).getLocality() != null) {
                            userCity = addresses.get(0).getLocality();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.v("search", place.getLatLng().toString());
                Log.v("search", place.getAddress());
                Log.v("search", place.getName());
                Log.v("search", userZipcode + ":" + userCity);
            }
            @Override
            public void onError(@NonNull Status status) {

            }
        });

        prefereceDistanceText = findViewById(R.id.PreferencesDistanceText);
        seekBar = findViewById(R.id.PreferencesDistance);
        seekBar.setProgress(distance);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressValue = distance;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefereceDistanceText.setText(String.valueOf(progressValue) + " Miles");
                distance = progressValue;
            }
        });


        grabonCheck = findViewById(R.id.PrefereceGrabon);
        grabonCheck.setChecked(grabon);
        grabonCheck.setClickable(false);


        ebayCheck = findViewById(R.id.PreferenceEbay);
        ebayCheck.setChecked(ebay);
        ebayCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ebay = ((Switch) v).isChecked();

            }
        });

        craigslistCheck = findViewById(R.id.PreferenceCraigslist);
        craigslistCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                craigslist = ((Switch) v).isChecked();

            }
        });

        numberPicker = findViewById(R.id.PreferenceItems);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(9);
        final String [] pickerValues = new String[] {"5", "10", "15", "20", "25", "30", "35", "40", "45", "50"};
        numberPicker.setDisplayedValues(pickerValues);
        numberPicker.setValue(2);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                numberItems = Integer.parseInt(pickerValues[picker.getValue()]);
            }
        });

        rangeSeekbar = findViewById(R.id.PreferencesPriceRange);
        preferencePriceMin = findViewById(R.id.PreferecePriceMin);
        preferencePriceMax = findViewById(R.id.PreferecePriceMax);
        rangeSeekbar.setMinValue(0);
        rangeSeekbar.setMaxValue(1000);
        rangeSeekbar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                preferencePriceMin.setText(String.valueOf(minValue));
                preferencePriceMax.setText(String.valueOf(maxValue));
            }
        });
        rangeSeekbar.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                priceMin = Integer.parseInt(String.valueOf(minValue));
                priceMax = Integer.parseInt(String.valueOf(maxValue));
            }
        });

        FancyButton reset = findViewById(R.id.PreferencesResetButton);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                numberItems = 15;
                distance = 25;
                grabon = true;
                ebay = true;
                craigslist = true;
                priceMin = 0;
                priceMax = 2000;
                userZipcode = "";
                userCity = "";
                userAddress = "";
                userLatitude = "";
                userLongitude = "";

                DocumentReference documentReference = db.collection("preferences").document(user.getUid());
                Map<String, Object> dbpref = new HashMap<>();
                dbpref.put("distance", distance);
                dbpref.put("grabon", grabon);
                dbpref.put("ebay", ebay);
                dbpref.put("craigslist", craigslist);
                dbpref.put("priceMin", priceMin);
                dbpref.put("priceMax", priceMax);
                dbpref.put("numberItems", numberItems);
                dbpref.put("userAddress", "");
                dbpref.put("userZipcode", "");
                dbpref.put("userCity", "");
                dbpref.put("userLatitude", "");
                dbpref.put("userLongitude", "");
                dbpref.put("currentUserLat", currentUserLat);
                dbpref.put("currentUserLon", currentUserLon);
                dbpref.put("currentUserCity", currentUserCity);
                dbpref.put("currentUserZipcode", currentUserZipcode);

                documentReference.set(dbpref).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    updatePreferences();
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Preferences.this,"Preferences successfully reset",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        }
                                    }, 2000);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Preferences.this,"Preferences reset not successful",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });

        FancyButton save = findViewById(R.id.PreferencesSaveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DocumentReference documentReference = db.collection("preferences").document(user.getUid());
                Map<String, Object> dbpref = new HashMap<>();
                dbpref.put("distance", distance);
                dbpref.put("grabon", grabon);
                dbpref.put("ebay", ebay);
                dbpref.put("craigslist", craigslist);
                dbpref.put("priceMin", priceMin);
                dbpref.put("priceMax", priceMax);
                dbpref.put("numberItems", numberItems);
                dbpref.put("userAddress", userAddress);
                dbpref.put("userZipcode", userZipcode);
                dbpref.put("userCity", userCity);
                dbpref.put("userLatitude", userLatitude);
                dbpref.put("userLongitude", userLongitude);
                dbpref.put("currentUserLat", currentUserLat);
                dbpref.put("currentUserLon", currentUserLon);
                dbpref.put("currentUserCity", currentUserCity);
                dbpref.put("currentUserZipcode", currentUserZipcode);

                documentReference.set(dbpref).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(Preferences.this,"Preferences saved successfully",Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("PreferenceGrabon",grabon);
                                            intent.putExtra("PreferenceEbay",ebay);
                                            intent.putExtra("PreferenceCraigslist",craigslist);
                                            intent.putExtra("PreferredNumberOfItems",numberItems);
                                            intent.putExtra("PreferenceMinimumPrice",priceMin);
                                            intent.putExtra("PreferenceMaximumPrice",priceMax);
                                            startActivity(intent);
                                        }
                                    }, 2000);                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Preferences.this,"Preferences save not successful",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

            }
        });

    }

    private void getPreferencesFromDb() {

        db.collection("preferences").document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()) {
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
                });

    }

    private void updatePreferences() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                prefereceDistanceText.setText(String.valueOf(distance) + " Miles");
                seekBar.setProgress(distance);
                preferencePriceMin.setText(String.valueOf(priceMin));
                preferencePriceMax.setText(String.valueOf(priceMax));
                grabonCheck.setChecked(grabon);
                ebayCheck.setChecked(ebay);
                craigslistCheck.setChecked(craigslist);
                rangeSeekbar.setMinStartValue(priceMin)
                        .setMaxStartValue(priceMax)
                        .apply();
                String [] pickerValues = new String[] {"5", "10", "15", "20", "25", "30", "35", "40", "45", "50"};
                numberPicker.setValue(Arrays
                        .asList(pickerValues)
                        .indexOf(String.valueOf(numberItems)));
                if (userCity.equals("")) {
                    address.setText(currentUserCity);
                } else {
                    address.setText(userAddress);
                }
            }
        }, 2000);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Preferences.this, new String[]{ACCESS_FINE_LOCATION}, 1);
    }

}
