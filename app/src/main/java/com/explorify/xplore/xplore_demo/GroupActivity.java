package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.Calendar;
import java.util.Date;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by nikao on 2/12/2017.
 */

public class GroupActivity extends Activity {

    private String USERBASE_KEY;
    private String USERBASE_APPID;
    private String USERBASE_URL;

    private String leader_id, leader_image_ref, group_id, reserveName, startDate, endDate;
    private int reserveID, selectedMemberPos;
    private ImageView leader_image, group_experience_image;
    private Button reserveButton; //TODO CHANGE TO ImageView
    private RelativeLayout memberLayout;
    private TextView leader_lname_text, leader_fname_text, leader_rep_text, leader_age_text, leader_tel_text;
    private TextView member_lname_text, member_fname_text, member_age_text, member_tel_text;
    private TextView startDate_text, endDate_text, groupPrefs_text, extraInfo_text;
    private ProgressBar progressBar;
    private RecyclerView memberRecList;
    private long memberCount;
    private Resources resources;
    //private View divider;
    //private float dividerMoveY;
    private ArrayList<User> members = new ArrayList<>();

    FirebaseApp userBaseApp;
    FirebaseOptions userBaseOptions;
    FirebaseDatabase userDB;
    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference();
    Group tempGroup;
    User tempMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.group_layout);

        Authorize();
        buildUserBase();
        PreLoadData();

        resources = getResources();

        //Receiving data from last intent
        Intent intent = this.getIntent();
        group_id = intent.getStringExtra("group_id");
        leader_id = intent.getStringExtra("leader_id");
        leader_image_ref = intent.getStringExtra("leader_image_ref");
        reserveID = intent.getIntExtra("reserve_id",0);
        reserveName = intent.getStringExtra("reserve_name");

        InitLayout();
        InitMemberList();

        //reset the layout TODO remove after finishing testing and clear layouts
        ResetLayout();

        //Loading the info
        LoadGroupData(group_id);
        ApplyReserveData();
    }

    private void InitLayout()
    {
        //ProgressBar
        progressBar = (ProgressBar) findViewById(R.id.groupProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        //Leader Stuff
        leader_image = (ImageView) findViewById(R.id.leader_profile_image);
        leader_fname_text = (TextView) findViewById(R.id.leader_fname_text);
        leader_lname_text = (TextView) findViewById(R.id.leader_lname_text);
        leader_rep_text = (TextView) findViewById(R.id.leader_rep_text);
        leader_age_text = (TextView) findViewById(R.id.leader_age_text);
        leader_tel_text = (TextView) findViewById(R.id.leader_tel_text);

        //Member Stuff
        member_fname_text = (TextView) findViewById(R.id.member_fname_text);
        member_lname_text = (TextView) findViewById(R.id.member_lname_text);
        member_age_text = (TextView) findViewById(R.id.member_age_text);
        member_tel_text = (TextView) findViewById(R.id.member_tel_text);
        memberLayout = (RelativeLayout) findViewById(R.id.member_profile_layout);
        memberLayout.setVisibility(View.GONE);

        //Group Stuff
        group_experience_image = (ImageView) findViewById(R.id.group_experience_image);
        startDate_text = (TextView) findViewById(R.id.date_start);
        endDate_text = (TextView) findViewById(R.id.date_end);
        groupPrefs_text = (TextView) findViewById(R.id.group_prefs_text);
        extraInfo_text = (TextView) findViewById(R.id.group_extrainfo_text);

        //ReserveButon Stuff
        reserveButton = (Button) findViewById(R.id.group_reserveButton);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                General.OpenLibFragment(reserveID, GroupActivity.this);
            }
        });

    }

    private void InitMemberList()
    {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        memberRecList = (RecyclerView) findViewById(R.id.member_list); //NOTE: uncomment 'member_list' in group_layout.xml
        memberRecList.setHasFixedSize(true);
        memberRecList.setLayoutManager(layoutManager);
    }

    private void ResetLayout()
    {
        //leader
        leader_fname_text.setText("-");
        leader_lname_text.setText("-");
        leader_age_text.setText("");
        leader_tel_text.setText("");
        leader_rep_text.setText("");
        //member
        member_fname_text.setText("-");
        member_lname_text.setText("-");
        member_age_text.setText("");
        member_tel_text.setText("");
        //group
        startDate_text.setText("");
        endDate_text.setText("");
        groupPrefs_text.setText("");
        extraInfo_text.setText("");
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
        memberCount = 1;
        selectedMemberPos = -1;
        members.clear();
    }

    private void LoadGroupData(final String groupId)
    {
        DatabaseReference Ref = groupsRef.getRef();
        Query query = Ref.child(groupId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //creating the temporary (current) group
                tempGroup = new Group();
                tempGroup.leader = new User();
                tempGroup = dataSnapshot.getValue(Group.class);
                tempGroup.setGroup_id(dataSnapshot.getKey());

                memberCount = tempGroup.getMember_ids().size();

                for(String memberId : tempGroup.getMember_ids()){ //TODO? this returns members in random order
                    GetMemberInfo(String.valueOf(memberId));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void GetMemberInfo(final String userId)
    {
        DatabaseReference ref = userDB.getReference().getRef();
        Query query = ref.child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tempMember = dataSnapshot.getValue(User.class); //Getting member info
                members.add(tempMember); //Setting member info
                memberCount --; //Iterating member index
                if(memberCount == 0) { //Checking if member list retrieval finished
                    ApplyGroupData();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private void ApplyReserveData()
    {
        reserveButton.setText(reserveName);
        reserveButton.setBackground(General.dbManager.getReserveImage(
                General.getCurrentTable(GroupActivity.this),reserveID, this)
        );
    }

    private void ApplyGroupData()
    {
        progressBar.setVisibility(View.GONE);
        Picasso.with(this)
                .load(leader_image_ref)
                .transform(new RoundedCornersTransformation(
                        getResources().getInteger(R.integer.pic_big_angle),
                        getResources().getInteger(R.integer.pic_big_margin)))
                .into(leader_image);
        leader_fname_text.setText(members.get(0).getFname());
        leader_lname_text.setText(members.get(0).getLname());
        leader_age_text.setText(getString(R.string.age)+": "+members.get(0).getAge());
        leader_tel_text.setText(getString(R.string.tel)+": "+members.get(0).getTel_num());
        leader_rep_text.setText(String.valueOf(members.get(0).getReputation()));

        if(tempGroup.isExperienced()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                group_experience_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_check, this.getTheme()));
            else
                group_experience_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_check));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                group_experience_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_x, this.getTheme()));
            else
                group_experience_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_x));
        }

        group_experience_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this);
                builder.setTitle(R.string.what_is_this)
                            .setMessage(R.string.group_exp_help)
                            .setPositiveButton(R.string.okay, null);
                builder.show();
            }
        });

        startDate = BufferDate(tempGroup.getStart_date());
        endDate = BufferDate(tempGroup.getEnd_date());

        startDate_text.setText(startDate);
        endDate_text.setText(endDate);
        groupPrefs_text.setText(tempGroup.getGroup_preferences());
        extraInfo_text.setText(tempGroup.getExtra_info());

        populateMemberImageList();
    }

    private void populateMemberImageList() {
        MembersListAdapter adapter = new MembersListAdapter(members);
        memberRecList.setAdapter(adapter);
    }

    public class MembersListAdapter extends RecyclerView.Adapter<MembersListAdapter.MemberViewHolder> {

        private ArrayList<User> users = new ArrayList<>();

        public MembersListAdapter(ArrayList<User> users) {
            this.users = users;
        }

        public class MemberViewHolder extends RecyclerView.ViewHolder{
            TextView rep_txt;
            ImageView memberImage;

            public MemberViewHolder(View itemView) {
                super(itemView);
                rep_txt = (TextView) itemView.findViewById((R.id.member_rep_text));
                memberImage = (ImageView) itemView.findViewById(R.id.member_profile_image);
            }
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        @Override
        public MemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.member_list_item, parent, false);
            return new MemberViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MemberViewHolder holder, final int position) {
            final User currentMember = users.get(position);

            //Loading Member Reputation
            holder.rep_txt.setText(String.valueOf(currentMember.getReputation()));

            //Loading Member Image
            Picasso.with(GroupActivity.this)
                    .load(currentMember.getProfile_picture_ref())
                    .transform(new RoundedCornersTransformation(
                            getResources().getInteger(R.integer.pic_small_angle),
                            getResources().getInteger(R.integer.pic_small_margin)))
                    .into(holder.memberImage);

            //Configuring Clicks
            holder.memberImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(selectedMemberPos == position) {//same member click
                        memberLayout.setVisibility(View.GONE);
                        selectedMemberPos = -1;
                        //divider.animate().translationY(-dividerMoveY);
                    }
                    else {//other member click

                        if(selectedMemberPos == -1) {//first click
                            memberLayout.setVisibility(View.VISIBLE);
                            //divider.animate().translationY(dividerMoveY);
                        }

                        selectedMemberPos = position;
                        member_fname_text.setText(currentMember.getFname());
                        member_lname_text.setText(currentMember.getLname());
                        member_age_text.setText(getString(R.string.age) + ": " + currentMember.getAge());
                        member_tel_text.setText(getString(R.string.tel) + ": " + currentMember.getTel_num());
                    }
                }
            });
        }
    }
    
    //adds slashes to a date given in int (yyyy.mm.dd) without dots
    private String BufferDate(long date)
    {
        StringBuffer sd = new StringBuffer(String.valueOf(date));
        sd.insert(4,"/");
        sd.insert(7,"/");
        return String.valueOf(sd);
    }
}
