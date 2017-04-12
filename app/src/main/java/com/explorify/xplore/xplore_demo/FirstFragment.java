package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static com.explorify.xplore.xplore_demo.General.*;
import static com.explorify.xplore.xplore_demo.GoogleSignInActivity.*;
import static com.explorify.xplore.xplore_demo.GoogleSignInActivity.googleApiClient;


/**
 * Created by Nika on 11/9/2016.
 */

public class FirstFragment extends Fragment implements View.OnClickListener {

    private View myView;
    private ImageView profileImage;
    private TextView rep_text, fname, lname, age_text, tel, email;
    private ProgressBar progressBar;
    private Button LogOutBtn;
    private Long tempTimeStamp;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.profile_layout, container, false);

        Authorize();

        progressBar = (ProgressBar) myView.findViewById(R.id.imageProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        profileImage = (ImageView) myView.findViewById(R.id.user_profile_image);
        rep_text = (TextView) myView.findViewById(R.id.user_rep_text);
        fname = (TextView) myView.findViewById(R.id.user_fname_text);
        lname = (TextView) myView.findViewById(R.id.user_lname_text);
        age_text = (TextView) myView.findViewById(R.id.user_age_text);
        tel = (TextView) myView.findViewById(R.id.user_tel_text);
        email = (TextView) myView.findViewById(R.id.user_email_text);

        LogOutBtn = (Button) myView.findViewById(R.id.log_out_btn);
        LogOutBtn.setOnClickListener(this);

        if(accountStatus == 0)
            LogOutBtn.setEnabled(false);

        return myView;
    }

    private void Authorize()
    {
        //Connect Google Api
        if(googleApiClient == null) {
            googleApiClient = BuildGoogleApiClient();
            googleApiClient.connect();
        }
        else
            googleApiClient.connect();


        //Get Auth Instance
        auth = FirebaseAuth.getInstance();
    }

    private GoogleApiClient BuildGoogleApiClient()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        return new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();

        if(accountStatus != 0)
            LogOutBtn.setEnabled(true);

        if(!isNetConnected(context)){
            createNetErrorDialog(context);
        }
        else if (!isUserSignedIn()) {
            progressBar.setVisibility(View.INVISIBLE);
            popSignInMenu(0.8, 0.6, false, myView, getActivity());
        }
        else{
            showUserInfo();
        }
    }

    @Override
    public void onClick(View view) { //Log Out
        LogOut();
    }

    //TODO this is a hack. Create SignInActivity and use LogIn() and LogOut() from that activity
    public void LogOut() {
        auth.signOut();
        Auth.GoogleSignInApi.signOut(GoogleSignInActivity.googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess() && accountStatus > 0 ) {
                            currentUserId = "";
                            accountStatus = 0;
                            Toast.makeText(getActivity(), "Logged Out", Toast.LENGTH_SHORT).show();

                            //Refresh Fragment
                            Fragment currentFragment = FirstFragment.this;
                            FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                            fragTransaction.detach(currentFragment);
                            fragTransaction.attach(currentFragment);
                            fragTransaction.commit();
                        }
                    }
                }
        );
    }

    private void showUserInfo()
    {
        //Gets the user info from database and loads them into views
        //===============
        final DatabaseReference DBref = FirebaseDatabase.getInstance().getReference();
        Query query = DBref.child("users").orderByKey().equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && getActivity() !=null) {
                    User tempUser = new User();
                    tempUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                        //Loading Image
                        Picasso.with(getActivity())
                                .load(tempUser.getProfile_picture_url())
                                .transform(new RoundedCornersTransformation(
                                        getResources().getInteger(R.integer.pic_big_angle),
                                        getResources().getInteger(R.integer.pic_big_margin)))
                                .into(profileImage);
                    //Loading Texts
                    fname.setText(tempUser.getFname());
                    lname.setText(tempUser.getLname());
                    rep_text.setText(String.valueOf(tempUser.getReputation()));

                    //Update current server time
                    Map<String, Object> dateValue = new HashMap<>();
                    dateValue.put("timestamp", ServerValue.TIMESTAMP);
                    DBref.child("date").setValue(dateValue);
                    Query query = DBref.child("date").child("timestamp");

                    final User finalTempUser = tempUser;

                    //get server time and calculate age
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tempTimeStamp = dataSnapshot.getValue(Long.class);

                            //String ageLabel = getString(R.string.age);
                            age_text.setText(getString(R.string.age) + ": "
                                    + calculateAge(tempTimeStamp, finalTempUser.getBirth_date()));

                            progressBar.setVisibility(View.INVISIBLE);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });

                    tel.setText(getString(R.string.tel)+": "+tempUser.getTel_num());
                    email.setText(tempUser.getEmail());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }});
    }

    @Override
    public void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }
}
