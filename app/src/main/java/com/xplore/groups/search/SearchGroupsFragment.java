package com.xplore.groups.search;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xplore.database.DBManager;
import com.xplore.General;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.groups.GroupCard;
import com.xplore.groups.GroupCardRecyclerViewAdapter;
import com.xplore.groups.create.CreateGroupActivity;
import com.xplore.user.User;

import java.util.ArrayList;

/**
 * Created by Nikaoto on 2/8/2017.
 */

//TODO add searching
public class SearchGroupsFragment extends Fragment implements EditText.OnEditorActionListener {

    //Firebase References
    private static final DatabaseReference usersRef
            = FirebaseDatabase.getInstance().getReference().child("users");
    private static final DatabaseReference groupsRef
            = FirebaseDatabase.getInstance().getReference().child("groups");
    //Firebase Tags
    private static final String F_START_DATE = "start_date";
    private static final String F_MEMBER_IDS = "member_ids";

    private String searchQuery;
    private boolean firstLoad;

    private RecyclerView resultsRV;
    private ProgressBar progressBar;

    private ArrayList<GroupCard> groupCards = new ArrayList<>();
    private ArrayList<GroupCard> displayCards = new ArrayList<>();

    private Boolean allowRefresh = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instState) {
        return inflater.inflate(R.layout.search_layout2, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimeManager.refreshGlobalTimeStamp();

        //RecyclerView
        resultsRV = (RecyclerView) view.findViewById(R.id.resultsRV);
        resultsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        //Search EditText
        EditText searchBar = (EditText) view.findViewById(R.id.searchEditText);
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
                startActivity(CreateGroupActivity.Companion.getStartIntent(getActivity()));
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
        displayCards.clear();
        firstLoad = true;

        //Displaying list already (empty)
        resultsRV.setAdapter(new GroupCardRecyclerViewAdapter(displayCards, getActivity()));
    }

    private int getMemberCount(DataSnapshot groupSnapshot) {
        return (int) groupSnapshot.child(F_MEMBER_IDS).getChildrenCount();
    }

    private void loadData() {
        //TODO change this after adding sort by options
        groupsRef.orderByChild(F_START_DATE).limitToFirst(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    //Geting group info
                    GroupCard tempCard = snapshot.getValue(GroupCard.class);
                    //group id
                    tempCard.setId(snapshot.getKey());
                    tempCard.setMemberCount(getMemberCount(snapshot));
                    //Leader id
                    //TODO change when multiple leaders are added
                    for (DataSnapshot memberId : snapshot.child(F_MEMBER_IDS).getChildren()) {
                        if (memberId.getValue(Boolean.class)) {
                            tempCard.setLeaderId(memberId.getKey());
                        }
                    }

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

        usersRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    //For every user, check every group and check if they're the leader
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { //getting users
                        for(GroupCard groupCard : groupCards) { //going over every collected group
                            if(groupCard.getLeaderId().equals(userSnapshot.getKey())) { //checking if leader
                                //TODO change database calls
                                GroupCard tempGroupCard = groupCard;

                                //Setting leader info
                                User leader = userSnapshot.getValue(User.class);
                                tempGroupCard.setLeaderName(leader.getFname() + " " + leader.getLname());
                                tempGroupCard.setLeaderReputation(leader.getReputation());
                                tempGroupCard.setLeaderImageUrl(
                                        userSnapshot.getValue(User.class).getProfile_picture_url());

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

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        //TODO get query and search type
        //searchQuery = textView.getText().toString().toLowerCase();

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstLoad) {
            postLoadData();
        }

        //Checking if refresh needed
        if (allowRefresh) {
            allowRefresh = false;
            getFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .detach(this).attach(this).commit();
        } else {
            allowRefresh = true;
        }
    }
}
