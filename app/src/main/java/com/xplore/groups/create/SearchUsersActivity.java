package com.xplore.groups.create;

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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xplore.CircleTransformation;
import com.xplore.General;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.user.User;

import java.util.ArrayList;

import static com.xplore.groups.create.CreateGroupActivity.invitedMembers;


/**
 * Created by Nikaoto on 3/1/2017.
 *
 * არწერა:
 * ეს მოქმედება იხსნება როდესაც მომხარებელი ჯგუფს ქმნის სა იწყებს თანამოლაშქრეების ძებნას რომ ჩაამატოს.
 *
 * Description:
 * This activity opens up when the user is creating a group and starts searching for members to add.
 *
 */

public class SearchUsersActivity extends Activity implements EditText.OnEditorActionListener{

    private final String FIREBASE_FNAME_TAG = "fname";
    private final String FIREBASE_LNAME_TAG = "lname";
    private final DatabaseReference firebaseUsersRef
            = FirebaseDatabase.getInstance().getReference().child("users");

    private ArrayList<User> userList = new ArrayList<>(); //replace with UserButtons

    /*
    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;

    DatabaseReference dbRef;
    FirebaseApp userBaseApp;
    FirebaseOptions userBaseOptions;
    */

    private ListView listView;
    private ProgressBar progressBar;

    private boolean dataFound;
    private boolean memberAdded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        TimeManager.Companion.refreshGlobalTimeStamp();

        listView = (ListView) findViewById(R.id.resultsListView);
        progressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        EditText searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_users);
        searchBar.setOnEditorActionListener(this);

        //buildUserBase();
        prepareForSearch();
    }

    private void prepareForSearch() {
        memberAdded = false;
        progressBar.setVisibility(View.INVISIBLE);
        userList.clear();
    }

    //search by last names in firebase database (because fname collisions are more frequent) and then filter results with first names
    private void loadUsersWithFullName(final String fname, final String lname, final boolean displayData) {
        //Sorting by first name
        Query fnameQuery = firebaseUsersRef.orderByChild(FIREBASE_LNAME_TAG).startAt(lname).endAt(lname+"\uf8ff");
        fnameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataFound = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        User tempUser = userSnapshot.getValue(User.class);
                        if (tempUser.getFname().toLowerCase().contains(fname.toLowerCase())) {
                            tempUser = userSnapshot.getValue(User.class);
                            tempUser.setId(userSnapshot.getKey());
                            userList.add(tempUser);
                            dataFound = true;
                        }
                    }
                    if (dataFound && displayData) {
                        displayUserList();
                    } else if (!dataFound) {
                        nothingFound();
                    }
                } else {
                    nothingFound();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void loadUsersWithTag(final String query, final String tag, final boolean resummon,
                                  final String query2, final String tag2) {
        //if resummon == true, do NOT display data
        Query dbQuery = firebaseUsersRef.orderByChild(tag).startAt(query).endAt(query+"\uf8ff");
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
                } else if (!resummon && !dataFound) {
                    nothingFound();
                } else if(resummon) {
                    dataFound = false;
                }

                if (resummon) {
                    loadUsersWithTag(query2, tag2, false, null, null);
                } else if (dataFound) {
                    displayUserList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    //Filters duplicate users from given array and returns filtered list
    // Also removes the currently logged in user from the list
    private ArrayList<User> filterDuplicates(ArrayList<User> list) {
        boolean foundDuplicate;
        ArrayList<User> ansList = new ArrayList<>();
        //Filtering duplicates into ansList
        for(int i = 0; i < list.size(); i++) {
            foundDuplicate = false;
            //Checking if list[i] is a duplicate
            for(int j = i; j < list.size(); j++) {
                if(i!= j && list.get(j).getId().equals(list.get(i).getId())) {
                    foundDuplicate = true;
                    break;
                }
            }
            //If we have a user that is not a duplicate and not the user itself
            if(!foundDuplicate && (!(list.get(i).getId().equals(General.currentUserId)))) {
                ansList.add(list.get(i));
            }
        }
        return ansList;
    }

    private void displayUserList() {
        ArrayAdapter<User> adapter = new UserListAdapter(filterDuplicates(userList)); //change User to UserButtons
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void nothingFound() {
        Toast.makeText(SearchUsersActivity.this, R.string.search_no_results,
                Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class UserListAdapter extends ArrayAdapter<User> { //change to userbutton

        private final int imgSize;
        private final ArrayList<User> userList;

        public UserListAdapter(ArrayList<User> userList) {
            super(SearchUsersActivity.this, R.layout.group_list_item, userList);
            imgSize = Math.round(getResources().getDimension(R.dimen.user_profile_image_small_size));
            this.userList = userList;
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

            //Reputation
            TextView rep_text = (TextView) itemView.findViewById(R.id.user_rep_text_combined);
            rep_text.setText(currentUser.getReputation() + " " +
                    getResources().getString(R.string.reputation));

            //First Name
            TextView fname_text = (TextView) itemView.findViewById(R.id.user_fname_text);
            fname_text.setText(currentUser.getFname());

            //Last Name
            TextView lname_text = (TextView) itemView.findViewById(R.id.user_lname_text);
            lname_text.setText(currentUser.getLname());

            //Age
            TextView age_text = (TextView) itemView.findViewById(R.id.user_age_text);
            age_text.setText(getResources().getString(R.string.age) +": "+
                    General.calculateAge(TimeManager.Companion.getGlobalTimeStamp(), currentUser.getBirth_date()));

            //Profile Picture
            final ImageView userImage = (ImageView) itemView.findViewById(R.id.user_profile_image);
            String userImageRef = currentUser.getProfile_picture_url();
            Picasso.with(getContext())
                    .load(userImageRef)
                    .transform(new CircleTransformation(imgSize, imgSize))
                    .into(userImage);

            //Configuring Clicks
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userAlreadyInvited(currentUser)) {
                        Toast.makeText(SearchUsersActivity.this, R.string.member_already_added,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        memberAdded = true;
                        invitedMembers.add(currentUser);
                        Toast.makeText(SearchUsersActivity.this,R.string.member_added,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return itemView;
        }
    }

    public boolean userAlreadyInvited(User user) {
        for(User u : invitedMembers) {
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
    }

    //returns given string with the first letter in uppercase
    private String firstLetterUpper(String str) {
        if(str.length() == 0)
            return str.toUpperCase();

        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        progressBar.setVisibility(View.VISIBLE);

        userList.clear();
        dataFound = false;

        String searchQuery = v.getText().toString().toLowerCase();

        ////TODO search types (search by..)

        //If searchQuery has both first and last names
        if(searchQuery.contains(" ")) {
            String[] parts = searchQuery.split(" ",2);
            loadUsersWithFullName(firstLetterUpper(parts[0]), firstLetterUpper(parts[1]), true);

            //TODO add fname search with lname filter (reverse loadUsersWithFullName, in case they type lname first, then fname)
        } else {
            loadUsersWithTag(firstLetterUpper(searchQuery), FIREBASE_FNAME_TAG, true,
                    firstLetterUpper(searchQuery), FIREBASE_LNAME_TAG);
        }

        return false;
    }
}