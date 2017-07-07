package com.xplore.maps;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nika on 11/9/2016.
 */

public class MapFragment extends Fragment {

    View myView;
    public static boolean MAPS_CLOSED;
    public static boolean FIRST_TIME;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MAPS_CLOSED=false;
        FIRST_TIME = true;
        Intent intent= new Intent(getActivity(),MapsActivity.class);
        intent.putExtra("show_reserve", false);
        getActivity().startActivity(intent);
        return myView;
    }

    @Override
    public void onResume() {
        if(MAPS_CLOSED)
        {
            getFragmentManager().popBackStack();
        }
        else
        {   if(!FIRST_TIME){
                Intent intent= new Intent(getActivity(),MapsActivity.class);
                intent.putExtra("show_reserve", false);
                getActivity().startActivity(intent);
            }
        }
        super.onResume();
    }
}
