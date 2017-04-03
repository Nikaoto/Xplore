package com.explorify.xplore.xplore_demo;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nika on 11/9/2016.
 */

public class SecondFragment extends Fragment implements TextView.OnEditorActionListener {


    public static boolean needRefresh = false;

    private View myView;
    private ListView list;
    private EditText searchBar;
    private List<Integer> resultIDs = new ArrayList<>();
    private Context context;
    private ArrayList<ReserveButton> answerButtons = new ArrayList<>();
    private ArrayList<ReserveButton> reserveButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.first_layout, container, false);

        context = getActivity();

        //setting up the listview
        list = (ListView) myView.findViewById(R.id.resultslist);

        //setting up searchbar
        searchBar = (EditText) myView.findViewById(R.id.search_bar);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_hint);
        searchBar.setSelectAllOnFocus(true);
        searchBar.setOnEditorActionListener(this);

        return myView;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        searchListItems(textView.getText().toString().toLowerCase());

        return false;
    }

    //Sets up reserveButtons, refreshes data and preloads data, then displays it
    private void InitDataLists()
    {
        //Load all reserveButtons from DB
        General.populateButtonList(reserveButtons, context);

        //Clear answer list
        answerButtons.clear();

        //Push all reserveButtons to answers
        answerButtons.addAll(reserveButtons);

        populateListView();
    }


    @Override
    public void onResume() {
        super.onResume();

        //Check if language was changed and data needs refreshing
        if (needRefresh) {
            needRefresh = false;
            InitDataLists();
        }

    }

    private void populateListView() {

        //creating list
        ArrayAdapter<ReserveButton> adapter = new SecondFragment.MyListAdapter();
        list.setAdapter(adapter);
    }



    private class MyListAdapter extends ArrayAdapter<ReserveButton> {
        public MyListAdapter() {
            super(context, R.layout.list_item, answerButtons);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.list_item, parent, false);
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
                    General.OpenLibFragment(currentButton.getId(), getActivity());
                }
            });

            return itemView;
        }
    }

    //Searches DB for query
    private void searchListItems(String query) {
        answerButtons.clear();
        //Opening Database
        try{ General.dbManager.openDataBase(); }
        catch (SQLException sqle){ throw sqle; }

        //Searching Database
        resultIDs = General.dbManager.getIdFromQuery(query,General.getCurrentTable(context));

        //Returning Results
        if(resultIDs == null)
        {
            Toast.makeText(context, R.string.search_no_results , Toast.LENGTH_SHORT).show();
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

    //hides the sotft keyboard
    public void HideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        InitDataLists();
    }

}
