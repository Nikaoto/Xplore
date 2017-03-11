package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.explorify.xplore.xplore_demo.General.dbManager;

/**
 * Created by nikao on 2/18/2017.
 */

public class CreateGroupFragment extends Fragment {

    public static int chosenDestId = -1; //TODO remove static
    public static ArrayList<User> invitedMembers = new ArrayList<>();
    public static DateSetup dateSetup;

    final int G_PREFS_CHAR_MAX = 200;
    final int G_PREFS_CHAR_MIN = 2;
    final int E_INFO_CHAR_MAX = 200;
    final int E_INFO_CHAR_MIN = 2;

    private int selectedMemberPos;
    private Context context;
    private RelativeLayout memberLayout;
    private RecyclerView memberRecList;

    EditText leaderId_text, groupPrefs_text, extraInfo_text;
    ImageView prefs_help, info_help;
    int experienceAns; // -1 -> not selected, 0 -> no exp, 1-> exp.
    String gPrefs, eInfo;
    User leader;
    TextView startDate_text, endDate_text, member_fname_text, member_lname_text, member_age_text, member_tel_text;
    Button chooseButton, reserveButton, inviteButton, uninviteButton, doneButton, startDate, endDate;
    RadioGroup radioGroup;
    View myView;

    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference();

    private class UploadGroup extends Group
    {
        public UploadGroup() {
        }

        public UploadGroup(String group_id, boolean experienced, long start_date, long end_date, String destination_id,
                           String extra_info, String group_preferences, ArrayList<String> member_ids) {
            this.group_id = group_id;
            this.experienced = experienced;
            this.start_date = start_date;
            this.end_date = end_date;
            this.destination_id = destination_id;
            this.extra_info = extra_info;
            this.group_preferences = group_preferences;
            this.member_ids = member_ids;
        }

        @Exclude
        public Map<String, Object> toMap()
        {
            HashMap<String, Object> result = new HashMap<>();
            result.put("destination_id", this.destination_id);
            result.put("start_date", this.start_date);
            result.put("end_date", this.end_date);
            result.put("experienced", this.experienced);
            result.put("group_preferences", this.group_preferences);
            result.put("extra_info", this.extra_info);
            result.put("member_ids", this.member_ids);
            return result;
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.create_group,container,false);

        context = getActivity();

        dateSetup = new DateSetup();

        InitLayout();

        invitedMembers.clear();
        chosenDestId = -1;
        experienceAns = -1;

