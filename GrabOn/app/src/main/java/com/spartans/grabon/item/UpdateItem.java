package com.spartans.grabon.item;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.interfaces.FileDataImageStatus;
import com.spartans.grabon.model.Item;
import com.spartans.grabon.utils.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-03-30
 */
public class UpdateItem extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Item item;
    private Uri filePath;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();
    private String uID;

    private String itemID;
    private String itemSellerUID;
    private String itemName;
    private String itemDesc;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;
    private ImageButton updateItemImage;
    private EditText updateItemName;
    private EditText updateItemDesc;
    private EditText updateItemPrice;
    private FancyButton updateItemButton, deleteItemButton;
    public static String docid=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_item);

        updateItemImage = findViewById(R.id.updateItemImage);
        updateItemName = findViewById(R.id.updateItemName);
        updateItemDesc = findViewById(R.id.updateItemDesc);
        updateItemPrice = findViewById(R.id.updateItemPrice);
        updateItemButton = findViewById(R.id.updateItemButton);
        deleteItemButton = findViewById(R.id.deleteItemButton);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = auth.getCurrentUser().getUid();
        storage = Singleton.getStorage();
        storageReference = storage.getReference();


        itemID = (String) getIntent().getSerializableExtra("itemid");
        itemSellerUID = (String) getIntent().getSerializableExtra("selleruid");
        itemName = (String) getIntent().getSerializableExtra("itemname");
        itemDesc = (String) getIntent().getSerializableExtra("itemdesc");
        itemPrice = (float) getIntent().getSerializableExtra("itemprice");
        itemImage = (String) getIntent().getSerializableExtra("itemimage");
        itemImageList = (ArrayList) getIntent().getSerializableExtra("itemimagelist");

        updateItemName.setText(itemName);
        updateItemDesc.setText(itemDesc);
        updateItemPrice.setText(String.valueOf(itemPrice));

        updateItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Log.v("sudha", "add image start");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });

        updateItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateItem();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        deleteItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            uploadImage(new FileDataImageStatus() {
                @Override
                public void onSuccess(final Uri uri) {

                    Glide.with(UpdateItem.this).load(uri).into(updateItemImage);
                    updateItemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            ArrayList image = new ArrayList();
                            image.add(uri.toString());

                            String itemName = updateItemName.getText().toString();
                            String itemDesc = updateItemDesc.getText().toString();
                            String itemPrice = updateItemPrice.getText().toString();

                            if (TextUtils.isEmpty(itemName) == false &&
                                TextUtils.isEmpty(itemDesc) == false &&
                                TextUtils.isEmpty(itemPrice) == false) {

                                item = new Item(itemName, itemDesc, uID,
                                        Float.valueOf(itemPrice),
                                        uri.toString(), image);

                                //Add item to the db
                                Map<String, Object> dbitem = new HashMap<>();
                                dbitem.put("selleruid",item.getItemSellerUID());
                                dbitem.put("itemname",item.getItemName());
                                dbitem.put("itemdesc", item.getItemDescription());
                                dbitem.put("itemprice", item.getItemPrice());
                                dbitem.put("itemimage", item.getItemImage());
                                dbitem.put("itemimagelist", item.getItemImageList());

                                DocumentReference updateItem = db.collection("items")
                                        .document(itemID);
                                updateItem.update(dbitem)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.v("updateItem", "Update Item Success, Document ID : " + itemID);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.v("updateItem", "Update Item Failed: " + itemID);
                                    }
                                });
                                Toast.makeText(UpdateItem.this, "Item Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(UpdateItem.this, "Item Not Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    deleteItemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            deleteItem();
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

            final StorageReference ref = storageReference.child("images/" + user.getUid() + "/" + UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Log.v("uploadImage", ref.getDownloadUrl().toString());

                            UploadTask uploadTask = ref.putFile(filePath);

                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    Log.v("uploadImage", ref.getDownloadUrl().toString());
                                    return ref.getDownloadUrl();
                                }

                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downloadUri = task.getResult();
                                        fileDataImageStatus.onSuccess(downloadUri);
                                    } else {
                                        Toast.makeText(UpdateItem.this, "Error in fileDataImageStatus", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(UpdateItem.this, "Image Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void updateItem () {

        String itemName = updateItemName.getText().toString();
        String itemDesc = updateItemDesc.getText().toString();
        String itemPrice = updateItemPrice.getText().toString();

        if (TextUtils.isEmpty(itemName) == false &&
                TextUtils.isEmpty(itemDesc) == false &&
                TextUtils.isEmpty(itemPrice) == false) {

            //Update item to the db
            Map<String, Object> dbitem = new HashMap<>();
            dbitem.put("itemname",itemName);
            dbitem.put("itemdesc", itemDesc);
            dbitem.put("itemprice", Float.valueOf(itemPrice));
            //dbitem.put("itemimage", item.getItemImage());
            //dbitem.put("itemimagelist", item.getItemImageList());


            DocumentReference updateItem = db.collection("items")
                    .document(itemID);

            updateItem.update(dbitem)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.v("updateItem", "Update Item Success, Document ID : " + itemID);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.v("updateItem", "Update Item Failed: " + itemID);
                }
            });

            Toast.makeText(UpdateItem.this, "Item Updated", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(UpdateItem.this, "Item Not Updated", Toast.LENGTH_SHORT).show();
        }

    }


    private void deleteItem () {

        db.collection("items")
                .document(itemID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.v("deleteItem", "Delete Item Success, Document ID : " + itemID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v("deleteItem", "Delete Item Failed, Document ID : " + itemID);
            }
        });

    }

}
