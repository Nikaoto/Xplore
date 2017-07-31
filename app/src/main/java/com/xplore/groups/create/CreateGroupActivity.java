package com.xplore.groups.create;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xplore.database.DBManager;
import com.xplore.CustomDatePicker;
import com.xplore.General;
import com.xplore.MemberListAdapter;
import com.xplore.R;
import com.xplore.TimeManager;
import com.xplore.reserve.ReserveInfoActivity;
import com.xplore.user.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.xplore.General.currentUserId;

/**
 * Created by Nikaoto on 2/18/2017.
 *
 * აღწერა:
 * ეს ფრაგმენტი იხსნება როცა მომხმარებელი ახალ გუნდს ქმნის. ეს კლასი ამოწმებს მომხმარებლის შეცდომებს
 * ფორმის შევსებისას და "დასტურზე" დაჭერის შემდგომ ტვირთავს ახალ გუნდს Firebase-ს ბაზაში
 *
 * Description:
 * This fragment opens when user is creating a group. This class checks for any errors in user's
 * group info and with "Done" uploads group info to groups Fireabase Database
 *
 */

public class CreateGroupActivity extends Activity implements DatePickerDialog.OnDateSetListener {

    //TODO turn this into an activity
    //TODO add meeting time
    //TODO add finish time
    //TODO close activity when "Done" is clicked and and group info is correct. Upload group data afterwards (async)

    public static ArrayList<User> invitedMembers = new ArrayList<>();

    //Activity Codes
    private static final int SEARCH_DESTINATION_ACTIVITY_CODE = 1;
    private static final int INVITE_USERS_ACTIVITY_CODE = 4;
    //Limits and restrictions to fields
    private static final int CHOSEN_DEST_DEFAULT = -1;
    private static final int EXPERIENCE_ANS_DEFAULT = -1;
    private static final int EXPERIENCE_ANS_NO = 0;
    private static final int EXPERIENCE_ANS_YES = 1;
    private static final int G_PREFS_CHAR_MAX = 200;
    private static final int G_PREFS_CHAR_MIN = 0; //TODO add selection if user doesn't have prefs
    private static final int E_INFO_CHAR_MAX = 200;
    private static final int E_INFO_CHAR_MIN = 5;

    //Setting chosen answer and destination to default
    private int chosenDestId = CHOSEN_DEST_DEFAULT;
    private int experienceAns = EXPERIENCE_ANS_DEFAULT;

    //Variables for finding out which date user is selecting (start / end)
    private static String SELECTION_NONE = "";
    private static String SELECTION_START = "start";
    private static String SELECTION_END = "end";
    private String selectingDate = SELECTION_NONE;


    //TODO Make a new class for holding the date after switching to UNIX time
    private int sYear = 0, sMonth, sDay, eYear = 0, eMonth, eDay;

    private RecyclerView memberRecList;

    EditText groupPrefs_text, extraInfo_text;
    ImageView prefs_help, info_help;
    String groupPrefs, extraInfo;
    User leader;
    TextView startDate_text, endDate_text;
    Button chooseButton, inviteButton, doneButton, startDate, endDate;
    Button reserveButton; //TODO replace reserveButton with reserveCard
    RadioGroup radioGroup;
    DBManager dbManager;

    DatabaseReference groupsRef = FirebaseDatabase.getInstance().getReference();

    public static Intent getStartIntent(Context context) {
        return new Intent(context, CreateGroupActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_group);

        setTitle(R.string.activity_create_group_title);

        //Refreshing server timeStamp
        TimeManager.Companion.refreshGlobalTimeStamp();

        //TODO move this stuff to onViewCreated

        InitLayout();

        invitedMembers.clear();
        chosenDestId = CHOSEN_DEST_DEFAULT;
        experienceAns = EXPERIENCE_ANS_DEFAULT;

        InitClickEvents();

        //TODO convert this to Kotlin and skip the other crap arguments
        dbManager = new DBManager(this, "reserveDB.db", General.DB_TABLE);
        dbManager.openDataBase();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Check if net connected
        if (!General.isNetConnected(this)) {
            General.createNetErrorDialog(this);
        }
        //Checking if user chose a destination
        else if (chosenDestId != CHOSEN_DEST_DEFAULT) {//TODO remove table arguments after converting to kotlin
            //TODO make a separate method for displaying the reserve
            reserveButton.setBackgroundResource(dbManager.getImageId(chosenDestId, this, dbManager.getGENERAL_TABLE()));
            reserveButton.setText(dbManager.getStr(chosenDestId, DBManager.ColumnNames.getNAME(), General.DB_TABLE));
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if(General.isNetConnected(this)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(TimeManager.Companion.getGlobalTimeStamp()));

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
        } else General.createNetErrorDialog(this);
    }

