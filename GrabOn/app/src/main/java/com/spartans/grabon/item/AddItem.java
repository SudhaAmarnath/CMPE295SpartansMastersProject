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
import com.google.firebase.firestore.FieldValue;
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
 * Author : Sudha Amarnath on 2020-02-23
 */
public class AddItem extends AppCompatActivity {

    private static final int SELECT_PICTURE = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Item item;
    private Uri filePath;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();
    private String uID;

    private ImageButton addItemImage;
    private EditText addItemName;
    private EditText addItemDesc;
    private EditText addItemPrice;
    private FancyButton addItemButton;
    public static String docid=null;
    ArrayList image = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        addItemImage = findViewById(R.id.addItemImage);
        addItemName = findViewById(R.id.addItemName);
        addItemDesc = findViewById(R.id.addItemDesc);
        addItemPrice = findViewById(R.id.addItemPrice);
        addItemButton = findViewById(R.id.addItemButton);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = auth.getCurrentUser().getUid();
        storage = Singleton.getStorage();
        storageReference = storage.getReference();

        addItemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                Log.v("sudha", "add image start");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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

                    Glide.with(AddItem.this).load(uri).into(addItemImage);
                    addItemButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //ArrayList image = new ArrayList();
                            //image.add(uri.toString());

                            String itemName = addItemName.getText().toString();
                            String itemDesc = addItemDesc.getText().toString();
                            String itemPrice = addItemPrice.getText().toString();

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
                                dbitem.put("itemordered", false);
                                dbitem.put("itempicked", false);

                                db.collection("items")
                                        .add(dbitem)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                AddItem.docid = documentReference.getId();
                                                documentReference.update("itemid", FieldValue.arrayUnion(AddItem.docid));
                                                Log.v("addItem", "Add Item Success, Document ID : " + AddItem.docid);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.v("addItem", "Add Item Failed");
                                            }
                                        });
                                Toast.makeText(AddItem.this, "Item Added", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(AddItem.this, "Item Not Added", Toast.LENGTH_SHORT).show();
                            }
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
                                        image.add(downloadUri.toString());
                                        Log.v("uploadImages", downloadUri.toString());
                                    } else {
                                        Toast.makeText(AddItem.this, "Error in fileDataImageStatus", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(AddItem.this, "Image Upload Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
