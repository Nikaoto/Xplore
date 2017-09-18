package com.xplore.groups.search;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.xplore.General;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.base.SearchFragment;
import com.xplore.database.DBManager;
import com.xplore.groups.GroupCard;
import com.xplore.groups.GroupCardRecyclerViewAdapter;
import com.xplore.groups.create.CreateGroupActivity;
import com.xplore.user.User;

import java.util.ArrayList;

import static com.xplore.util.FirebaseUtil.F_GROUP_NAME;
import static com.xplore.util.FirebaseUtil.F_MEMBER_IDS;
import static com.xplore.util.FirebaseUtil.F_START_DATE;
import static com.xplore.util.FirebaseUtil.groupsRef;
import static com.xplore.util.FirebaseUtil.usersRef;

/**
 * Created by Nikaoto on 2/8/2017.
 */

//TODO add searching
public class SearchGroupsFragment extends SearchFragment {

    private boolean firstLoad;

    private RecyclerView resultsRV;

    private ArrayList<GroupCard> groupCards = new ArrayList<>();
    private ArrayList<GroupCard> displayCards = new ArrayList<>();

    private Boolean allowRefresh = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle instState) {
        return inflater.inflate(R.layout.search_layout3, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TimeManager.refreshGlobalTimeStamp();

        // FAB
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.createGroupFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Opens CreateGroupFragment
                startActivity(CreateGroupActivity.Companion.getStartIntent(getActivity()));
            }
        });

        // RecyclerView
        resultsRV = (RecyclerView) view.findViewById(R.id.resultsRV);
        resultsRV.setLayoutManager(new LinearLayoutManager(getActivity()));

        resultsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }

                super.onScrolled(recyclerView, dx, dy);
            }
        });

        // Checking internet and displaying data
        if(!General.isNetConnected(getActivity())) {
            General.createNetErrorDialog(getActivity());
        } else if (getActivity() != null) {
            prepareToLoadData();
            loadData();
        }
    }

    @Override
    public void setUpSearchView(SearchView newSearchView) {
        newSearchView.setQueryHint(getResources().getString(R.string.search_groups_hint));
    }

    private void prepareToLoadData() {
        showProgressBar();

        groupCards.clear();
        displayCards.clear();
        firstLoad = true;

        //Displaying list already (empty)
        resultsRV.setAdapter(new GroupCardRecyclerViewAdapter(displayCards, getActivity()));
    }

    // Returns member count from a datasnapshot of a group
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
        final DBManager dbManager = new DBManager(getActivity(), "reserveDB.db", DBManager.DB_TABLE);
        dbManager.openDataBase();

        usersRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    // For every user, check every group and check if they're the leader
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) { //getting users
                        for(GroupCard groupCard : groupCards) { //going over every collected group
                            if(groupCard.getLeaderId().equals(userSnapshot.getKey())) { //checking if leader

                                // Set leader info
                                User leader = userSnapshot.getValue(User.class);
                                if (leader != null) {
                                    groupCard.setLeaderName(leader.getFname() + " " + leader.getLname());
                                    groupCard.setLeaderReputation(leader.getReputation());
                                    groupCard.setLeaderImageUrl(
                                            userSnapshot.getValue(User.class).getProfile_picture_url());

                                    displayCards.add(groupCard); //TODO maybe remove displaycards? needs testing :P
                                    resultsRV.getAdapter().notifyDataSetChanged();
                                }
                            }
                        }
                    }
                } else {
                    // Couldn't find results
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

    @Override
    public boolean onSearch(@NonNull String query) {
        prepareToLoadData();

        // Search by group name
        // TODO add more search filters
        groupsRef.orderByChild(F_GROUP_NAME).startAt(query).endAt(query+"\uf8ff").limitToFirst(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null) {
                            for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                                //Geting group info
                                GroupCard groupCard = groupSnapshot.getValue(GroupCard.class);
                                if (groupCard != null) {
                                    // Group Id
                                    groupCard.setId(groupSnapshot.getKey());
                                    groupCard.setMemberCount(getMemberCount(groupSnapshot));

                                    // Leader Id
                                    for (DataSnapshot memberEntry
                                            : groupSnapshot.child(F_MEMBER_IDS).getChildren()) {

                                        // Check if current memberEntry belongs to the leader
                                        Boolean isLeader = memberEntry.getValue(Boolean.class);
                                        if (isLeader != null && isLeader) {
                                            // Set leader id
                                            groupCard.setLeaderId(memberEntry.getKey());
                                        }
                                    }

                                    // Adding card to list
                                    groupCards.add(groupCard);
                                }
                            }
                            if (getActivity() != null) {
                                sortLeaderInfo();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

        return super.onSearch(query);
    }

    private void postLoadData() {
        firstLoad = false;
        hideProgressBar();
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