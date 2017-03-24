package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import static com.explorify.xplore.xplore_demo.General.popSignInMenu;


/**
 * Created by Nika on 11/9/2016.
 */

public class FourthFragment extends Fragment {

    private View myView;
    private ImageView b_join, b_create;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fourth_layout, container, false);
        return myView;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        b_join = (ImageView) myView.findViewById(R.id.join_party);
        b_create = (ImageView) myView.findViewById(R.id.create_party);

        b_join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(General.isUserSignedIn()) {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new SearchGroupsFragment()).addToBackStack("4").commit();
                }
                else
                {
                    popSignInMenu(0.8, 0.6, true, myView, getActivity());
                }
            }
        });

        b_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(General.isUserSignedIn()) {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new CreateGroupFragment()).addToBackStack("4").commit();
                }
                else
                {
                    popSignInMenu(0.8, 0.6, true, myView, getActivity());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(General.accountStatus == General.JUST_SIGNED_IN) {
            General.accountStatus = 0;
            Toast.makeText(getActivity(), "Signed In", Toast.LENGTH_SHORT).show(); //TODO string resources
        }
        else if(General.accountStatus == General.REGISTERED)
        {
            General.accountStatus = 0;
            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show(); //TODO string resources
        }
    }
}
