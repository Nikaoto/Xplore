package com.xplore.groups.create;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;
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
import com.xplore.base.BaseAppCompatActivity;
import com.xplore.user.UserCard;
import com.xplore.util.ImageUtil;
import com.xplore.R;
import com.xplore.TimeManager;

import java.util.ArrayList;

import static com.xplore.util.FirebaseUtil.F_FNAME;
import static com.xplore.util.FirebaseUtil.F_LNAME;
import static com.xplore.util.FirebaseUtil.F_USERS;


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

//TODO replace the adapter with one used when ONLY inviting members through leaderControls

public class SearchUsersActivity extends BaseAppCompatActivity implements EditText.OnEditorActionListener{

    public static final String ARG_INVITED_MEMBER_IDS = "invitedMemberIds";

    public static Intent getStartIntent(Context context, ArrayList<String> invitedMemberIds) {
        return new Intent(context, SearchUsersActivity.class)
                .putExtra(ARG_INVITED_MEMBER_IDS, invitedMemberIds);
    }

    private final DatabaseReference firebaseUsersRef
            = FirebaseDatabase.getInstance().getReference().child(F_USERS);
    private ArrayList<UserCard> userList = new ArrayList<>();

    // Contains ids of already invited members + the ones the user invites in this session
    private ArrayList<String> invitedMemberIds = new ArrayList<>();
    private ListView listView;

    private ProgressBar progressBar;
    private boolean dataFound;

    private boolean memberAdded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        setTitle(R.string.invite_members);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getStringArrayListExtra(ARG_INVITED_MEMBER_IDS) != null) {
            invitedMemberIds = getIntent().getStringArrayListExtra(ARG_INVITED_MEMBER_IDS);
        }

        TimeManager.refreshGlobalTimeStamp();

        listView = (ListView) findViewById(R.id.resultsListView);
        progressBar = (ProgressBar) findViewById(R.id.searchProgressBar);

        EditText searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.activity_search_users_title);
        searchBar.setOnEditorActionListener(this);

        prepareForSearch();
    }

    private void prepareForSearch() {
        memberAdded = false;
        progressBar.setVisibility(View.INVISIBLE);
        userList.clear();
    }

    // Search by last names in firebase database (because fname collisions are more frequent) and then filter results with first names
    private void loadUsersWithFullName(final String fname, final String lname, final boolean displayData) {
        // Sorting by first name
        Query fnameQuery = firebaseUsersRef.orderByChild(F_LNAME).startAt(lname).endAt(lname+"\uf8ff");
        fnameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataFound = false;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        UserCard tempUser = userSnapshot.getValue(UserCard.class);
                        if (tempUser != null
                                && tempUser.getFname().toLowerCase().contains(fname.toLowerCase())) {
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

    /*
    *  Searches queries with given tags.
    *  Do not change this code in any way. Implementing a for-each loop is a great idea, but it
    *  won't work because the requests are sent much quicker than firebase is able to respond, so
    *  it ends up responding too late to every request. You can't display the correct results
    *  without sending a request after a response has been received from the previous request.
    *  Requests need to be sent one step at a time. This is crap, I know, but we can't do anything
    *  about it until we get a normal backend for this app.
    */
    private void loadUsersWithTag(final String query, final String tag, final boolean resummon,
                                  final String query2, final String tag2) {
        // if resummon == true, do NOT display data yet
        Query dbQuery = firebaseUsersRef.orderByChild(tag).startAt(query).endAt(query+"\uf8ff");
        dbQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataFound = true;
                    UserCard tempUser;
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        tempUser = userSnapshot.getValue(UserCard.class);
                        if (tempUser != null) {
                            tempUser.setId(userSnapshot.getKey());
                            userList.add(tempUser);
                        }
                    }
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

    // Filters duplicate UserCards from given array and returns filtered list
    // Also removes the currently logged in user from the list
    private ArrayList<UserCard> filterDuplicates(ArrayList<UserCard> list) {
        ArrayList<UserCard> ansList = new ArrayList<>();
        boolean foundDuplicate;
        // Filtering duplicates into ansList
        for(int i = 0; i < list.size(); i++) {
            if (!list.get(i).getId().equals(General.currentUserId)) {
                foundDuplicate = false;
                // Checking if list[i] is a duplicate
                for (int j = i + 1; j < list.size(); j++) {
                    if (list.get(j).getId().equals(list.get(i).getId())) {
                        foundDuplicate = true;
                        break;
                    }
                }
                // If we have a user that is not a duplicate and not the user itself
                if (!foundDuplicate) {
                    ansList.add(list.get(i));
                }
            }
        }
        return ansList;
    }

    private void displayUserList() {
        ArrayAdapter<UserCard> adapter = new UserListAdapter(filterDuplicates(userList));
        listView.setAdapter(adapter);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private class UserListAdapter extends ArrayAdapter<UserCard> {

        private final ArrayList<UserCard> userList;

        UserListAdapter(ArrayList<UserCard> userList) {
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

            final UserCard currentUser = userList.get(position);

            // Loading Data

            // Reputation
            TextView rep_text = (TextView) itemView.findViewById(R.id.combinedReputationTextView);
            rep_text.setText(currentUser.getReputation() + " " +
                    getResources().getString(R.string.reputation));

            // First Name
            TextView fname_text = (TextView) itemView.findViewById(R.id.fullNameTextView);
            fname_text.setText(currentUser.getFullName());

            // Profile Picture
            final ImageView userImage = (ImageView) itemView.findViewById(R.id.profileImageView);
            String userImageRef = currentUser.getProfile_picture_url();
            Picasso.with(getContext())
                    .load(userImageRef)
                    .transform(ImageUtil.smallCircle(getContext()))
                    .into(userImage);

            // Configuring Clicks
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
        General.hideKeyboard(this);
        if (memberAdded) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ARG_INVITED_MEMBER_IDS, invitedMemberIds);
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }

    // Returns given string with the first letter in uppercase
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

        //TODO search types (search by..)

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
            loadUsersWithTag(searchQuery, F_FNAME, true, searchQuery, F_LNAME);

            loadUsersWithTag(firstLetterUpper(searchQuery), F_FNAME, true,
                    firstLetterUpper(searchQuery), F_LNAME);
        }

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
