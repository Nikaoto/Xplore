package com.xplore.account;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.xplore.ApiManager;
import com.xplore.General;
import com.xplore.MainActivity;
import com.xplore.R;
import com.xplore.account.registration.RegistrationActivity;
import com.xplore.base.BaseAppCompatActivity;
import com.xplore.util.FirebaseUtil;

import static com.xplore.General.JUST_LOGGED_IN;
import static com.xplore.General.NOT_LOGGED_IN;
import static com.xplore.General.accountStatus;
import static com.xplore.General.currentUserId;
import static com.xplore.util.FirebaseUtil.FB_PROFILE_PIC_HEIGHT;
import static com.xplore.util.FirebaseUtil.FB_PROFILE_PIC_WIDTH;
import static com.xplore.util.FirebaseUtil.F_EMAIL;
import static com.xplore.util.FirebaseUtil.usersRef;

/**
 * Created by Nikaoto on 3/8/2017.
 *
 * Handles sign in with Google and Facebook.
 * Launches RegisterAct if new user.
 *
 */


public class SignInActivity extends BaseAppCompatActivity {

    public static String ARG_SHOULD_LAUNCH_MAIN_ACT = "shouldLaunchMainAct";
    public static Intent getStartIntent(Context context, boolean shouldLaunchMainAct) {
        return new Intent(context, SignInActivity.class)
                .putExtra(ARG_SHOULD_LAUNCH_MAIN_ACT, shouldLaunchMainAct);
    }

    private static String TAG = "brejk";

    // TODO: upload bare user before starting registration act so if that fails, users can change info with 'edit profile'

    private boolean signInWithFacebook = false;

    private static final int REQ_GOOGLE_SIGN_IN = 1;

    private static final int REQ_REGISTER = 3;

    // For google login
    private GoogleApiClient googleApiClient;

    // For facebook login
    CallbackManager callbackManager;

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private FirebaseAuth.AuthStateListener authListener = setUpAuthStateListener();
    private PopupWindow popupWindow;

    private EditText emailEditText;
    private EditText passwordEditText;

    private boolean shouldCheckUserExists = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        boolean shouldShowBackBtn = !getIntent().getBooleanExtra(ARG_SHOULD_LAUNCH_MAIN_ACT, false);
        if (shouldShowBackBtn) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        setTitle(R.string.activity_authorization_title);

        // Building Google Api Client
        googleApiClient = ApiManager.getGoogleAuthApiClient(this);