        InitClickEvents();

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!General.isNetConnected(context))
        {
            General.groups_DisplayNetErrorDialog(context);
        }
        else {

            ApplyDates();

            if (chosenDestId != -1) {
                reserveButton.setBackground(dbManager.getReserveImage(General.getCurrentTable(context), chosenDestId, context));
                reserveButton.setText(dbManager.getStrFromDB(General.getCurrentTable(context), chosenDestId, dbManager.getNameColumnName()));
            }
        }
    }

    private void ApplyDates()
    {
        if(dateSetup.isConfirmedS())
            startDate_text.setText(dateSetup.getsYear()+"/"+dateSetup.getsMonth()+"/"+dateSetup.getsDay());

        if(dateSetup.isConfirmedE())
            endDate_text.setText(dateSetup.geteYear()+"/"+dateSetup.geteMonth()+"/"+dateSetup.geteDay());
    }

    private void InitLayout()
    {
        //Member List RecyclerView
        memberRecList = (RecyclerView) myView.findViewById(R.id.createGroup_member_list); //NOTE: uncomment in create_group.xml
        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        memberRecList.setHasFixedSize(true);
        memberRecList.setLayoutManager(layoutManager);

        //Selected Member Info Layout
        memberLayout = (RelativeLayout) myView.findViewById(R.id.member_profile_layout);
        memberLayout.setVisibility(View.GONE);

        //Selected Member Stuff
        member_fname_text = (TextView) myView.findViewById(R.id.member_fname_text);
        member_lname_text = (TextView) myView.findViewById(R.id.member_lname_text);
        member_age_text = (TextView) myView.findViewById(R.id.member_age_text);
        member_tel_text = (TextView) myView.findViewById(R.id.member_tel_text);

        //Buttons
        chooseButton = (Button) myView.findViewById(R.id.chooseReserve_button);
        reserveButton = (Button) myView.findViewById(R.id.createGroup_reserveButton);
        doneButton = (Button) myView.findViewById(R.id.creteGroup_done_button);
        inviteButton = (Button) myView.findViewById(R.id.inviteMembers_button);
        uninviteButton = (Button) myView.findViewById(R.id.uninviteMember_button);
        startDate = (Button) myView.findViewById(R.id.chooseStartDate);
        endDate = (Button) myView.findViewById(R.id.chooseEndDate);

        //Radios
        radioGroup = (RadioGroup) myView.findViewById(R.id.radioGroup);

        //Images
        prefs_help = (ImageView) myView.findViewById(R.id.prefs_help);
        info_help = (ImageView) myView.findViewById(R.id.extraInfo_help);

        //Tint Images
        prefs_help.setColorFilter(ContextCompat.getColor(context,R.color.colorGrey));
        info_help.setColorFilter(ContextCompat.getColor(context,R.color.colorGrey));

        //Texts
        leaderId_text= (EditText) myView.findViewById(R.id.userId_editText);
        startDate_text = (TextView) myView.findViewById(R.id.dateStart_text);
        endDate_text = (TextView) myView.findViewById(R.id.dateEnd_text);
        groupPrefs_text= (EditText) myView.findViewById(R.id.groupPrefs_editText);
        extraInfo_text = (EditText) myView.findViewById(R.id.extraInfo_editText);
    }

    private void InitClickEvents()
    {
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,SearchDestinationActivity.class);
                context.startActivity(intent);
            }
        });

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chosenDestId != -1)
                    General.OpenLibFragment(chosenDestId, context);
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSetup.setChoice(true);
                Intent intent = new Intent(context,CalendarActivity.class);
                context.startActivity(intent);
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateSetup.setChoice(false);
                Intent intent = new Intent(context,CalendarActivity.class);
                context.startActivity(intent);
            }
        });

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchUsersActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        prefs_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowHelp(R.string.group_preferences, R.string.group_prefs_help, R.string.okay,
                        getResources());
            }
        });

        info_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowHelp(R.string.extra_info, R.string.extra_info_help, R.string.okay,
                        getResources());
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GatherData();

                if(CheckFields())
                {
                    UploadGroupData();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.yes_rb)
                    experienceAns = 1;
                else if(i == R.id.no_rb)
                    experienceAns = 0;
            }
        });
    }

    private UploadGroup CreateGroup(String key)
    {
        //get member IDs
        ArrayList<String> member_ids = new ArrayList<>();
        member_ids.add(String.valueOf(leaderId_text.getText())); //TODO change with Google Account associate lookup
        for(int i = 0; i<invitedMembers.size(); i++)
        {
            member_ids.add(invitedMembers.get(i).getId());
        }

        //get experience question
        boolean exp;
        if(experienceAns == 0)
            exp = false;
        else
            exp = true;

        return new UploadGroup(key, exp, dateSetup.getStart(), dateSetup.getEnd(), String.valueOf(chosenDestId),
                eInfo, gPrefs, member_ids);
    }

    private void UploadGroupData()
    {
        Toast.makeText(context,"Uploading Data...", Toast.LENGTH_SHORT).show(); //TODO add string resources
        String key = groupsRef.child("groups").push().getKey();
        Map<String, Object> groupData = CreateGroup(key).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/groups/"+key, groupData);

        groupsRef.updateChildren(childUpdates);
        Toast.makeText(context,"Data Uploaded", Toast.LENGTH_SHORT).show(); //TODO add string resources
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK)
        {
            if(data.getBooleanExtra("member_added", false)) {//checking if members added
                PopulateMembersList();
            }
        }
    }

    private void PopulateMembersList()
    {
        memberRecList.setVisibility(View.VISIBLE);
        final MemberListAdapter adapter = new MemberListAdapter(context, invitedMembers, memberLayout);
        memberRecList.setAdapter(adapter);

        uninviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMemberPos = adapter.GetSelectedMemberPos();
                Toast.makeText(context, R.string.member_removed, Toast.LENGTH_SHORT).show();
                invitedMembers.remove(selectedMemberPos);
                adapter.notifyItemRemoved(selectedMemberPos);
                adapter.notifyItemRangeChanged(selectedMemberPos, invitedMembers.size());
                adapter.SetSelectedMemberPos(-1);
                memberLayout.setVisibility(view.GONE);
                if(invitedMembers.isEmpty())
                    memberRecList.setVisibility(View.GONE);

            }
        });
    }

    private void GatherData()
    {
        gPrefs = groupPrefs_text.getText().toString();
        eInfo = extraInfo_text.getText().toString();
    }

    private void ShowHelp(int title, int text, int butt_text, Resources resources)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(resources.getString(text))
                .setTitle(resources.getString(title))
                .setCancelable(false)
                .setPositiveButton(resources.getString(butt_text),null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean CheckFields()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.okay, null);

        if(leaderId_text.getText().length() < 1)
        { //TODO remove this after finishing groupCreation testing & adding user account management
            builder.setMessage("Fill user id pls")
                    .show();
            return false;
        }
        else if(chosenDestId == -1) {
            builder.setMessage(R.string.dest_field_incomplete)
                    .show();
            return false;
        }
        else if(!dateSetup.confirmedS) {
            builder.setMessage(R.string.date_start_field_incomplete)
                    .show();
            return false;
        }
        else if(!dateSetup.confirmedE) {
            builder.setMessage(R.string.date_end_field_incomplete)
                    .show();
            return false;
        }
        else if(dateSetup.getStart() > dateSetup.getEnd())
        {
            builder.setMessage(R.string.date_invalid)
                    .show();
            return false;
        }
        else if(dateSetup.getStart() < General.GetCurrentDate()
                || dateSetup.getEnd() < General.GetCurrentDate()){
            builder.setMessage(R.string.date_past_invalid)
                    .show();
            return false;
        }
        else if(experienceAns == -1)
        {
            builder.setMessage(R.string.exp_field_incomplete)
                    .show();
            return false;
        }
        else if( gPrefs.length() < G_PREFS_CHAR_MIN || gPrefs.length() > G_PREFS_CHAR_MAX
                || eInfo.length() < E_INFO_CHAR_MIN || eInfo.length() > E_INFO_CHAR_MAX) {
         builder.setMessage(R.string.text_field_incomplete)
                 .show();
            return false;
        }
        else
            return true;
    }
}
