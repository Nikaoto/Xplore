package com.explorify.xplore.xplore;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nika on 11/9/2016.
 */

public class FifthFragment extends Fragment {

    View myView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fifth_layout, container, false);
        return myView;
    }
}
