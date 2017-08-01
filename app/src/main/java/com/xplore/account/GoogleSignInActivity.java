package com.xplore.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.xplore.ApiManager;
import com.xplore.General;
import com.xplore.R;

import static com.xplore.General.*;

/**
 * Created by Nikaoto on 3/8/2017.
 * TODO write description of this class - what it does and why.
 */

public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 2;
    private static final int RC_REGISTER = 3;

    public static GoogleApiClient googleApiClient;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private SignInButton googleSignInButton;
    private FirebaseAuth.AuthStateListener authListener;
    private View myView;
    private PopupWindow popupWindow;

    private DatabaseReference firebaseUsersReference
            = FirebaseDatabase.getInstance().getReference().child("users");


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);
        setTitle(R.string.activity_authorization_title);
        myView = getLayoutInflater().inflate(R.layout.signin_layout, null);

        //Building Google Api Client
        googleApiClient = ApiManager.INSTANCE.getGoogleAuthApiClient(this);

        //Setting up auth state listener
        setUpAuthStateListener();

        googleSignInButton = (SignInButton) findViewById(R.id.signin_google_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        popupWindow = General.popLoadingBar(0.8, 0.8, this, myView);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setUpAuthStateListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(GoogleSignInActivity.this, "Logged In", Toast.LENGTH_SHORT).show(); //TODO string resources

                    //User signed in
                    currentUserId = user.getUid();
                    checkUserExists(user); //creates user in case it doesn't exist
                } else {
                    // User is signed out
                    Log.d("SIGNED OUT", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    private void checkUserExists(final FirebaseUser user) {
        Log.println(Log.INFO, "firebaseuser", "User FullName = "+user.getDisplayName());
        Query query = firebaseUsersReference.orderByKey().equalTo(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //Start user registration
                    startActivityForResult(
                            RegisterActivity.getStartIntent(
                                    GoogleSignInActivity.this,
                                    user.getUid(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    user.getPhotoUrl()
                            ),
                            RC_REGISTER);
                }
                else {
                    accountStatus = JUST_LOGGED_IN;
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(GoogleSignInActivity.this, "Could not authenticate", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if(requestCode == RC_REGISTER) {
            Toast.makeText(getApplicationContext(),
                    "Registered successfully. Welcome to Xplore!",
                    Toast.LENGTH_SHORT)
                    .show();
            General.accountStatus = General.JUST_LOGGED_IN;
            finish();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.

                        if (task.isSuccessful()) {
                            //Toast.makeText(GoogleSignInActivity.this, "Authenticated", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Log.w("SIGN IN", "signInWithCredential", task.getException());
                            Toast.makeText(GoogleSignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(authListener != null)
            auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(authListener);
        googleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accountStatus > 0) //If Logged in
            popupWindow.dismiss();
    }
}
