package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.util.Log;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by nikao on 3/1/2017.
 */

public class SearchUsersActivity extends Activity implements EditText.OnEditorActionListener{

    private final String DB_FNAME_TAG = "fname";
    private final String DB_LNAME_TAG = "lname";

    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;

    private EditText searchBar;
    private ListView listView;
    private ProgressBar progressBar;
    private String searchQuery;
    private boolean firstLoad, requestCancelled, fnameNotFound;

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
        progressBar.setVisibility(View.INVISIBLE);
        //tempUserList.clear();
        userList.clear();
        firstLoad = true;
        userCounter = 0;
        fnameNotFound = false;
        requestCancelled = false;
        dbRef = userDB.getReference().getRef();
    }

    int userCounter = 0;

    private void LoadDataWithFullName(final String fname, final String lname)
    {
        Query fnameQuery = dbRef.orderByChild(DB_FNAME_TAG).startAt(fname).endAt(fname+"\uf8ff");
        fnameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    User tempUser;
                    for(DataSnapshot userSnapshot : dataSnapshot.getChildren())
                    {
                        tempUser = userSnapshot.getValue(User.class);
                        userList.add(tempUser);
                    }
                }
                else
                    fnameNotFound = true;

                Log.println(Log.INFO, "BREJK", "Starting LoadDataWithlastName");

                LoadDataWithLastName(lname);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { requestCancelled = true; }
        });
    }

    private void LoadDataWithLastName(final String lname)
    {
        Query lnameQuery = dbRef.orderByChild(DB_LNAME_TAG).startAt(lname).endAt(lname+"\uf8ff");
        lnameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User tempUser;
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        tempUser = userSnapshot.getValue(User.class);
                        userList.add(tempUser);
                    }
                } else if (fnameNotFound)
                    NothingFound();

                PopulateButtonList();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { requestCancelled = true; }
        });
    }

    private void PopulateButtonList()
    {
        for(int i = 0; i<userList.size(); i++)
        {
            for(int j = 0; j<userList.size(); j++)
            {
                if(i != j && userList.get(j).getId() == userList.get(i).getId())
                {
                    userList.remove(j);
                    break;
                }
            }
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
            super(SearchUsersActivity.this, R.layout.group_list_item, userList);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getLayoutInflater()
                        .inflate(R.layout.user_list_item, parent, false);
            }
            final User currentUser = userList.get(position);

            //Loading Data///////////////////////////////////////////////////////////////////////

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
                    CreateGroupFragment.memberIds.add(currentUser.getId());
                    Toast.makeText(SearchUsersActivity.this,
                            "User "+ currentUser.getFname()+" "+currentUser.getLname()+" was added to your group.",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return itemView;
        }
    }

    private String FirstLetterUpper(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        progressBar.setVisibility(View.VISIBLE);

        userList.clear();
        answerList.clear();

        searchQuery = v.getText().toString().toLowerCase();

        //Check here for search types

        //if searchtype == name then
        String fname_search, lname_search;
        if(searchQuery.contains(" "))
        {
            String[] parts = searchQuery.split(" ",2);
            fname_search = FirstLetterUpper(parts[0]);
            lname_search = FirstLetterUpper(parts[1]);
        }
        else
        {
            fname_search = FirstLetterUpper(searchQuery); //this searches in both fname and lname fields in db
            lname_search = fname_search; //performance optimization ;^)
        }

        LoadDataWithFullName(fname_search, lname_search);
        //endif

        return false;
    }
}
