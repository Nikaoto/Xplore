package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

import static com.explorify.xplore.xplore_demo.CreateGroupFragment.invitedMembers;


/**
 * Created by nikao on 3/1/2017.
 */

public class SearchUsersActivity extends Activity implements EditText.OnEditorActionListener{

    private final String DB_FNAME_TAG = "fname";
    private final String DB_LNAME_TAG = "lname";

    private boolean memberAdded;
    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;

    private EditText searchBar;
    private ListView listView;
    private ProgressBar progressBar;
    private String searchQuery;
    private boolean firstLoad, requestCancelled, fnameNotFound, dataFound;

    private ArrayList<User> userList= new ArrayList<>(); //replace with UserButtons
    private ArrayList<User> answerList= new ArrayList<>(); //replace with UserButtons

    DatabaseReference dbRef;
    FirebaseApp userBaseApp;
    FirebaseOptions userBaseOptions;
    FirebaseDatabase userDB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);

        listView = (ListView) findViewById(R.id.resultslist);
        progressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setHint(R.string.search_users);
        searchBar.setOnEditorActionListener(this);

        Authorize();
        buildUserBase();
        PreLoadData();
    }

    private void Authorize()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    private void buildUserBase()
    {
        USERBASE_KEY = getResources().getString(R.string.user_firebase_key);
        USERBASE_APPID = getResources().getString(R.string.firebase_appid);
        USERBASE_URL = getResources().getString(R.string.user_firebase_url);

        userBaseOptions = new FirebaseOptions.Builder()
                .setApiKey(USERBASE_KEY)
                .setApplicationId(USERBASE_APPID)
                .setDatabaseUrl(USERBASE_URL)
                .build();
        try {
            if (FirebaseApp.getApps(this).get(1).equals(null)) {
                FirebaseApp.initializeApp(this, userBaseOptions, "userbase");
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            FirebaseApp.initializeApp(this, userBaseOptions, "userbase");
        }
        userBaseApp = FirebaseApp.getInstance("userbase");
        userDB = FirebaseDatabase.getInstance(userBaseApp);
    }

    private void PreLoadData()
    {
        memberAdded = false;
        progressBar.setVisibility(View.INVISIBLE);
        answerList.clear();
        userList.clear();
        userList.clear();
        firstLoad = true;
        userCounter = 0;
        fnameNotFound = false;
        requestCancelled = false;
        dbRef = userDB.getReference().getRef();
    }

    int userCounter = 0;

    //search lname in db and filter results with fnames (because lname collisions are less frequent)
    private void LoadDataWithFullName(final String fname, final String lname, final boolean displayData)
    {
        Query fnameQuery = dbRef.orderByChild(DB_LNAME_TAG).startAt(lname).endAt(lname+"\uf8ff");
        fnameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataFound = false;
                if (dataSnapshot.exists()) {
                    User tempUser;
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        if (userSnapshot.getValue(User.class).getFname().toLowerCase().contains(fname.toLowerCase())) {
                            tempUser = userSnapshot.getValue(User.class);
                            tempUser.setId(userSnapshot.getKey());
                            userList.add(tempUser);
                            dataFound = true;
                        }
                    }
                    if (dataFound && displayData)
                        PopulateButtonList();
                    else if (!dataFound)
                        NothingFound();
                }
                else {
                    NothingFound();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { requestCancelled = true; }
        });
    }

    private void LoadDataWithTag(final String query, final String tag, final boolean resummon,
                                 final String query2, final String tag2)
    {
        //if resummon == true, do NOT display data
        Query dbQuery = dbRef.orderByChild(tag).startAt(query).endAt(query+"\uf8ff");
        dbQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataFound = true;
                    User tempUser;
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        tempUser = userSnapshot.getValue(User.class);
                        tempUser.setId(userSnapshot.getKey());
                        userList.add(tempUser);
                    }
                }
                else if (!resummon && !dataFound)
                    NothingFound();
                else if(resummon)
                    dataFound =  false;

                if (resummon)
                    LoadDataWithTag(query2, tag2, false, null, null);
                else if (dataFound)
                    PopulateButtonList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { requestCancelled = true; }
        });
    }

    private void PopulateButtonList()
    {
/*        for(User user : userList)
            Log.println(Log.INFO, "fname", user.getFname()+" id ="+user.getId());*/

        boolean foundDup;
        //filtering duplicates into answerList
        for(int i = 0; i<userList.size(); i++)
        {
            foundDup = false;

            for(int j = i; j<userList.size(); j++)
            {
                if(i!= j && userList.get(j).getId().equals(userList.get(i).getId())) {
                    foundDup = true;
                    break;
                }
            }

            if(!foundDup)
              answerList.add(userList.get(i));
        }

        ArrayAdapter<User> adapter = new UserListAdapter(); //change User to UserButtons
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void NothingFound()
    {
        Toast.makeText(SearchUsersActivity.this, R.string.search_no_results,
                Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class UserListAdapter extends ArrayAdapter<User> { //change to userbutton
        public UserListAdapter() {
            super(SearchUsersActivity.this, R.layout.group_list_item, answerList);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater()
                        .inflate(R.layout.user_list_item, parent, false);
            }
            final User currentUser = answerList.get(position);

            //Loading Data///////////////////////////////////////////////////////////////////////

            //Reputation
            TextView rep_text = (TextView) itemView.findViewById(R.id.user_rep_text_combined);
            rep_text.setText(currentUser.getReputation() + " " +
                    getResources().getString(R.string.reputation_caps));

            //First Name
            TextView fname_text = (TextView) itemView.findViewById(R.id.user_fname_text);
            fname_text.setText(currentUser.getFname());

            //Last Name
            TextView lname_text = (TextView) itemView.findViewById(R.id.user_lname_text);
            lname_text.setText(currentUser.getLname());

            //Age
            TextView age_text = (TextView) itemView.findViewById(R.id.user_age_text);
            age_text.setText(getResources().getString(R.string.age) +": "+currentUser.getAge());

            //Profile Picture
            final ImageView userImage = (ImageView) itemView.findViewById(R.id.user_profile_image);
            String userImageRef = currentUser.getProfile_picture_ref();
            Picasso.with(getContext())
                    .load(userImageRef)
                    .transform(new RoundedCornersTransformation(
                            getResources().getInteger(R.integer.pic_small_angle),
                            getResources().getInteger(R.integer.pic_small_margin)))
                    .into(userImage);

            //Configuring Clicks
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(UserAlreadyInvited(currentUser))
                        Toast.makeText(SearchUsersActivity.this, "Already Added m8", //TODO already added string
                                Toast.LENGTH_SHORT).show();
                    else {
                        memberAdded = true;
                        invitedMembers.add(currentUser);
                        Toast.makeText(SearchUsersActivity.this,
                                "User " + currentUser.getFname() + " " + currentUser.getLname() //TODO was added string
                                        + " was added to your group.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return itemView;
        }
    }

    public boolean UserAlreadyInvited(User user)
    {
        for(User u : invitedMembers)
        {
            if(u.getId().equals(user.getId()))
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("member_added", memberAdded);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
        //super.onBackPressed();
    }

    //returns given string with the first letter in uppercase
    private String FirstLetterUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        progressBar.setVisibility(View.VISIBLE);

        userList.clear();
        answerList.clear();
        dataFound = false;

        searchQuery = v.getText().toString().toLowerCase();

        //Check here for search types

        //if searchtype == name then
        String fname_search, lname_search;
        if(searchQuery.contains(" "))
        {
            String[] parts = searchQuery.split(" ",2);
            LoadDataWithFullName(FirstLetterUpper(parts[0]), FirstLetterUpper(parts[1]), true);

            //TODO add fname search with lname filter (reverse LoadDataWithFullName, in case they type lname first, then fname)
        }
        else
        {
            LoadDataWithTag(FirstLetterUpper(searchQuery), DB_FNAME_TAG, true,
                    FirstLetterUpper(searchQuery), DB_LNAME_TAG);
        }
        //endif

        return false;
    }
}
