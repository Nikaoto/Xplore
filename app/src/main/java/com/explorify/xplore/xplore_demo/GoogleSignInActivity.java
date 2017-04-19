package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
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

import java.util.HashMap;
import java.util.Map;

import static android.R.attr.name;
import static com.explorify.xplore.xplore_demo.General.*;

/**
 * Created by nikao on 3/8/2017.
 */

public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 2;
    private static final int RC_REGISTER = 3;

    private FirebaseAuth auth;
    private SignInButton googleSignIn_b;
    public static GoogleApiClient googleApiClient;
    private FirebaseAuth.AuthStateListener authListener;
    private View myView;
    private PopupWindow popupWindow;

    private DatabaseReference DBref = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);
        myView = getLayoutInflater().inflate(R.layout.signin_layout, null);


        Authorize();

        googleSignIn_b = (SignInButton) findViewById(R.id.signin_google_button);
        googleSignIn_b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn() {
        popLoadingBar(0.8, 0.8, this, myView);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void popLoadingBar(double xScale, double yScale, Activity activity, View view)
    {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        View popupView = activity.getLayoutInflater().inflate(R.layout.loading_layout, null);


        popupWindow = new PopupWindow(popupView, popWidth, popHeight, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        dimBehind(popupWindow, 0.65f); //ajuk
    }

    private void Authorize()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    //Toast.makeText(GoogleSignInActivity.this, "Signed In", Toast.LENGTH_SHORT).show();
                    //User signed in
                    currentUserId = user.getUid();
                    CheckUserExists(user); //creates user in case it doesn't exist
                } else {
                    // User is signed out
                    Log.d("SIGNED OUT", "onAuthStateChanged:signed_out");
                }
            }
        };
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
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
        else if(requestCode == RC_REGISTER)
        {
            accountStatus = REGISTERED;
            finish();
        }
    }

    private void CheckUserExists(final FirebaseUser firebaseUser)
    {
        Query query = DBref.child("users").getRef().orderByKey().equalTo(firebaseUser.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    //Start user registration
                    RegisterActivity.newUser = firebaseUser;
                    Intent i = new Intent(GoogleSignInActivity.this, RegisterActivity.class);
                    startActivityForResult(i, RC_REGISTER);
                }
                else {
                    accountStatus = SIGNED_IN;
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accountStatus > 0) //If Logged in
            popupWindow.dismiss();
    }
}
