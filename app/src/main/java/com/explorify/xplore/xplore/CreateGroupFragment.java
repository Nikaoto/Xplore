package com.explorify.xplore.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.explorify.xplore.xplore.General.currentUserId;

/**
 * Created by nikao on 2/18/2017.
 */

public class CreateGroupFragment extends Fragment implements DatePickerDialog.OnDateSetListener {


    public static ArrayList<User> invitedMembers = new ArrayList<>();

    //Limits and restrictions to fields
    private static final int SEARCH_DESTINATION_ACODE = 1;
    private static final int INVITE_USERS_ACODE = 4;
    private static final int CHOSEN_DEST_DEFAULT_VAL = -1;
    private static final int EXPERIENCE_ANS_DEFAULT_VAL = -1;
    private static final int EXPERIENCE_ANS_NO_EXP = 0;
    private static final int EXPERIENCE_ANS_YES_EXP = 1;
    private static final int G_PREFS_CHAR_MAX = 200;
    private static final int G_PREFS_CHAR_MIN = 5; //TODO add selection if user doesn't have prefs
    private static final int E_INFO_CHAR_MAX = 200;
    private static final int E_INFO_CHAR_MIN = 5;

    private int chosenDestId = CHOSEN_DEST_DEFAULT_VAL;
    private int experienceAns = EXPERIENCE_ANS_DEFAULT_VAL;

    //Variables for finding out which date user is selecting (start / end)
    private static String SELECTION_NONE = "";
    private static String SELECTION_START = "start";
    private static String SELECTION_END = "end";
    private String selectingDate = SELECTION_NONE;


    //TODO Make a new class for holding the date after switching to UNIX time
    private Long globalTimeStamp = 0L;
    private int sYear = 0, sMonth, sDay, eYear = 0, eMonth, eDay;


    private int selectedMemberPos;
    private RelativeLayout memberLayout;
    private RecyclerView memberRecList;

    EditText groupPrefs_text, extraInfo_text;
    ImageView prefs_help, info_help;
    String groupPrefs, extraInfo;
    User leader;
    TextView startDate_text, endDate_text, member_fname_text, member_lname_text, member_age_text, member_tel_text;
    Button chooseButton, reserveButton, inviteButton, uninviteButton, doneButton, startDate, endDate;
    RadioGroup radioGroup;
    View myView;
    DBManager dbManager;

    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference();

    private class UploadGroup extends Group
    {
        public UploadGroup(String group_id, boolean experienced, long start_date, long end_date,
                           String destination_id, String extra_info, String group_preferences,
                           ArrayList<String> member_ids) {
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

        getServerEpoch();

        //TODO move this stuff to onViewCreated

        InitLayout();

        invitedMembers.clear();
        chosenDestId = CHOSEN_DEST_DEFAULT_VAL;
        experienceAns = EXPERIENCE_ANS_DEFAULT_VAL;

        InitClickEvents();

        return myView;
    }

