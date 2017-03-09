package com.explorify.xplore.xplore_demo;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
                if(isUserSignedIn()) {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new SearchGroupsFragment()).addToBackStack("4").commit();
                }
                else
                {
                    SignIn();
                }
            }
        });

        b_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isUserSignedIn()) {
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new CreateGroupFragment()).addToBackStack("4").commit();
                }
                else
                {
                    SignIn();
                }
            }
        });
    }

    private boolean isUserSignedIn()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return true;
        } else {
            return true;
        }
    }

    private void SignIn()
    {

    }

}
