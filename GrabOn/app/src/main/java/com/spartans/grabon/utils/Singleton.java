package com.spartans.grabon.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Singleton {

    // Using Singleton class for FirebaseFirestore and is the entry point for all the user and db operations.
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public static FirebaseFirestore getDb() {
        return db;
    }

    public static FirebaseUser getUser() {
        return  user;
    }

}

