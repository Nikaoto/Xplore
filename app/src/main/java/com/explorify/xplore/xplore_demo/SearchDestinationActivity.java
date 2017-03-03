package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_layout);

        //setting up the listview
        list = (ListView) findViewById(R.id.resultslist);

        //setting up searchbar
        searchBar = (EditText) findViewById(R.id.search_bar);
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
        General.populateButtonList(reserveButtons, this);

        if(first) {
            first = false;
            answerButtons.addAll(reserveButtons);
            populateListView();
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
        //Opening Database
        try{ General.dbManager.openDataBase(); }
        catch (SQLException sqle){ throw sqle; }

        //Searching Database
        resultIDs = General.dbManager.getIdFromQuery(query,General.getCurrentTable(this));

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