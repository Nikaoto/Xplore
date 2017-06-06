package com.explorify.xplore.xplore;

import android.app.Fragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class LibraryFragment extends Fragment implements TextView.OnEditorActionListener {

    private ListView list;
    private List<Integer> resultIDs = new ArrayList<>();
    private ArrayList<ReserveButton> answerButtons = new ArrayList<>();
    private ArrayList<ReserveButton> reserveButtons = new ArrayList<>();
    private DBManager dbManager;
    private View myView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.search_layout, container, false);
        return myView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO convert this to java and skip the other crap arguments
        dbManager = new DBManager(getActivity(), "reserveDB.db", General.DB_TABLE);
        dbManager.openDataBase();

        //setting up the listview
        list = (ListView) myView.findViewById(R.id.resultslist);

        //setting up searchbar
        EditText searchBar = (EditText) myView.findViewById(R.id.search_bar);
        searchBar.setSingleLine(true);
        searchBar.setHint(R.string.search_hint);
        searchBar.setSelectAllOnFocus(true);
        searchBar.setOnEditorActionListener(this);

        //Load all reserveButtons from DB
        populateButtonList(reserveButtons);

        //Clear answer list
        answerButtons.clear();

        //Push all reserveButtons to answers
        answerButtons.addAll(reserveButtons);

        //Setting adapter
        ArrayAdapter<ReserveButton> adapter = new LibraryFragment.MyListAdapter();
        list.setAdapter(adapter);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        searchListItems(textView.getText().toString().toLowerCase());

        return false;
    }

    private class MyListAdapter extends ArrayAdapter<ReserveButton> {
        public MyListAdapter() {
            super(getActivity(), R.layout.list_item, answerButtons);
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
                    General.HideKeyboard(getActivity());
                    //TODO afrer changing LibFragment from FragmentActivity to Fragment, use fragmentmanager to replace fragment_containter here
                    General.OpenLibFragment(currentButton.getId(), getActivity());
                }
            });

            return itemView;
        }
    }

    //TODO after converting to kotlin, do this asynchronously
    private void populateButtonList(ArrayList<ReserveButton> reserveButtons)
    {
        String table = General.DB_TABLE;
        Resources resources = getActivity().getResources();

        reserveButtons.clear();

        //Getting each resID separately
        //TODO this is utter shit, put the loop inside of DBManager so it doesn't create and destroy a goddamn cursor every time we need a string from DB
        for(int i = 0; i < MainActivity.RESERVE_NUM; i++)
        {
            //TODO omg change this
            int resid = resources.getIdentifier(dbManager.getStr(i, DBManager.ColumnNames.getIMAGE(), table),"drawable","com.explorify.xplore.xplore");
            reserveButtons.add (
                    new ReserveButton(i, ContextCompat.getDrawable(getActivity(), resid),
                            dbManager.getStr(i, DBManager.ColumnNames.getNAME(), table))
            );
        }
    }

    //Searches DB for query
    private void searchListItems(String query) {
        answerButtons.clear();

        //Searching Database
        //TODO do getCurrentTable once on a private String for fucks sake (or do it in DBManager once on init)
        resultIDs = dbManager.getIdFromQuery(query, General.DB_TABLE);

        //Returning Results
        if(resultIDs == null)
        {
            Toast.makeText(getActivity(), R.string.search_no_results , Toast.LENGTH_SHORT).show();
        }
        else {
            int index = 0;
            for (int result : resultIDs) {
                //result is the single ID of an answer
                answerButtons.add(index,reserveButtons.get(result));
                index ++;
            }

            //Setting adapter
            ArrayAdapter<ReserveButton> adapter = new LibraryFragment.MyListAdapter();
            list.setAdapter(adapter);
        }
    }
}
