package com.spartans.grabon.user;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.spartans.grabon.MainActivity;
import com.spartans.grabon.R;
import com.spartans.grabon.utils.Singleton;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

/**
 * Author : Sudha Amarnath on 2020-01-29
 */
public class Login extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private FancyButton loginButton, loginGmailButton;
    private LoginButton loginFacebookButton;
    private ProgressBar loginProgressBar;
    private FirebaseAuth auth;
    private TextView loginCreateUser;
    private FirebaseFirestore db;
    private String uID;
    //a constant for detecting the login intent result
    private static final int RC_SIGN_IN = 9001;
    //Tag for the logs optional
    private static final String TAG = "GrabOn";
    //creating a GoogleSignInClient object
    public static GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.LoginEmail);
        loginPassword = findViewById(R.id.LoginPassword);
        loginButton = findViewById(R.id.LoginButton);
        loginProgressBar = findViewById(R.id.LoginProgressBar);
        loginCreateUser = findViewById(R.id.LoginCreateUser);
        loginGmailButton = findViewById(R.id.LoginGmailButton);
        loginFacebookButton = findViewById(R.id.LoginFacebookButton);

        auth = Singleton.getAuth();
        db = Singleton.getDb();

        //Then we need a GoogleSignInOptions object
        //And we need to build it as below
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        //Then we will get the GoogleSignInClient object from GoogleSignIn class
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        logoutPreviousUser();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    loginEmail.setError("Enter Email ID");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    loginPassword.setError("Enter Password");
                    return;
                }

                loginProgressBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(Login.this, "Error ! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            loginProgressBar.setVisibility(View.GONE);
                        }

                    }
                });

            }
        });


        loginCreateUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Register.class));
            }
        });

        //Now we will attach a click listener to the sign_in_button
        //and inside onClick() method we are calling the signIn() method that will open
        //google sign in intent
        loginGmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInGoogle();
            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        loginFacebookButton.setReadPermissions("email", "public_profile");
        loginFacebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //this method is called on click
    private void signInGoogle() {
        //getting the google signin intent
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        //starting the activity for result
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount  googleAccount = completedTask.getResult(ApiException.class);

            //authenticating with firebase
            firebaseAuthWithGoogle(googleAccount);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(Login.this, "Logged in Successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        } catch (ApiException e) {
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //getting the auth credential
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        //Now using firebase we are signing in the user here
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            AddUserToFirebaseDB();
                            Toast.makeText(Login.this, "User Signed In", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            AddUserToFirebaseDB();
                            startActivity(new Intent(getApplicationContext(),MainActivity.class));
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void AddUserToFirebaseDB() {
        FirebaseUser loggedinUser = auth.getCurrentUser();
        if(loggedinUser!=null) {
            uID = loggedinUser.getUid();
            if(uID != null) {
                String emptyVal = "";
                DocumentReference documentReference = db.collection("users").document(uID);
                Map<String, Object> user = new HashMap<>();
                user.put("firstname", loggedinUser.getDisplayName());
                user.put("lastname", emptyVal);
                user.put("email", loggedinUser.getEmail());
                user.put("password", emptyVal);
                user.put("paypalid", "");
                user.put("address", "");
                user.put("apt", "");
                user.put("phone", "");
                user.put("latitude", "");
                user.put("longitude", "");
                documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: User Profile is created for " + uID);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                    }
                });
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(Login.this, "User Registered", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void logoutPreviousUser() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String providerID = user.getProviderData().get(1).getProviderId();
            FirebaseAuth.getInstance().signOut();
            if (providerID.equals("google.com")) {
                Login.mGoogleSignInClient.signOut();
            } else if (providerID.equals("facebook.com")) {
                LoginManager.getInstance().logOut();
            }
        }
    }
}