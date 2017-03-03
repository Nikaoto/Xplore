package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Nika on 11/9/2016.
 */

public class SecondFragment extends Fragment {

    private ListView list;
    private int reserveImageRef;//TODO fix layout first (change it to Image)
    private View myView;
    private ArrayList<ReserveButton> reserveButtons = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.second_layout, container, false);

        //setting up listview
        list = (ListView) myView.findViewById(R.id.libListView);

        return myView;
    }

    @Override
    public void onResume() {
        super.onResume();

        //reload reserveButtons
        General.populateButtonList(reserveButtons,getActivity());
    }

    private void populateListView()
    {
        ArrayAdapter<ReserveButton> adapter = new MyListAdapter();
        list.setAdapter(adapter);
    }

    private class MyListAdapter extends ArrayAdapter<ReserveButton> {
        public MyListAdapter() {
            super(getActivity(), R.layout.list_item, reserveButtons);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if(itemView == null) {
                itemView = getActivity().getLayoutInflater().inflate(R.layout.list_item, parent, false);
            }
            final ReserveButton currentButton = reserveButtons.get(position);
            Button butt = (Button) itemView.findViewById(R.id.resultItem);
            butt.setText(reserveButtons.get(position).getName());
            butt.setBackground(currentButton.getImage());

            if(position <= MainActivity.RESERVE_NUM) { //TODO remove this after filling reserve db
                //Configuring Clicks
                butt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        General.OpenLibFragment(currentButton.getId(), getActivity());
                    }
                });
            }
            return itemView;
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        populateListView();
        super.onViewCreated(view, savedInstanceState);
    }

}
