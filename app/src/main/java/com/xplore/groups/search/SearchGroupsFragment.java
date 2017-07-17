package com.xplore.groups.search;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.xplore.database.DBManager;
import com.xplore.General;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.groups.GroupCard;
import com.xplore.groups.create.CreateGroupActivity;
import com.xplore.user.User;

import java.util.ArrayList;

/**
 * Created by Nikaoto on 2/8/2017.
 */

//TODO add searching
public class SearchGroupsFragment extends Fragment implements EditText.OnEditorActionListener {

    private static final String FIREBASE_START_DATE_TAG = "start_date";
    private static final String FIREBASE_MEMBER_IDS_TAG = "member_ids";
    private static final DatabaseReference firebaseGroupsRef
            = FirebaseDatabase.getInstance().getReference().child("groups");
    private static final DatabaseReference firebaseUsersRef
            = FirebaseDatabase.getInstance().getReference().child("users");

    private String searchQuery;
    private boolean firstLoad;

    private RecyclerView resultsRV;
    private EditText searchBar;
    private ProgressBar progressBar;

    private ArrayList<GroupCard> groupCards = new ArrayList<>();
    private ArrayList<GroupCard> displayCards = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.search_layout2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimeManager.Companion.refreshGlobalTimeStamp();

        //RecyclerView
        resultsRV = (RecyclerView) view.findViewById(R.id.resultsRV);
        resultsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Search EditText
        searchBar = (EditText) view.findViewById(R.id.searchEditText);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_groups_hint);
        searchBar.setOnEditorActionListener(this);

        //Progressbar
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        //FAB
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.createGroupFAB);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opens CreateGroupFragment
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CreateGroupActivity()).commit();
                getFragmentManager().executePendingTransactions();
            }
        });

        //Checking internet and displaying data
        if(!General.isNetConnected(getActivity())) {
            General.createNetErrorDialog(getActivity());
        } else if (getActivity() != null) {
            //buildUserBase();
            prepareToLoadData();
            loadData();
        }
    }

    private void prepareToLoadData() {
        progressBar.setVisibility(View.VISIBLE);
        groupCards.clear();
        firstLoad = true;

        //Displaying list already
        resultsRV.setAdapter(new GroupsListAdapter(displayCards));
    }

    private void loadData() {
        Query query = firebaseGroupsRef.orderByChild(FIREBASE_START_DATE_TAG).limitToFirst(100); //TODO change this after adding sort by settings
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //Geting group info
                    GroupCard tempCard = snapshot.getValue(GroupCard.class);
                    //group id
                    tempCard.setGroupId(snapshot.getKey());
                    //leader id
                    tempCard.setLeaderId(snapshot.child(FIREBASE_MEMBER_IDS_TAG).getChildren().iterator().next().getValue(String.class));

                    //adding it to the list
                    groupCards.add(tempCard);
                }
                if (getActivity() != null) {
                    sortLeaderInfo();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    //Goes over every user in firebase to check which of them are leaders (to get leader image)
    private void sortLeaderInfo() {
        //TODO convert this to Kotlin and skip the other crap arguments
        final DBManager dbManager = new DBManager(getActivity(), "reserveDB.db", General.DB_TABLE);
        dbManager.openDataBase();

        Query query = firebaseUsersRef.orderByKey();
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //For every user, check every group and check if they're the leader
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { //getting users
                        for(GroupCard groupCard : groupCards) { //going over every collected group
                            if(groupCard.getLeaderId().equals(userSnapshot.getKey())) { //checking if leader
                                //TODO change database calls
                                GroupCard tempGroupCard = groupCard;
                                //Setting reserve id
                                //TODO remove this and just set display image url
                                tempGroupCard.setReserveImageId(
                                        dbManager.getImageId(
                                                Integer.valueOf(tempGroupCard.getDestination_id()),
                                                getActivity(),
                                                dbManager.getGENERAL_TABLE()));

                                //Setting leader info
                                User leader = userSnapshot.getValue(User.class);
                                tempGroupCard.setLeaderName(leader.getFname() + " " + leader.getLname());
                                tempGroupCard.setLeaderReputation(leader.getReputation());
                                tempGroupCard.setLeaderImageUrl(
                                        userSnapshot.getValue(User.class).getProfile_picture_url());

/*                                //Setting reserve name
                                //TODO remove this and just get tour name from firebase
                                groupCard.setName(
                                        dbManager.getStr(
                                                Integer.valueOf(groupCard.getDestination_id()),
                                                DBManager.ColumnNames.getNAME(),
                                                General.DB_TABLE));*/

                                displayCards.add(tempGroupCard); //TODO maybe remove displaycards? needs testing :P
                                resultsRV.getAdapter().notifyDataSetChanged();
                            }
                        }
                    }
                } else {
                    //couldn't find results
                    Toast.makeText(getActivity(), R.string.search_no_results, Toast.LENGTH_SHORT)
                            .show();
                }
                if(getActivity() != null) {
                    postLoadData();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void postLoadData() {
        firstLoad = false;
        progressBar.setVisibility(View.INVISIBLE);
    }
    private class GroupsListAdapter extends RecyclerView.Adapter<GroupsListAdapter.ResultsViewHolder> {
        final int imgSize;
        final ArrayList<GroupCard> groupCards;

        public GroupsListAdapter(ArrayList<GroupCard> groupCards) {
            this.groupCards = groupCards;
            imgSize = Math.round(getResources().getDimension(R.dimen.user_profile_image_tiny_size));
        }

        class ResultsViewHolder extends RecyclerView.ViewHolder {
            //TODO add ribbons and stuff
            ImageView groupImage;
            ImageView leaderImage;
            TextView leaderName;
            TextView leaderReputation;
            RelativeLayout leaderLayout;

            public ResultsViewHolder(View itemView) {
                super(itemView);
                this.leaderImage = (ImageView) itemView.findViewById(R.id.leaderImageView);
                this.leaderName = (TextView) itemView.findViewById(R.id.leaderNameTextView);
                this.leaderReputation = (TextView) itemView.findViewById(R.id.leaderRepCombinedTextView);
                this.leaderLayout = (RelativeLayout) itemView.findViewById(R.id.leaderLayout);
                this.groupImage = (ImageView) itemView.findViewById(R.id.groupImageView);
            }
        }

        @Override
        public ResultsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ResultsViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.group_card, parent, false));
        }

        @Override
        public void onBindViewHolder(ResultsViewHolder holder, final int position) {
            final GroupCard currentCard = groupCards.get(position);

            //Leader layout
            holder.leaderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    General.openUserProfile(getActivity(), currentCard.getLeaderId());
                }
            });

            //On card click
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    General.HideKeyboard(getActivity());
                    //TODO add this in general
                    //Creating intent
                    Intent intent = new Intent(getActivity(), ViewGroupActivity.class);

                    //Sending data over to intent
                    intent.putExtra("group_id", currentCard.getGroupId());
                    intent.putExtra("reserve_id", Integer.valueOf(currentCard.getDestination_id()));

                    //Starting intent
                    getActivity().startActivity(intent);
                }
            });

            //Leader name
            holder.leaderName.setText(currentCard.getLeaderName());

            //Leader reputation
            holder.leaderReputation.setText(currentCard.getLeaderReputation() + " " + getActivity().getResources().getString(R.string.reputation));

            //Leader image
            Picasso.with(getActivity())
                    .load(currentCard.getLeaderImageUrl())
                    .transform(new CircleTransformation(imgSize, imgSize))
                    .into(holder.leaderImage);
            holder.leaderImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    General.openUserProfile(getActivity(), currentCard.getLeaderId());
                }
            });

            //Group image
            //TODO change this to just map or submitted image
            holder.groupImage.setImageResource(currentCard.getReserveImageId());

            //TODO add ribbons and other stuff
        }

        @Override
        public int getItemCount() {
            return groupCards.size();
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        //TODO get query and search type
        //searchQuery = textView.getText().toString().toLowerCase();

        return false;
    }

    @Override
    public void onResume() {
        if(!firstLoad) {
            postLoadData();
        }
        super.onResume();
    }

    /*
    IN CASE WE'RE USING A SECOND FIREBASE DATABASE FOR USERS
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
    if (FirebaseApp.getApps(getActivity()).get(1).equals(null)) {
    FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
    } catch (IndexOutOfBoundsException e) {
    FirebaseApp.initializeApp(getActivity(), userBaseOptions, "userbase");
    }
    userBaseApp = FirebaseApp.getInstance("userbase");
    userDB = FirebaseDatabase.getInstance(userBaseApp);
    }
    */
}
