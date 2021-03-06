package com.spartans.grabon.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

/**
 * Author : Sudha Amarnath on 2020-02-20
 */
public class Singleton {

    // Using Singleton class for Firebase classes and is the entry point for all the user and db operations.
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private static final FirebaseStorage storage = FirebaseStorage.getInstance();

    public static FirebaseAuth getAuth() {
        return auth;
    }

    public static FirebaseFirestore getDb() {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        return db;
    }

    public static FirebaseUser getUser() {
        return  user;
    }

    public static FirebaseStorage getStorage() {
        return storage;
    }

}

