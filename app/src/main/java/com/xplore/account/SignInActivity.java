package com.xplore.account;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.xplore.ApiManager;
import com.xplore.General;
import com.xplore.R;
import com.xplore.base.BaseAppCompatActivity;

import static com.xplore.General.JUST_LOGGED_IN;
import static com.xplore.General.accountStatus;
import static com.xplore.General.currentUserId;

/**
 * Created by Nikaoto on 3/8/2017.
 *
 * Handles sign in with Google and Facebook
 * Launches RegisterAct if new user
 *
 */


public class SignInActivity extends BaseAppCompatActivity {

    private static String TAG = "brejk";

    private boolean signInWithFacebook = false;

    private static final int REQ_GOOGLE_SIGN_IN = 1;
    //private static final int REQ_FACEBOOK_SIGN_IN = 2;
    private static final int REQ_REGISTER = 3;

    // Firebase
    private DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

    // For google login
    private GoogleApiClient googleApiClient;

    // For facebook login
    CallbackManager callbackManager;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authListener = setUpAuthStateListener();
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);
        setTitle(R.string.activity_authorization_title);

        // Building Google Api Client
        googleApiClient = ApiManager.getGoogleAuthApiClient(this);

        // Setting up auth state listener
        setUpAuthStateListener();

        // Initialize Google Login button
        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithFacebook = false;
                googleSignIn();
            }
        });

        // Initialize Facebook Login button
        LoginButton facebookSignInButton = (LoginButton) findViewById(R.id.facebookSignInButton);
        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithFacebook = true;
                facebookSignIn();
            }
        });
        callbackManager = CallbackManager.Factory.create();
        facebookSignInButton.setReadPermissions("email", "public_profile");
        facebookSignInButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                firebaseAuthWithFacebook(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                popupWindow.dismiss();
            }

            @Override
            public void onError(FacebookException error) {
                popupWindow.dismiss();
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(SignInActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void googleSignIn() {
        popupWindow = General.popLoadingBar(0.8, 0.8, this);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, REQ_GOOGLE_SIGN_IN);
    }

    private void facebookSignIn() {
        popupWindow = General.popLoadingBar(0.8, 0.8, this);
    }

    // If user doesn't exist -> start RegisterAct
    // If user exists -> confirm log in and finish
    private void checkUserExists(final FirebaseUser user) {
        Log.println(Log.INFO, "firebaseuser", "User FullName = "+user.getDisplayName());
        Query query = usersRef.orderByKey().equalTo(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    startUserRegistration(user);
                }
                else {
                    accountStatus = JUST_LOGGED_IN;
                    Toast.makeText(SignInActivity.this, R.string.logged_in, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void startUserRegistration(final FirebaseUser user) {
        if (signInWithFacebook) {
            String fbId = "";
            for (UserInfo profile : user.getProviderData()) {
                if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                    fbId = profile.getUid();
                    String photoUrl = "https://graph.facebook.com/" + fbId + "/picture?height=300";
                    startActivityForResult(
                            RegisterActivity.getStartIntent(
                                    SignInActivity.this,
                                    user.getUid(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    Uri.parse(photoUrl)
                            ),
                            REQ_REGISTER);
                    break;
                }
            }
        } else {
            startActivityForResult(
                    RegisterActivity.getStartIntent(
                            SignInActivity.this,
                            user.getUid(),
                            user.getDisplayName(),
                            user.getEmail(),
                            user.getPhotoUrl()
                    ),
                    REQ_REGISTER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_GOOGLE_SIGN_IN:  {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess()) {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                } else {
                    popupWindow.dismiss();
                }
                break;
            }

            case REQ_REGISTER: {
                Toast.makeText(getApplicationContext(), R.string.welcome, Toast.LENGTH_SHORT).show();
                General.accountStatus = General.JUST_LOGGED_IN;
                finish();
                break;
            }
        }

        // Facebook login case
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user.
                        // If sign in succeeds the auth state listener will be notified

                        if (!task.isSuccessful()) {
                            Log.w("SIGN IN", "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, R.string.error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void firebaseAuthWithFacebook(AccessToken token) {
        Log.d(TAG, "firebaseAuthWithFacebook:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private FirebaseAuth.AuthStateListener setUpAuthStateListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User signed in
                    currentUserId = user.getUid();

                    checkUserExists(user); //creates user in case it doesn't exist
                } else {
                    // User signed out
                    Log.d("SIGNED OUT", "onAuthStateChanged:signed_out");
                }

                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        };
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
    public void onBackPressed() {
        if (popupWindow != null && !popupWindow.isShowing()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accountStatus > 0) //If Logged in
            popupWindow.dismiss();
    }
}
