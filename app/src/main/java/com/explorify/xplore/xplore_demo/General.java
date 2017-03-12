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
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by nikao on 2/24/2017.
 */

public class General {

    public static final int JUST_SIGNED_IN = 1;
    public static final int REGISTERED = 2;

    public static DBmanager dbManager;
    public static int appWidth, appHeight;
    public static String currentUserId;
    public static int accountStatus = 0;

    public static void InitDisplayMetrics(Activity activity)
    {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        appWidth = dm.widthPixels;
        appHeight = dm.heightPixels;
    }

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
        if(context != null)
            return context.getSharedPreferences("lang",0).getString("lang","en");
        else
            return "en";
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

        return GetDateLong(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH)+1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
    }

    public static int GetDateLong(int year, int month, int day)
    {
        return year*10000 + month*100 + day;
    }

    public static boolean isUserSignedIn()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            //Try to authenticate with firebase
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNetConnected(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    public static void createNetErrorDialog(final Context context) {
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
                .setNegativeButton(R.string.cancel,null);
        AlertDialog alert = builder.create();
        alert.show();
    }


    public static void dimBehind(PopupWindow popupWindow, float dimAmount) {
        View container;
        if (popupWindow.getBackground() == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent();
            } else {
                container = popupWindow.getContentView();
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container = (View) popupWindow.getContentView().getParent().getParent();
            } else {
                container = (View) popupWindow.getContentView().getParent();
            }
        }
        Context context = popupWindow.getContentView().getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
        p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        p.dimAmount = dimAmount;
        wm.updateViewLayout(container, p);
    }
}