    private void InitLayout() {
        //Member List RecyclerView
        memberRecList = (RecyclerView) findViewById(R.id.createGroup_member_list); //NOTE: uncomment in create_group.xml
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        memberRecList.setHasFixedSize(true);
        memberRecList.setLayoutManager(layoutManager);

        //Buttons
        chooseButton = (Button) findViewById(R.id.chooseReserve_button);
        reserveButton = (Button) findViewById(R.id.createGroup_reserveButton);
        doneButton = (Button) findViewById(R.id.creteGroup_done_button);
        inviteButton = (Button) findViewById(R.id.inviteMembers_button);
        startDate = (Button)findViewById(R.id.chooseStartDate);
        endDate = (Button) findViewById(R.id.chooseEndDate);

        //Radios
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        //Images
        prefs_help = (ImageView) findViewById(R.id.prefs_help);
        info_help = (ImageView) findViewById(R.id.extraInfo_help);

        //Tint Images
        prefs_help.setColorFilter(ContextCompat.getColor(this, R.color.colorGrey));
        info_help.setColorFilter(ContextCompat.getColor(this, R.color.colorGrey));

        //Texts
        startDate_text = (TextView) findViewById(R.id.dateStart_text);
        endDate_text = (TextView) findViewById(R.id.dateEnd_text);
        groupPrefs_text= (EditText) findViewById(R.id.groupPrefs_editText);
        extraInfo_text = (EditText) findViewById(R.id.extraInfo_editText);
    }

    private void InitClickEvents()
    {
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateGroupActivity.this, SearchDestinationActivity.class);
                startActivityForResult(intent, SEARCH_DESTINATION_ACTIVITY_CODE);
            }
        });

        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chosenDestId != CHOSEN_DEST_DEFAULT)
                    startActivity(ReserveInfoActivity.getStartIntent(CreateGroupActivity.this, chosenDestId));
            }
        });

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TimeManager.Companion.getGlobalTimeStamp() != 0L) {
                    selectingDate = SELECTION_START;
                    new CustomDatePicker(CreateGroupActivity.this, TimeManager.Companion.getGlobalTimeStamp(), 0)
                            .show(getFragmentManager(), "startDate");
                }
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TimeManager.Companion.getGlobalTimeStamp() != 0L) {
                    selectingDate = SELECTION_END;
                    new CustomDatePicker(CreateGroupActivity.this, TimeManager.Companion.getGlobalTimeStamp(), 0)
                            .show(getFragmentManager(), "endDate");
                }
            }
        });

        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateGroupActivity.this, SearchUsersActivity.class);
                startActivityForResult(intent, INVITE_USERS_ACTIVITY_CODE);
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
                    uploadGroupData();
                }
            }
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.yes_rb)
                    experienceAns = EXPERIENCE_ANS_YES;
                else if(i == R.id.no_rb)
                    experienceAns = EXPERIENCE_ANS_NO;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_DESTINATION_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                chosenDestId = data.getIntExtra("chosen_destination_id", CHOSEN_DEST_DEFAULT);
            }
        } else if (requestCode == INVITE_USERS_ACTIVITY_CODE) {
            if (resultCode == Activity.RESULT_OK) {//TODO remove chosenMembers static and get intarray of UserIds from SearchUsersActivity. Return RESULT_CANCELED when no member selected
                if (data.getBooleanExtra("member_added", false)) {//checking if members added
                    PopulateMembersList();
                }
            }
        }
    }

    //Creates uploadable group
    private UploadableGroup createUploadGroup(String key) {
        //getting member IDs
        ArrayList<String> member_ids = new ArrayList<>();
        member_ids.add(String.valueOf(currentUserId));

        for(User user : invitedMembers) {
            member_ids.add(user.getId());
        }

        //get experience question
        final boolean exp = (experienceAns != EXPERIENCE_ANS_NO);

        return new UploadableGroup(
                key,    //Firebase Unique Group Key
                exp,    //Group Experienced Boolean
                General.getDateLong(sYear, sMonth, sDay),   //Start Date
                General.getDateLong(eYear, eMonth, eDay),   //End Date
                String.valueOf(chosenDestId),     //Chosen Destination Id
                extraInfo,     //Group Extra Info
                groupPrefs,    //Group Preferences
                member_ids);   //Group Member Ids
    }

    private void uploadGroupData() {
        String key = groupsRef.child("groups").push().getKey();
        Map<String, Object> groupData = createUploadGroup(key).toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/groups/"+key, groupData);

        groupsRef.updateChildren(childUpdates);
        Toast.makeText(this, "Group Created", Toast.LENGTH_SHORT).show(); //TODO add string resources
        General.HideKeyboard(this);
        finish();
    }

    private void PopulateMembersList() {
        memberRecList.setVisibility(View.VISIBLE);
        final MemberListAdapter adapter = new MemberListAdapter(this, invitedMembers, true);
        memberRecList.setAdapter(adapter);
    }

    private void GatherData() {
        groupPrefs = groupPrefs_text.getText().toString();
        extraInfo = extraInfo_text.getText().toString();
    }

    private void ShowHelp(int title, int text, int butt_text, Resources resources) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(resources.getString(text))
                .setTitle(resources.getString(title))
                .setCancelable(false)
                .setPositiveButton(resources.getString(butt_text), null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private boolean CheckFields()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.okay, null);

        if(chosenDestId == CHOSEN_DEST_DEFAULT) {
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
        else if(experienceAns == EXPERIENCE_ANS_DEFAULT) {
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
            if(General.getDateLong(sYear, sMonth, sDay) < General.getDateLong(TimeManager.Companion.getGlobalTimeStamp()) ||
                    General.getDateLong(eYear, eMonth, eDay) < General.getDateLong(TimeManager.Companion.getGlobalTimeStamp())) {
                builder.setMessage(R.string.date_past_invalid).show();
                return false;
            } else
                return true;
        }
    }
}
