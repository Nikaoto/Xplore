package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nika on 11/9/2016.
 */

public class FirstFragment extends Fragment implements EditText.OnEditorActionListener {

    private View myView;
    private ListView list;
    private EditText searchBar;
    private String searchQuery;
    private List<Integer> resultID;
    private ArrayList<ReserveButton> answerButtons = new ArrayList<>();
    private ArrayList<ReserveButton> reserveButtons = new ArrayList<>();
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.first_layout, container, false);
        resultID = new ArrayList<Integer>();//maybe

        //setting up listview
        list = (ListView) myView.findViewById(R.id.resultslist);

        //setting up searchbar
        searchBar = (EditText) myView.findViewById(R.id.search_bar);
        searchBar.setHint(R.string.search_hint);
        searchBar.setOnEditorActionListener(this);

        //setting up progressbar
        progressBar = (ProgressBar) myView.findViewById(R.id.searchProgressBar);

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //Refresh reserveButtons data
        General.populateButtonList(reserveButtons, getActivity());
    }

    private void populateListView() {
        ArrayAdapter<ReserveButton> adapter = new MyListAdapter();
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<ReserveButton> {
        public MyListAdapter() {
            super(getActivity(), R.layout.list_item, answerButtons);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.list_item, parent,
                        false);
            }
            final ReserveButton currentButton = answerButtons.get(position);

            Button butt = (Button) itemView.findViewById(R.id.resultItem);
            butt.setText(currentButton.getName());
            butt.setBackground(currentButton.getImage());

            //Configuring Clicks
            butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    General.OpenLibFragment(currentButton.getId(), getActivity());
                }
            });

            return itemView;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        progressBar.setVisibility(View.VISIBLE);

        //Clear list
        answerButtons.clear();

        //Getting query
        searchQuery = textView.getText().toString().toLowerCase();

        //Opening Database
        try{ General.dbManager.openDataBase(); }
        catch (SQLException sqle){ throw sqle; }

        //Searching Database
        resultID = General.dbManager.getIdFromQuery(
                searchQuery,
                General.getCurrentTable(getActivity())
        );

        progressBar.setVisibility(View.GONE);

        //Returning Results
        if(resultID == null)
        {
            Toast.makeText(getActivity().getApplicationContext(), R.string.search_no_results,
                    Toast.LENGTH_SHORT).show();
        }
        else {
            int index = 0;
            for (int result : resultID) {
                //result is the single ID of an answer
                answerButtons.add(index,reserveButtons.get(result));
                index ++;
            }

            populateListView();
        }
        return false;
    }
}
