package com.explorify.xplore.xplore_demo;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by nikao on 2/24/2017.
 */

public class General {

    public static DBmanager dbManager;

    public static void InitDBManager(Context context) {
        dbManager = new DBmanager(context);

        //try creating DB
        dbManager.createDataBase();
    }

    public static void populateButtonList(ArrayList<ReserveButton> reserveButtons, Context context)
    {
        String table = getCurrentTable(context);
        Resources resources = context.getResources();

        try { dbManager.openDataBase(); }
        catch (SQLException sqle){ throw sqle; }

        reserveButtons.clear();

        for(int i = 0; i < MainActivity.RESERVE_NUM; i++)
        {
            int resid = resources.getIdentifier(dbManager.getStrFromDB(table, i, dbManager.getImageColumnName()),"drawable","com.explorify.xplore.xplore_demo");
            reserveButtons.add (
                    new ReserveButton(i, ContextCompat.getDrawable(context, resid),
                            dbManager.getStrFromDB(table, i, dbManager.getNameColumnName()))
            );
        }
    }

    public static String getCurrentTable(Context context)
    {
        return context.getSharedPreferences("lang",0).getString("lang","en");
    }

    public static void OpenLibFragment(int resId, Context context)
    {
        Intent intent= new Intent(context,LibFragment.class);
        intent.putExtra("chosen_element",resId);
        context.startActivity(intent);
    }

    public static int GetCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();

        return GetDateTime(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static int GetDateTime(int year, int month, int day)
    {
        return year*10000 + month*100 + day;
    }
}
