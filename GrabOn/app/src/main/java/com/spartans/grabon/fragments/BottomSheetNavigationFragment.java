package com.spartans.grabon.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.spartans.grabon.R;
import com.spartans.grabon.cart.Cart;
import com.spartans.grabon.item.AddItem;
import com.spartans.grabon.item.PostedItems;
import com.spartans.grabon.maps.MapsActivity;
import com.spartans.grabon.order.OrdersActivity;
import com.spartans.grabon.user.Profile;
import com.spartans.grabon.utils.Singleton;

public class BottomSheetNavigationFragment extends BottomSheetDialogFragment {

    Intent intent = new Intent();

    TextView username, emailid;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private FirebaseFirestore db = Singleton.getDb();;
    private ListenerRegistration documentRefRegistration;
    private String uID;
    private String paypalid = "";
    private String latitude = "";
    private String longitude = "";


    public static BottomSheetNavigationFragment newInstance() {

        Bundle args = new Bundle();

        BottomSheetNavigationFragment fragment = new BottomSheetNavigationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            //check the slide offset and change the visibility of close button
            if (slideOffset > 0.5) {
                closeButton.setVisibility(View.VISIBLE);
            } else {
                closeButton.setVisibility(View.GONE);
            }
        }
    };

    private ImageView closeButton;

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        //Get the content View
        View contentView = View.inflate(getContext(), R.layout.bottom_navigation_drawer, null);
        dialog.setContentView(contentView);


        NavigationView navigationView = contentView.findViewById(R.id.navigation_view);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        uID = auth.getCurrentUser().getUid();

        username = contentView.findViewById(R.id.user_name);
        emailid = contentView.findViewById(R.id.user_email);

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String name = task.getResult().get("firstname").toString()
                                    + " "
                                    + task.getResult().get("lastname").toString();
                            username.setText(name);
                            emailid.setText(task.getResult().get("email").toString());
                            paypalid = task.getResult().get("paypalid").toString();
                            latitude = task.getResult().get("latitude").toString();
                            longitude = task.getResult().get("longitude").toString();
                        }
                    }
                });

        //implement navigation menu item click event
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav01:
                        intent = new Intent(getActivity(), MapsActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav02:
                        if (paypalid.equals("")  || latitude.equals("") || longitude.equals("")) {
                            Toast.makeText(getContext(), "Set Paypal id and Address in User Profile before adding item", Toast.LENGTH_LONG).show();
                            intent = new Intent(getActivity(), Profile.class);
                            startActivity(intent);
                        } else {
                            intent = new Intent(getActivity(), AddItem.class);
                            startActivity(intent);
                        }
                        break;
                    case R.id.nav03:
                        intent = new Intent(getActivity(), Cart.class);
                        startActivity(intent);
                        break;
                    case R.id.nav04:
                        intent = new Intent(getActivity(), Profile.class);
                        startActivity(intent);
                        break;
                    case R.id.nav05:
                        intent = new Intent(getActivity(), OrdersActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.nav06:
                        intent = new Intent(getActivity(), PostedItems.class);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });
        closeButton = contentView.findViewById(R.id.close_image_view);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //dismiss bottom sheet
                dismiss();
            }
        });

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

}