        // Initialize login fields
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        // Initialize Xplore Login button
        Button xploreSignInButton = (Button) findViewById(R.id.xploreSignInButton);
        xploreSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (General.isNetConnected(SignInActivity.this)) {
                    toastLoading();
                    signInWithFacebook = false;
                    xploreSignIn();
                } else {
                    General.createNetErrorDialog(SignInActivity.this);
                }
            }
        });

        // Initialize Google Login button
        SignInButton googleSignInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (General.isNetConnected(SignInActivity.this)) {
                    toastLoading();
                    signInWithFacebook = false;
                    googleSignIn();
                } else {
                    General.createNetErrorDialog(SignInActivity.this);
                }
            }
        });

        // Initialize Facebook Login button
        LoginButton facebookSignInButton = (LoginButton) findViewById(R.id.facebookSignInButton);
        facebookSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (General.isNetConnected(SignInActivity.this)) {
                    toastLoading();
                    signInWithFacebook = true;
                    facebookSignIn();
                } else {
                    General.createNetErrorDialog(SignInActivity.this);
                }
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
                dismissPopupWindow();
            }

            @Override
            public void onError(FacebookException error) {
                dismissPopupWindow();
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(SignInActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Gets fields and initiates signin or register accordingly
    private void xploreSignIn() {
        if (loginFieldsValid()) {
            final String email = emailEditText.getText().toString();
            final String password = passwordEditText.getText().toString();
            Query query = usersRef.orderByChild(F_EMAIL).equalTo(email);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        signInXploreUser(email, password);
                    } else {
                        createXploreUser(email, password);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {}
            });
        }
    }

    // Signs in an existing user to Xplore
    private void signInXploreUser(final String email, final String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "xplore auth: sign in successful");
                            // Leave empty. Firebase auth state listener will handle it
                        } else {
                            Toast.makeText(SignInActivity.this, R.string.incorrect_credentials,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Creates Xplore user in firebase (from xplore login) and starts registration
    private void createXploreUser(final String email, final String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(SignInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "xplore auth: user created");
                            // Auth state listener will handle it
                        } else {
                            Log.i(TAG, "xplore auth: user creation failed");

                            Toast.makeText(SignInActivity.this, R.string.error, Toast.LENGTH_SHORT)
                                    .show();
                        }
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

    private void toastLoading() {
        Toast.makeText(SignInActivity.this, R.string.loading, Toast.LENGTH_LONG).show();
    }

    // If user doesn't exist -> start RegisterAct
    // If user exists -> confirm log in and finish
    private void checkUserExists(final FirebaseUser user) {
        // Uid to find if user exists
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
                    finishActivity();
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
                    String photoUrl = "https://graph.facebook.com/" + fbId
                            + "/picture?height=" + FB_PROFILE_PIC_HEIGHT
                            + "&width=" + FB_PROFILE_PIC_WIDTH;
                    startActivityForResult(
                            RegistrationActivity.newIntent(
                                    SignInActivity.this,
                                    user.getUid(),
                                    user.getDisplayName(),
                                    user.getEmail(),
                                    photoUrl
                            ),
                            REQ_REGISTER);
                    break;
                }
            }
        } else {
            String userPhotoUrl = "";
            if (user.getPhotoUrl() != null) {
                userPhotoUrl = user.getPhotoUrl().toString();
            }

            startActivityForResult(
                    RegistrationActivity.newIntent(
                            SignInActivity.this,
                            user.getUid(),
                            user.getDisplayName(),
                            user.getEmail(),
                            userPhotoUrl
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
                    dismissPopupWindow();
                }
                break;
            }

            case REQ_REGISTER: {
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), R.string.welcome, Toast.LENGTH_SHORT).show();
                    General.accountStatus = General.JUST_LOGGED_IN;
                    finishActivity();
                } else {
                    shouldCheckUserExists = false;
                    googleApiClient.connect();
                    googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(@Nullable Bundle bundle) {
                            FirebaseUtil.forceLogOut(SignInActivity.this, googleApiClient);
                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            Toast.makeText(SignInActivity.this, R.string.error, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                }
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
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            // FB email might already be registered in Xplore
                            fbEmailTakenDialog(SignInActivity.this).show();

                        }
                    }
                });
    }

    // Shows big dialog saying email is already taken by some other service
    private AlertDialog fbEmailTakenDialog(Context context) {
        return new AlertDialog.Builder(context)
                .setTitle(R.string.error)
                .setMessage(R.string.facebook_email_in_use)
                .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dismissPopupWindow();
                    }
                }).create();
    }


    // Called when authorized using any service
    private FirebaseAuth.AuthStateListener setUpAuthStateListener() {
        return new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null & shouldCheckUserExists) {
                    // User signed in
                    Log.i("sign-in-act", "checking user exists");
                    currentUserId = user.getUid();

                    checkUserExists(user); // creates user in case it doesn't exist
                } else {
                    // User signed out
                    Log.d("SIGNED OUT", "onAuthStateChanged:signed_out");
                    shouldCheckUserExists = true;
                    accountStatus = NOT_LOGGED_IN;
                }

                dismissPopupWindow();
            }
        };
    }

    // Validates input fields and returns respective errors or true
    private boolean loginFieldsValid() {
        makeBorderGreen(emailEditText);
        makeBorderGreen(passwordEditText);

        if (emailEditText.getText().toString().isEmpty()) {
            makeBorderRed(emailEditText);
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (passwordEditText.getText().toString().isEmpty()) {
            makeBorderRed(passwordEditText);
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!General.isValidEmail(emailEditText.getText())) {
            makeBorderRed(emailEditText);
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return false;
        } else if (passwordEditText.length() < FirebaseUtil.MIN_PASS_LENGTH) {
            makeBorderRed(passwordEditText);
            Toast.makeText(this, R.string.error_invalid_password, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void finishActivity() {
        General.setRegistrationFinished(this, true);
        finish();
        if (getIntent().getBooleanExtra(ARG_SHOULD_LAUNCH_MAIN_ACT, false)) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void makeBorderRed(EditText et) {
        et.setBackgroundResource(R.drawable.edit_text_border_red);
    }

    private void makeBorderGreen(EditText et) {
        et.setBackgroundResource(R.drawable.edit_text_border);
    }

    // Safely dismisses the popup window
    private void dismissPopupWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
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
        if (popupWindow == null || !popupWindow.isShowing()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (General.isUserLoggedIn()) {
            if (popupWindow != null && popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        }
    }
}
