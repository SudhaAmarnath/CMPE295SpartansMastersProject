package com.spartans.grabon.user;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.Login;
import com.spartans.grabon.R;
import com.spartans.grabon.adapters.PlaceAutoSuggestAdapter;
import com.spartans.grabon.utils.Singleton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KMLogoutHandler;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Profile extends AppCompatActivity {

    public static final String TAG = "TAG";
    private ListenerRegistration documentRefRegistration;
    private FirebaseUser loggedinUser;
    private String providerID;
    private FirebaseFirestore db = Singleton.getDb();

    private static String edittxtaddrline = null;
    private static String addressline = null;
    private static String feature = null;
    private static String street = null;
    private static String city = null;
    private static String country = null;
    private static String pincode = null;
    private static String latitude = null;
    private static String longitude = null;
    private static GeoPoint geopoint = new GeoPoint(0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final EditText name, email, phone, paypalid, apt;
        final Button userLogout;
        final ImageView phonebutton, paypalidbutton, addressbutton, aptbutton;
        final AutoCompleteTextView address;
        final FirebaseAuth firebaseAuth;
        FirebaseFirestore firebaseFirestore;
        final String uID;

        name = findViewById(R.id.profile_username);
        email = findViewById(R.id.Email);
        userLogout = findViewById(R.id.UserLogout);
        phone = findViewById(R.id.Phone);
        phonebutton = findViewById(R.id.profile_phone_edit);
        paypalid = findViewById(R.id.profile_paypalid);
        paypalidbutton = findViewById(R.id.profile_paypalid_edit);
        address = findViewById(R.id.profile_address);
        addressbutton = findViewById(R.id.profile_address_edit);
        apt = findViewById(R.id.profile_addressapt);
        aptbutton = findViewById(R.id.profile_addressapt_edit);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loggedinUser = firebaseAuth.getCurrentUser();

        address.setAdapter(new PlaceAutoSuggestAdapter(Profile.this,android.R.layout.simple_list_item_1));

        if (loggedinUser != null) {
            providerID = loggedinUser.getProviderData().get(1).getProviderId();
            uID = loggedinUser.getUid();


                DocumentReference documentReference = firebaseFirestore.collection("users").document(uID);
                documentRefRegistration = documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        String fn = documentSnapshot.getString("firstname");
                        String ln = documentSnapshot.getString("lastname");
                        name.setText(fn + " " + ln);
                        email.setText(documentSnapshot.getString("email"));
                        phone.setText(documentSnapshot.getString("phone"));
                        address.setText(documentSnapshot.getString("address"));
                        paypalid.setText(documentSnapshot.getString("paypalid"));

                    }
                });



        }

        phonebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ph = phone.getText().toString();

                if (isValidPhone(ph)) {
                    //Update item to the db
                    Map<String, Object> dbitem = new HashMap<>();
                    dbitem.put("phone", ph);

                    DocumentReference updateUser = db.collection("users")
                            .document(loggedinUser.getUid());

                    updateUser.update(dbitem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.v("updateProfile", "Update Phone No. Success: ");
                                    Toast.makeText(Profile.this, "Update Phone No. Success:", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v("updateProfile", "Update Phone No. Failed: ");
                            Toast.makeText(Profile.this, "Update Phone No. Failed:", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        });

        paypalidbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = paypalid.getText().toString();

                if (isValidEmail(id)) {
                        //Update item to the db
                        Map<String, Object> dbitem = new HashMap<>();
                        dbitem.put("paypalid", paypalid.getText().toString());

                        DocumentReference updateUser = db.collection("users")
                                .document(loggedinUser.getUid());

                        updateUser.update(dbitem)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.v("updateProfile", "Update Paypal ID Success: ");
                                        Toast.makeText(Profile.this, "Update Paypal ID Success:", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.v("updateProfile", "Update Paypal ID Failed: ");
                                Toast.makeText(Profile.this, "Update Paypal ID Failed:", Toast.LENGTH_SHORT).show();
                            }
                        });
                } else {
                    paypalid.setError("Invalid Email Address");
                }

            }
        });

        address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Address : ",address.getText().toString());
                LatLng latLng=getLatLngFromAddress(address.getText().toString());
                if(latLng!=null) {
                    Address address=getAddressFromLatLng(latLng);
                    if(address!=null) {
                        Profile.addressline = address.getAddressLine(0);
                        Profile.feature = address.getFeatureName();
                        Profile.street = address.getThoroughfare();
                        Profile.city = address.getLocality();
                        Profile.country = address.getCountryName();
                        Profile.pincode = address.getPostalCode();
                        Profile.latitude = String.valueOf(address.getLatitude());
                        Profile.longitude = String.valueOf(address.getLongitude());
                        Log.d("Address : ", "" + address.toString());
                        Log.d("Address Line : ",""+Profile.addressline);
                        Log.d("Phone : ",""+address.getPhone());
                        Log.d("Pin Code : ",""+Profile.pincode);
                        Log.d("Feature No: ",""+Profile.feature);
                        Log.d("City : ",""+Profile.city);
                        Log.d("Street : ",""+Profile.street);
                        Log.d("Country : ",""+Profile.country);
                        Log.d("Lat Lng: ",""+Profile.latitude + " " + Profile.longitude);
                    } else {
                        Log.d("Address","Address Not Found");
                    }
                }
                else {
                    Log.d("LatLng","Latitude Longitued Not Found");
                }

            }
        });

        addressbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Profile.edittxtaddrline = address.getText().toString();

                if (isValidAddress()) {

                    //Update item to the db
                    Map<String, Object> dbitem = new HashMap<>();
                    dbitem.put("address", Profile.edittxtaddrline);
                    dbitem.put("latitude", Profile.latitude);
                    dbitem.put("longitude", Profile.longitude);
                    DocumentReference updateUser = db.collection("users")
                            .document(loggedinUser.getUid());

                    updateUser.update(dbitem)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.v("updateProfile", "Update Address Success: ");
                                    Toast.makeText(Profile.this, "Update Address Success:", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.v("updateProfile", "Update Address Failed: ");
                            Toast.makeText(Profile.this, "Update Address Failed:", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    address.setError("Invalid Postal Address");
                }
            }
        });


        aptbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Update item to the db
                Map<String, Object> dbitem = new HashMap<>();
                dbitem.put("apt", apt.getText().toString());

                DocumentReference updateUser = db.collection("users")
                        .document(loggedinUser.getUid());

                updateUser.update(dbitem)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.v("updateProfile", "Update Apt Success: ");
                                Toast.makeText(Profile.this,"Update Apt Success:", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.v("updateProfile", "Update Apt Failed: ");
                        Toast.makeText(Profile.this,"Update Apt Failed:", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });


        userLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (documentRefRegistration!=null) {
                        documentRefRegistration.remove();
                    }
                    FirebaseAuth.getInstance().signOut();//logout

                    if (providerID.equals("google.com")) {
                        Login.mGoogleSignInClient.signOut();
                    } else if (providerID.equals("facebook.com")) {
                        LoginManager.getInstance().logOut();
                    }

                    // Logout chatbot
                    Kommunicate.logout(getApplicationContext(), new KMLogoutHandler() {
                        @Override
                        public void onSuccess(Context context) {
                            Log.i("Logout","Success");
                        }

                        @Override
                        public void onFailure(Exception exception) {
                            Log.i("Logout","Failed");

                        }
                    });

                } catch (Exception logoutException) {
                    Log.d(TAG, "logoutException: " + logoutException.toString());
                }
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidPhone(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.PHONE.matcher(target).matches());
    }

    public static boolean isValidAddress() {

        if (Profile.country.equals("United States") || Profile.country.equals("Canada")) {
            if (!Profile.edittxtaddrline.startsWith(Profile.feature)
                    || Profile.feature.equals("null") || Profile.street.equals("null")
                    || Profile.city.equals("null") || Profile.pincode.equals("null")) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }


    }

    private LatLng getLatLngFromAddress(String address){

        Geocoder geocoder=new Geocoder(Profile.this);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if(addressList!=null){
                Address singleaddress=addressList.get(0);
                LatLng latLng=new LatLng(singleaddress.getLatitude(),singleaddress.getLongitude());
                return latLng;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    private Address getAddressFromLatLng(LatLng latLng){
        Geocoder geocoder=new Geocoder(Profile.this);
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 5);
            if(addresses!=null){
                Address address=addresses.get(0);
                return address;
            }
            else{
                return null;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


}
