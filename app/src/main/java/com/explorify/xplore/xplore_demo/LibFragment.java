package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nikao on 11/16/2016.
 */

public class LibFragment extends FragmentActivity {

    private Reserve reserve = new Reserve();
    private int chosenElement;
    private Button libButton;

    private RatingBar libDifficulty;
    private TextView libDescription, libFauna, libFlora, libTags, libEquipment, libLocation;
    final Activity activity = (Activity) this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lib_element_layout);

        Intent intent = this.getIntent();
        chosenElement = intent.getIntExtra("chosen_element",0);

        libButton = (Button) findViewById(R.id.libButton);
        libDescription = (TextView) findViewById(R.id.lib_description);
        libFlora = (TextView) findViewById(R.id.lib_flora);
        libFauna = (ExpandableTextView) findViewById(R.id.lib_fauna);

        libEquipment = (TextView) findViewById(R.id.lib_equipment);
        libTags = (TextView) findViewById(R.id.lib_tags);
        libDifficulty = (RatingBar) findViewById(R.id.lib_difficutyBar);
        libLocation = (Button) findViewById(R.id.showOnMapButton);
        libLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ThirdFragment.MAPS_CLOSED = false;
                Intent intent= new Intent(activity, MapsActivity.class);
                intent.putExtra("show_reserve", true);
                intent.putExtra("reserve_name",reserve.getName());
                intent.putExtra("reserve_latitude",reserve.getLocation().latitude);
                intent.putExtra("reserve_longitude",reserve.getLocation().longitude);
                activity.startActivity(intent);
            }
        });

        LoadReserveInfo();
        setupLayout();
    }

    @Override
    protected void onResume() {
        if(ThirdFragment.MAPS_CLOSED)
        {
            getFragmentManager().popBackStack();
        }
        super.onResume();
    }

    public void setupLayout()
    {
        libButton.setBackground(reserve.getDrawable());
        libButton.setText(reserve.getName());
        libDescription.setText(reserve.getDescription());
        libFauna.setText(reserve.getFauna());
        libFlora.setText(reserve.getFlora());
        libEquipment.setText(reserve.getEquipment());
        libTags.setText(reserve.getExtratags());
        libDifficulty.setRating(reserve.getDifficulty());
    }

    public void LoadReserveInfo()
    {
        //try opening DB
        try { General.dbManager.openDataBase(); }
        catch (SQLException sqle){ throw sqle; }

        //Getting reserve info from DB
        reserve = General.dbManager.getReserve(chosenElement, this);
    }
}
