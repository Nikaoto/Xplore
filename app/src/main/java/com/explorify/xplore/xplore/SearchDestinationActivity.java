package com.explorify.xplore.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nikao on 2/23/2017.
 */


public class SearchDestinationActivity extends Activity {

    private boolean first = true;
    private ListView list;
    private EditText searchBar;
    private List<Integer> resultIDs = new ArrayList<>();
    private ArrayList<ReserveButton> answerButtons = new ArrayList<>();
    private ArrayList<ReserveButton> reserveButtons = new ArrayList<>();

    DBManager dbManager;
    String table;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);

        //setting up the listview
        list = (ListView) findViewById(R.id.resultslist);

        //TODO convert this to java and skip the other crap arguments
        dbManager = new DBManager(this, "reserveDB.db", General.getCurrentTable(this));
        dbManager.openDataBase();
        table = General.getCurrentTable(this);

        //setting up searchbar
        searchBar = (EditText) findViewById(R.id.search_bar);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_hint);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    searchListItems(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        populateButtonList(reserveButtons, this);

        if(first) {
            first = false;
            answerButtons.addAll(reserveButtons);
            populateListView();
        }
    }

    //TODO after converting to kotlin, do this asynchronously
    private void populateButtonList(ArrayList<ReserveButton> reserveButtons, Context context)
    {
        String table = General.getCurrentTable(context);
        Resources resources = context.getResources();

        reserveButtons.clear();

        //Getting each resID separately
        //TODO this is utter shit, put the loop inside of DBManager so it doesn't create and destroy a goddamn cursor every time we need a string from DB
        for(int i = 0; i < MainActivity.RESERVE_NUM; i++)
        {
            //TODO the "image" and "name" column names have to be changed, remove hardcode
            //TODO omg change this
            int resid = resources.getIdentifier(dbManager.getStr(i, "image", table),"drawable","com.explorify.xplore.xplore");
            reserveButtons.add (
                    new ReserveButton(i, ContextCompat.getDrawable(context, resid),
                            dbManager.getStr(i, "name", table))
            );
        }
    }

    private void populateListView() {

        //creating list
        ArrayAdapter<ReserveButton> adapter = new MyListAdapter();
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<ReserveButton> {
        public MyListAdapter() {
            super(SearchDestinationActivity.this, R.layout.list_item, answerButtons);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
            }
            final ReserveButton currentButton = answerButtons.get(position);

            Button butt = (Button) itemView.findViewById(R.id.resultItem);
            butt.setText(currentButton.getName());
            butt.setBackground(currentButton.getImage());

            //Configuring Clicks
            butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HideKeyboard();
                    AlertDialog.Builder builder = new AlertDialog.Builder(SearchDestinationActivity.this);
                    builder.setMessage(getResources().getString(R.string.question_choose_reserve)+" "+currentButton.getName()+"?")
                            .setTitle(currentButton.getName())
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    CreateGroupFragment.chosenDestId = currentButton.getId();
                                    onBackPressed();
                                }
                            })
                            .setNegativeButton(R.string.no, null);

                    builder.show();
                }
            });

            return itemView;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    //hides the sotft keyboard
    public void HideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void searchListItems(String query) {
        answerButtons.clear();

        //Searching Database
        resultIDs = dbManager.getIdFromQuery(query,table);

        //Returning Results
        if(resultIDs == null)
        {
            Toast.makeText(this, R.string.search_no_results , Toast.LENGTH_SHORT).show();
        }
        else {
            int index = 0;
            for (int result : resultIDs) {
                //result is the single ID of an answer
                answerButtons.add(index,reserveButtons.get(result));
                index ++;
            }

            populateListView();
        }
    }
}