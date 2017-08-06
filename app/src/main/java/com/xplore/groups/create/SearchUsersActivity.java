package com.xplore.groups.create;

import android.app.Activity;
import android.content.Context;
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
import com.xplore.General;
import com.xplore.ImageUtil;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.user.User;

import java.util.ArrayList;


/**
 * Created by Nikaoto on 3/1/2017.
 *
 * არწერა:
 * ეს მოქმედება იხსნება როდესაც მომხარებელი ეძებს თანამოლაშქრეებს რომ ჯფუფში დაპატიჟოს
 * (CreateGroupActivity და GroupInfoActivity - დან)
 *
 * Description:
 * This activity opens up when the user wants to invite other hikers to the group (from
 * CreateGroupActivity and GroupInfoActivity)
 *
 */

public class SearchUsersActivity extends Activity implements EditText.OnEditorActionListener{

    private final String FIREBASE_FNAME_TAG = "fname";
    private final String FIREBASE_LNAME_TAG = "lname";
    private final DatabaseReference firebaseUsersRef
            = FirebaseDatabase.getInstance().getReference().child("users");

    private ArrayList<User> userList = new ArrayList<>(); //replace with UserButtons
    private ArrayList<String> invitedMemberIds = new ArrayList<>(); //Ids of already invited members + the ones we invite

    private ListView listView;
    private ProgressBar progressBar;

    private boolean dataFound;
    private boolean memberAdded;

    public static Intent getStartIntent(Context context, ArrayList<String> invitedMemberIds) {
        return new Intent(context, SearchUsersActivity.class)
                .putExtra("invitedMemberIds", invitedMemberIds);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);

        if (getIntent().getStringArrayListExtra("invitedMemberIds") != null) {
            invitedMemberIds = getIntent().getStringArrayListExtra("invitedMemberIds");
        }

        TimeManager.Companion.refreshGlobalTimeStamp();

        listView = (ListView) findViewById(R.id.resultsListView);
        progressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        EditText searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.activity_search_users_title);
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
                    }
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

/*    private void nothingFound() {
        Toast.makeText(SearchUsersActivity.this, R.string.search_no_results,
                Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.INVISIBLE);
    }*/

    private class UserListAdapter extends ArrayAdapter<User> { //change to userbutton

        private final ArrayList<User> userList;

        public UserListAdapter(ArrayList<User> userList) {
            super(SearchUsersActivity.this, R.layout.user_list_item, userList);
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
                    .transform(ImageUtil.smallCircle(getContext()))
                    .into(userImage);

            //Configuring Clicks
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userAlreadyInvited(currentUser.getId())) {
                        Toast.makeText(SearchUsersActivity.this, R.string.member_already_added,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        memberAdded = true;
                        invitedMemberIds.add(currentUser.getId());
                        Toast.makeText(SearchUsersActivity.this,R.string.member_added,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return itemView;
        }
    }

    public boolean userAlreadyInvited(String userId) {
        return invitedMemberIds.contains(userId);
    }

    @Override
    public void onBackPressed() {
        if (memberAdded) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("invitedMemberIds", invitedMemberIds);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
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
            //Search with 1 -> 2
            loadUsersWithFullName(firstLetterUpper(parts[0]), firstLetterUpper(parts[1]), true);
            loadUsersWithFullName(parts[0], parts[1], true);
            //Now the other way around (2 -> 1)
            loadUsersWithFullName(firstLetterUpper(parts[1]), firstLetterUpper(parts[0]), true);
            loadUsersWithFullName(parts[1], parts[0], true);
        } else {
            loadUsersWithTag(searchQuery, FIREBASE_FNAME_TAG, true,
                    searchQuery, FIREBASE_LNAME_TAG);

            loadUsersWithTag(firstLetterUpper(searchQuery), FIREBASE_FNAME_TAG, true,
                    firstLetterUpper(searchQuery), FIREBASE_LNAME_TAG);
        }

        return false;
    }
}
