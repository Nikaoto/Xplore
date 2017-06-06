package com.explorify.xplore.xplore;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import static com.explorify.xplore.xplore.General.popSignInMenu;


/**
 * Created by Nika on 11/9/2016.
 */

public class GroupMenuFragment extends Fragment {

    private View myView;
    private ImageView b_join, b_create;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.group_menu_layout, container, false);
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
                    popSignInMenu(0.8, 0.6, myView, getActivity());
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
                    popSignInMenu(0.8, 0.6, myView, getActivity());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        /*if(General.accountStatus == General.SIGNED_IN) {
            Toast.makeText(getActivity(), "Signed In", Toast.LENGTH_SHORT).show(); //TODO string resources
        }*/

        if(General.accountStatus == General.REGISTERED)
            Toast.makeText(getActivity(), "Registered", Toast.LENGTH_SHORT).show(); //TODO string resources
    }
}
