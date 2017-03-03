package com.explorify.xplore.xplore_demo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import static com.explorify.xplore.xplore_demo.General.dbManager;

/**
 * Created by nikao on 2/18/2017.
 */

public class CreateGroupFragment extends Fragment {

    public static int chosenDestId = -1;
    public static ArrayList<String> memberIds = new ArrayList<>();
    public static DateSetup dateSetup;

    final int G_PREFS_CHAR_MAX = 200;
    final int G_PREFS_CHAR_MIN = 2;
    final int E_INFO_CHAR_MAX = 200;
    final int E_INFO_CHAR_MIN = 2;

    private Context context;

    EditText leaderId_text, groupPrefs_text, extraInfo_text;
    ImageView prefs_help, info_help;
    int experienceAns; // -1 -> not selected, 0 -> no exp, 1-> exp.
    String gPrefs, eInfo;
    User leader;
    TextView startDate_text, endDate_text;
    Button chooseButton, reserveButton, inviteButton, doneButton, startDate, endDate;
    RadioGroup radioGroup;
    View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.create_group,container,false);

        context = getActivity();

        dateSetup = new DateSetup();

        InitLayout();

        chosenDestId = -1;
        experienceAns = -1;

        InitClickEvents();

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();

        ApplyDates();

        if(chosenDestId != -1) {
            reserveButton.setBackground(dbManager.getReserveImage(General.getCurrentTable(context), chosenDestId, context));
            reserveButton.setText(dbManager.getStrFromDB(General.getCurrentTable(context), chosenDestId, dbManager.getNameColumnName()));
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
        //Buttons
        chooseButton = (Button) myView.findViewById(R.id.chooseReserve_button);
        reserveButton = (Button) myView.findViewById(R.id.createGroup_reserveButton);
        doneButton = (Button) myView.findViewById(R.id.creteGroup_done_button);
        inviteButton = (Button) myView.findViewById(R.id.inviteMembers_button);
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
                context.startActivity(intent);
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
                    //start post info to firebase
                    Toast.makeText(getActivity(),"Uploading Data...", Toast.LENGTH_SHORT).show();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.okay, null);

        if(leaderId_text.getText().length() < 1)
        { //TODO remove this after finishing groupCreation testing
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
