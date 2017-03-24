package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static com.explorify.xplore.xplore_demo.General.*;


/**
 * Created by Nika on 11/9/2016.
 */

public class FirstFragment extends Fragment {

    private View myView;
    private ImageView profileImage;
    private TextView fname, lname, age_text, tel, email;
    private ProgressBar progressBar;
    private Long tempTimeStamp;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.profile_layout, container, false);

        progressBar = (ProgressBar) myView.findViewById(R.id.imageProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        profileImage = (ImageView) myView.findViewById(R.id.user_profile_image);
        fname = (TextView) myView.findViewById(R.id.user_fname_text);
        lname = (TextView) myView.findViewById(R.id.user_lname_text);
        age_text = (TextView) myView.findViewById(R.id.user_age_text);
        tel = (TextView) myView.findViewById(R.id.user_tel_text);
        email = (TextView) myView.findViewById(R.id.user_email_text);

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity();

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

    private void showUserInfo()
    {
        //Gets the user info from database and loads them into views
        //===============
        final DatabaseReference DBref = FirebaseDatabase.getInstance().getReference();
        Query query = DBref.child("users").orderByKey().equalTo(currentUserId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
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
}
