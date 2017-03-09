package com.explorify.xplore.xplore_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.SQLException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
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

    public static boolean isNetConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static void groups_DisplayNetErrorDialog(final Context context, final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.wifi_connect_dialog)
                .setTitle(R.string.unable_to_connect)
                .setCancelable(false)
                .setPositiveButton(R.string.action_settings,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                context.startActivity(i);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                activity.onBackPressed(); //close dialog
                                //go back to 4th fragment
                                activity.getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                                        new FourthFragment()).addToBackStack("4").commit();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }
}
