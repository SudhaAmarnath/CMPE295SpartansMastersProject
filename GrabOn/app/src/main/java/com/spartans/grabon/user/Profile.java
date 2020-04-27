package com.spartans.grabon.user;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.FileDataImageStatus;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Author : Sudha Amarnath on 2020-02-19
 */
public class Profile extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    public static final String TAG = "User Profile";
    private ListenerRegistration documentRefRegistration;
    private FirebaseUser loggedinUser;
    private String providerID;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private FirebaseFirestore db = Singleton.getDb();
    private FirebaseAuth auth;
    ArrayList image = new ArrayList();

    private static String edittxtaddrline = null;
    private static String addressline = null;
    private static String feature = null;
    private static String street = null;
    private static String city = null;
    private static String country = null;
    private static String pincode = null;
    private static String latitude = null;
    private static String longitude = null;
    private static ImageView addprofilepic= null;
    private static ImageView addImage= null;

    PlacesClient placesClient;

    private Uri filePath;

    int TAKE_IMAGE_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final EditText name, email, phone, paypalid, apt;
        final ImageView phonebutton, paypalidbutton, addressbutton, aptbutton;
        final AutocompleteSupportFragment address;
        final FirebaseAuth firebaseAuth;
        FirebaseFirestore firebaseFirestore;
        final String uID;
        final StorageReference profilePic;

        name = findViewById(R.id.profile_username);
        email = findViewById(R.id.Email);
        phone = findViewById(R.id.Phone);
        phonebutton = findViewById(R.id.profile_phone_edit);
        paypalid = findViewById(R.id.profile_paypalid);
        paypalidbutton = findViewById(R.id.profile_paypalid_edit);
        address = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.profile_address);
        addressbutton = findViewById(R.id.profile_address_edit);
        apt = findViewById(R.id.profile_addressapt);
        aptbutton = findViewById(R.id.profile_addressapt_edit);
        addprofilepic = findViewById(R.id.ImgUserV);
        addImage = findViewById(R.id.addImage);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        loggedinUser = firebaseAuth.getCurrentUser();
        auth = FirebaseAuth.getInstance();
        storage = Singleton.getStorage();
        storageReference = storage.getReference();

        String apiKey = getString(R.string.google_maps_key);
        if (!Places.isInitialized()) {
            Places.initialize(Profile.this, apiKey);
        }
        placesClient = Places.createClient(this);
        address.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS
        ));

        if (loggedinUser != null) {
            providerID = loggedinUser.getProviderData().get(1).getProviderId();
            uID = loggedinUser.getUid();
            profilePic = storageReference.child("profileImages/" + loggedinUser.getUid());
            Log.v("uploadProfileImage", " For the User On Create " +  profilePic.getDownloadUrl().toString());
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


                if(profilePic != null){
                    profilePic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.v("uploadProfileImage", " Downloaded URI  " +  uri.toString());
                            Glide.with(Profile.this).load(uri).into(addprofilepic);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.v("uploadProfileImage", " Unable to download image");
                        }
                    });
                }
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

        address.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {
                        final LatLng latLng = place.getLatLng();

                        Address address=getAddressFromLatLng(latLng);
                        if(address!=null) {
                            Profile.addressline = address.getAddressLine(0);
                            Profile.edittxtaddrline = place.getName();
                            Profile.feature = address.getFeatureName();
                            Profile.street = address.getThoroughfare();
                            Profile.city = address.getLocality();
                            Profile.country = address.getCountryName();
                            Profile.pincode = address.getPostalCode();
                            Profile.latitude = String.valueOf(address.getLatitude());
                            Profile.longitude = String.valueOf(address.getLongitude());
                            Log.d("Address : ", "" + address.toString());
                            Log.d("Address Line : ",""+Profile.addressline);
                            Log.d("Address : ", "" + Profile.edittxtaddrline);
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

                        Log.v("search", place.getLatLng().toString());
                        Log.v("search", place.getAddress());
                        Log.v("search", place.getName());

                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(Profile.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


        addressbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isValidAddress()) {

                    //Update item to the db
                    Map<String, Object> dbitem = new HashMap<>();
                    dbitem.put("address", Profile.addressline);
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
                    address.setText("");
                    address.setHint("Enter shipping address");
                    Toast toast2 = Toast.makeText(Profile.this, "Enter shipping address", Toast.LENGTH_SHORT);
                    TextView toastMessage= toast2.getView().findViewById(android.R.id.message);
                    toastMessage.setTextColor(Color.RED);
                    toast2.show();

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

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Log.d(TAG, "add image start");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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

        if (Strings.isNullOrEmpty(Profile.country) || Strings.isNullOrEmpty(Profile.feature)) {
            return false;
        }
        if (Profile.country.equals("United States") || Profile.country.equals("Canada")) {
            if (!Profile.edittxtaddrline.startsWith(Profile.feature)
                    || Strings.isNullOrEmpty(Profile.feature) || Strings.isNullOrEmpty(Profile.street)
                    || Strings.isNullOrEmpty(Profile.city) || Strings.isNullOrEmpty(Profile.pincode)) {
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

    public void uploadProfileImageClick(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, TAKE_IMAGE_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            uploadImage(new FileDataImageStatus() {
                @Override
                public void onSuccess(final Uri uri) {

                    Glide.with(Profile.this).load(uri).into(addprofilepic);
                    addprofilepic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                                Toast.makeText(Profile.this, "Item Added", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    });

                }

                @Override
                public void onError(String e) {

                }
            });

        }

    }

    private void uploadImage(final FileDataImageStatus fileDataImageStatus) {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading Image...");
            progressDialog.show();

            final StorageReference ref = storageReference.child("profileImages/" + loggedinUser.getUid());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Log.v("uploadProfileImage", ref.getDownloadUrl().toString());

                            UploadTask uploadTask = ref.putFile(filePath);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    Log.v("uploadProfileImage", ref.getDownloadUrl().toString());
                                    return ref.getDownloadUrl();
                                }

                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        fileDataImageStatus.onSuccess(downloadUri);
                                        image.add(downloadUri.toString());
                                        Log.v("uploadProfileImage", "During Upload Image "+downloadUri.toString());
                                    } else {
                                        Toast.makeText(Profile.this, "Error in fileDataImageStatus", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            fileDataImageStatus.onError("Error");
                            Toast.makeText(Profile.this, "Image Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Image Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

}
