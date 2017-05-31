package com.explorify.xplore.xplore;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by nikao on 2/8/2017.
 */

public class SearchGroupsFragment extends Fragment implements EditText.OnEditorActionListener {

/*    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;*/

    private final String JSON_START_DATE_TAG = "start_date";

    private String searchQuery, tempUserImageUrl;
    private boolean requestCancelled, firstLoad;
    private int leaderCounter;

    private View myView;
    private ListView list;
    private EditText searchBar;
    private ProgressBar progressBar;

    private List<Integer> resultID = new ArrayList<Integer>(); //TODO make search in groups
    private ArrayList<GroupButton> groupButtons = new ArrayList<>(); //changed to ArrayList from List (roll back if errors ensue)
    private ArrayList<Group> tempGroupList = new ArrayList<>();
    private Group tempGroup;

    /*FirebaseApp userBaseApp;
    FirebaseOptions userBaseOptions;
    FirebaseDatabase userDB;*/
    DatabaseReference DBref = FirebaseDatabase.getInstance().getReference();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.first_layout, container, false);

        //setting up listview
        list = (ListView) myView.findViewById(R.id.resultslist);

        //setting up searchbar
        searchBar = (EditText) myView.findViewById(R.id.search_bar);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_groups_hint);
        searchBar.setOnEditorActionListener(this);

        //setting up progressbar
        progressBar = (ProgressBar) myView.findViewById(R.id.searchProgressBar);

        if(!General.isNetConnected(getActivity()))
        {
            General.createNetErrorDialog(getActivity());
        }
        else {
            Authorize();
            //buildUserBase();
            PreLoadData();
            LoadData();
        }
        return myView;
    }

    private void Authorize()
    {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

/*    private void buildUserBase()
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
            if (FirebaseApp.getApps(getActivity()).get(1).equals(null)) {
                FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
        }
        userBaseApp = FirebaseApp.getInstance("userbase");
        userDB = FirebaseDatabase.getInstance(userBaseApp);
    }*/

    private void PreLoadData()
    {
        progressBar.setVisibility(View.VISIBLE);
        tempGroup = new Group();
        tempGroupList.clear();
        groupButtons.clear();
        tempUserImageUrl = "";
        firstLoad = true;
        leaderCounter = 0;
        requestCancelled = false;
        resultID.clear();
    }

    private void LoadData()
    {
        Query query = DBref.child("groups").orderByChild(JSON_START_DATE_TAG).limitToFirst(20); //TODO change this after adding sort by settings
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //creating the temporary group
                    tempGroup = new Group();
                    tempGroup.leader = new User();
                    tempGroup = snapshot.getValue(Group.class);
                    tempGroup.setGroup_id(snapshot.getKey());

                    //adding it to the list
                    tempGroupList.add(tempGroup);
                }
                SortLeaderInfo();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                requestCancelled = true;
            }
        });
    }

    //TODO when user is searching for groups and presses back, getActivity() will throw NPE. Fix that ONLY after converting this to kotlin
    private void SortLeaderInfo()
    {
        //TODO convert this to java and skip the other crap arguments
        final String table = General.getCurrentTable(getActivity());
        final DBManager dbManager = new DBManager(getActivity(), "reserveDB.db", table);
        dbManager.openDataBase();

        Query query = DBref.child("users").orderByKey(); //TODO user search
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) //getting users
                    {
                        for(Group group : tempGroupList) //going over every collected group
                        {
                            if(group.getMember_ids().get(0).equals(userSnapshot.getKey())) //checking if leader
                            {
                                //TODO change database calls
                                //loading leader profile picture url
                                tempUserImageUrl = userSnapshot.getValue(User.class)
                                        .getProfile_picture_url();
                                int tempDestId = Integer.parseInt(group
                                        .getDestination_id());

                                //creating the group button
                                GroupButton tempGroupButton = new GroupButton(
                                        group.getGroup_id(), //Group ID
                                        dbManager.getImage( //Reserve Image
                                                tempDestId, getActivity(), table
                                        ),
                                        tempUserImageUrl, //Leader Image URL
                                        tempDestId, //Reserve ID
                                        dbManager.getStr( //Reserve Name
                                                tempDestId,
                                                "name", table //TODO remove hardcode
                                        ),
                                        group.getMember_ids().get(0) //Leader ID
                                );

                                //adding the button to the list
                                groupButtons.add(tempGroupButton);

                            }
                        }
                    }
                } else {
                    //couldn't find results
                    Toast.makeText(getActivity(), R.string.search_no_results, Toast.LENGTH_SHORT)
                            .show();
                }

                //check for interruptions
                if(getActivity() != null)
                    PostLoadData();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                requestCancelled = true;
            }
        });

    }

    private void PostLoadData()
    {
        firstLoad = false;

        //NO NEED FOR ANSWERBUTTONS, THE GROUPBUTTONS WILL CONTAIN QUERIED GROUPS
        populateListView();
    }

    private void populateListView()
    {
            ArrayAdapter<GroupButton> adapter = new GroupsListAdapter();
            list.setAdapter(adapter);
            progressBar.setVisibility(View.INVISIBLE);
    }

    private class GroupsListAdapter extends ArrayAdapter<GroupButton> {
        public GroupsListAdapter() {
            super(getActivity(), R.layout.group_list_item, groupButtons);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getActivity().getLayoutInflater()
                        .inflate(R.layout.group_list_item, parent, false);
            }
            final GroupButton currentButton = groupButtons.get(position);

            //Loading Reserve Text
            TextView txtView = (TextView) itemView.findViewById((R.id.resultGroupText));
            txtView.setText(currentButton.getName());

            //Loading Leader Image
            final ImageView leaderImage = (ImageView) itemView.findViewById(R.id.leader_image);
            String leaderImageRef = currentButton.getLeader_image_url();
            Picasso.with(getContext())
                    .load(leaderImageRef)
                    .transform(new RoundedCornersTransformation(
                            getResources().getInteger(R.integer.pic_small_angle),
                            getResources().getInteger(R.integer.pic_small_margin)))
                    .into(leaderImage);

            //Loading Reserve Background
            ImageView reserveImage = (ImageView) itemView.findViewById(R.id.resultGroupImage);
            reserveImage.setImageDrawable(currentButton.getImage());

            //Configuring Clicks
            reserveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Creating intent
                    Intent intent= new Intent(getActivity(),GroupActivity.class);

                    //Sending data over to intent
                    intent.putExtra("group_id",currentButton.getGroup_id());
                    intent.putExtra("leader_id",currentButton.getLeader_id());
                    intent.putExtra("leader_image_url",currentButton.getLeader_image_url());
                    intent.putExtra("reserve_id",currentButton.getReserve_id());
                    intent.putExtra("reserve_name",currentButton.getName());

                    //Starting intent
                    getActivity().startActivity(intent);
                }
            });

            return itemView;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        //Getting query TODO get the search query
        //searchQuery = textView.getText().toString().toLowerCase();

        return false;
    }

    @Override
    public void onResume() {
        if(!firstLoad)
            PostLoadData();

        super.onResume();
    }
}
