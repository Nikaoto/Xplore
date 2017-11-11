package com.xplore.groups.search;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.xplore.base.RefreshableSearchFragment;
import com.xplore.database.DBManager;
import com.xplore.groups.GroupCard;
import com.xplore.groups.GroupCardRecyclerViewAdapter;
import com.xplore.groups.create.CreateGroupActivity;
import com.xplore.user.User;

import java.util.ArrayList;

import static com.xplore.util.FirebaseUtil.F_DESTINATION_ID;
import static com.xplore.util.FirebaseUtil.F_GROUP_NAME;
import static com.xplore.util.FirebaseUtil.F_MEMBER_IDS;
import static com.xplore.util.FirebaseUtil.F_START_DATE;
import static com.xplore.util.FirebaseUtil.groupsRef;
import static com.xplore.util.FirebaseUtil.usersRef;

/**
 * Created by Nikaoto on 2/8/2017.
 *
 *  მომხმარებელი ამ ფრაგმენტიდან ეძებს დაგეგმილ ლაშქრობებს ან ქმნის ახალს (FAB-ით).
 *
 */

public class SearchGroupsFragment extends RefreshableSearchFragment {

    private static String ARG_DESTINATION_ID = "query";
    // Used to search for groups by destination (from ReserveInfoAct->Find Groups w/ This Dest)
    public static SearchGroupsFragment newInstance(int destId) {
        Fragment f = new SearchGroupsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_DESTINATION_ID, destId);
        f.setArguments(args);
        return (SearchGroupsFragment) f;
    }

    private static int FAB_HIDE_SCROLL_DY = 2;

    private boolean firstLoad;

    private RecyclerView resultsRV;
    private FloatingActionButton fab;

    private ArrayList<GroupCard> groupCards = new ArrayList<>();
    private ArrayList<GroupCard> displayCards = new ArrayList<>();

    // Used to refreshData the whole fragment in onResume to update cards
    private boolean allowRefresh = false;

    // Determines whether the data should reload when user clears search text
    private boolean canReset = false;

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
        fab = (FloatingActionButton) view.findViewById(R.id.createGroupFAB);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Opens CreateGroupFragment
                startActivity(CreateGroupActivity.Companion.getStartIntent(getActivity()));
            }
        });

        initRefreshLayout((SwipeRefreshLayout) view.findViewById(R.id.refreshLayout));

        // RecyclerView
        resultsRV = (RecyclerView) view.findViewById(R.id.resultsRV);
        resultsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        resultsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > FAB_HIDE_SCROLL_DY) {
                    fab.hide();
                } else {
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        firstLoadData();
    }

    @Override
    public void setUpSearchView(SearchView newSearchView) {
        if (getActivity() != null) {
            newSearchView.setQueryHint(getResources().getString(R.string.search_groups_hint));
        }
    }

    // Checks internet and displays data
    private void firstLoadData() {
        canReset = false;
        if(!General.isNetConnected(getActivity())) {
            General.createNetErrorDialog(getActivity());
        } else if (getActivity() != null) {
            prepareToLoadData();
            loadData();
        }
    }

    private void prepareToLoadData() {
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

    // Loads all groups (with searching by destination or just viewing)
    private void loadData() {
        // Check if searching from reserve
        Bundle b = getArguments();
        if (b != null && b.getInt(ARG_DESTINATION_ID, -1) != -1) {
            int destId = b.getInt(ARG_DESTINATION_ID, -1);

            // TODO add some sign that the user searched with the reserve

            groupsRef.orderByChild(F_DESTINATION_ID).equalTo(destId).limitToFirst(100)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadDataFromSnapshot(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        } else {

            //TODO change this after adding sort by options
            groupsRef.orderByChild(F_START_DATE).limitToFirst(100)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            loadDataFromSnapshot(dataSnapshot);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {}
                    });
        }
    }

    // Loads groups from snapshot
    private void loadDataFromSnapshot(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
                    nothingFound();
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
        fab.show();
        canReset = true;
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

    @Override
    public boolean onReset() {
        if (canReset) {
            firstLoadData();
        }
        return super.onReset();
    }

    private void nothingFound() {
        Toast.makeText(getActivity(), R.string.search_no_results, Toast.LENGTH_SHORT).show();
    }

    private void postLoadData() {
        setLoading(false);
        firstLoad = false;
        if (displayCards.isEmpty()) {
            nothingFound();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstLoad) {
            postLoadData();
        }

        // Checking if refreshData needed
        if (allowRefresh) {
            refreshData();
        } else {
            allowRefresh = true;
        }
    }

    private void refreshData() {
        setLoading(true);
        firstLoadData();
    }

    @Override
    public void onRefreshed() {
        super.onRefreshed();
        refreshData();
    }
}