    private void getServerEpoch() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Map<String, String>> dateValue = new HashMap<>();
        dateValue.put("timestamp", ServerValue.TIMESTAMP);
        ref.child("date").setValue(dateValue);
        ref.child("date").child("timestamp").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        globalTimeStamp = dataSnapshot.getValue(Long.class);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!General.isNetConnected(getActivity())) {
            General.createNetErrorDialog(getActivity());
        }
        else if (chosenDestId != CHOSEN_DEST_DEFAULT_VAL) {//TODO remove table arguments after converting to kotlin
            reserveButton.setBackgroundResource(dbManager.getImageId(chosenDestId, getActivity(), dbManager.getGENERAL_TABLE()));
            reserveButton.setText(dbManager.getStr(chosenDestId, DBManager.ColumnNames.getNAME(), General.DB_TABLE));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(General.isNetConnected(getActivity())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(globalTimeStamp));

            if(selectingDate.equals(SELECTION_START)){
                selectingDate = SELECTION_NONE;
                sYear = year; sMonth = month; sDay = day;
                startDate_text.setText(sYear+"/"+sMonth+"/"+sDay);
            }
            else if(selectingDate.equals(SELECTION_END)){
                selectingDate = SELECTION_NONE;
                eYear = year; eMonth = month; eDay = day;
                endDate_text.setText(eYear+"/"+eMonth+"/"+eDay);
            }
        } else General.createNetErrorDialog(getActivity());
    }

    private void InitLayout() {
        //Member List RecyclerView
        memberRecList = (RecyclerView) myView.findViewById(R.id.createGroup_member_list); //NOTE: uncomment in create_group.xml
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
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
        prefs_help.setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorGrey));
        info_help.setColorFilter(ContextCompat.getColor(getActivity(),R.color.colorGrey));

        //Texts
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
                Intent intent = new Intent(getActivity(),SearchDestinationActivity.class);
                startActivityForResult(intent, SEARCH_DESTINATION_ACODE);
            }
        });

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chosenDestId != CHOSEN_DEST_DEFAULT_VAL)
                    General.openReserveInfoFragment(chosenDestId, getActivity());
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(globalTimeStamp != 0L) {
                    selectingDate = SELECTION_START;
                    new DatePickerDialogFragment(CreateGroupFragment.this, globalTimeStamp, 0)
                            .show(getFragmentManager(), "startDate");
                }
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(globalTimeStamp != 0L) {
                    selectingDate = SELECTION_END;
                    new DatePickerDialogFragment(CreateGroupFragment.this, globalTimeStamp, 0)
                            .show(getFragmentManager(), "endDate");
                }
            }
        });

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchUsersActivity.class);
                startActivityForResult(intent, INVITE_USERS_ACODE);
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
                if(CheckFields()) {
                    UploadGroupData();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.yes_rb)
                    experienceAns = EXPERIENCE_ANS_YES_EXP;
                else if(i == R.id.no_rb)
                    experienceAns = EXPERIENCE_ANS_NO_EXP;
            }
        });
    }

    private UploadGroup CreateGroup(String key)
    {
        //getting member IDs
        ArrayList<String> member_ids = new ArrayList<>();
        member_ids.add(String.valueOf(currentUserId));

        for(int i = 0; i<invitedMembers.size(); i++) {
            member_ids.add(invitedMembers.get(i).getId());
        }

        //get experience question
        final boolean exp = (experienceAns != EXPERIENCE_ANS_NO_EXP);

        return new UploadGroup(
                key,    //Firebase Unique Group Key
                exp,    //Group Experienced Boolean
                General.getDateLong(sYear, sMonth, sDay),   //Start Date
                General.getDateLong(eYear, eMonth, eDay),   //End Date
                String.valueOf(chosenDestId),     //Chosen Destination Id
                extraInfo,     //Group Extra Info
                groupPrefs,    //Group Preferences
                member_ids);   //Group Member Ids
    }

    private void UploadGroupData()
    {
        Toast.makeText(getActivity(),"Uploading Data...", Toast.LENGTH_SHORT).show(); //TODO add string resources
        String key = groupsRef.child("groups").push().getKey();
        Map<String, Object> groupData = CreateGroup(key).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/groups/"+key, groupData);

        groupsRef.updateChildren(childUpdates);
        Toast.makeText(getActivity(),"Data Uploaded", Toast.LENGTH_SHORT).show(); //TODO add string resources
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_DESTINATION_ACODE) {
            if (resultCode == Activity.RESULT_OK) {
                chosenDestId = data.getIntExtra("chosen_destination_id", CHOSEN_DEST_DEFAULT_VAL);
            }
        } else if (requestCode == INVITE_USERS_ACODE) {
            if (resultCode == Activity.RESULT_OK) {//TODO remove chosenMembers static and get intarray of UserIds from SearchUsersActivity. Return RESULT_CANCELED when no member selected
                if (data.getBooleanExtra("member_added", false)) {//checking if members added
                    PopulateMembersList();
                }
            }
        }
    }

    private void PopulateMembersList()
    {
        memberRecList.setVisibility(View.VISIBLE);
        final MemberListAdapter adapter = new MemberListAdapter(getActivity(), invitedMembers, memberLayout);
        memberRecList.setAdapter(adapter);

        uninviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMemberPos = adapter.GetSelectedMemberPos();
                Toast.makeText(getActivity(), R.string.member_removed, Toast.LENGTH_SHORT).show();
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
        groupPrefs = groupPrefs_text.getText().toString();
        extraInfo = extraInfo_text.getText().toString();
    }

    private void ShowHelp(int title, int text, int butt_text, Resources resources)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(resources.getString(text))
                .setTitle(resources.getString(title))
                .setCancelable(false)
                .setPositiveButton(resources.getString(butt_text),null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean CheckFields()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.okay, null);

        if(chosenDestId == CHOSEN_DEST_DEFAULT_VAL) {
            builder.setMessage(R.string.dest_field_incomplete)
                    .show();
            return false;
        }
        else if(sYear == 0) {
            builder.setMessage(R.string.date_start_field_incomplete)
                    .show();
            return false;
        }
        else if(eYear == 0) {
            builder.setMessage(R.string.date_end_field_incomplete)
                    .show();
            return false;
        }
        else if(General.getDateLong(sYear, sMonth, sDay) > General.getDateLong(eYear, eMonth, eDay)){
            builder.setMessage(R.string.date_invalid)
                    .show();
            return false;
        }
        else if(experienceAns == EXPERIENCE_ANS_DEFAULT_VAL) {
            builder.setMessage(R.string.exp_field_incomplete)
                    .show();
            return false;
        }
        else if( groupPrefs.length() < G_PREFS_CHAR_MIN || groupPrefs.length() > G_PREFS_CHAR_MAX
                || extraInfo.length() < E_INFO_CHAR_MIN || extraInfo.length() > E_INFO_CHAR_MAX) {
         builder.setMessage(R.string.text_field_incomplete)
                 .show();
            return false;
        } else {
            if(General.getDateLong(sYear, sMonth, sDay) < General.getDateLong(globalTimeStamp) ||
                    General.getDateLong(eYear, eMonth, eDay) < General.getDateLong(globalTimeStamp)) {
                builder.setMessage(R.string.date_past_invalid).show();
                return false;
            } else
                return true;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO convert this to java and skip the other crap arguments
        dbManager = new DBManager(getActivity(), "reserveDB.db", General.DB_TABLE);
        dbManager.openDataBase();
    }
}
