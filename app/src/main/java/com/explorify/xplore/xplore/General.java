package com.explorify.xplore.xplore;

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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by nikao on 2/24/2017.
 */

public class General {

    //==== Account status stuff ===
    public static final int SIGNED_IN = 1;
    public static final int REGISTERED = 2;
    public static int accountStatus = 0;
    //=================

    public static int appWidth, appHeight;
    public static String currentUserId;
    public static String DB_TABLE; //The language table to use from database

    public static void InitDisplayMetrics(Activity activity)
    {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        appWidth = dm.widthPixels;
        appHeight = dm.heightPixels;
    }

    public static void setCurrentTable(Context context)
    {
        if(context != null)
            DB_TABLE = context.getSharedPreferences("lang",0).getString("lang","en");
        else
            DB_TABLE = "en";
    }

    public static void OpenLibFragment(int resId, Context context)
    {
        Intent intent= new Intent(context, LibFragment.class);
        intent.putExtra("chosen_element", resId);
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

    public static int calculateAge(Long tempTimeStamp, int birthDate)
    {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(tempTimeStamp));
        String bDate = String.valueOf(birthDate);

        int bYear = Integer.valueOf(bDate.substring(0,4));
        int bMonth = Integer.valueOf(bDate.substring(4,6));
        int bDay = Integer.valueOf(bDate.substring(6));

        int nowYear = cal.get(Calendar.YEAR);
        int nowMonth = cal.get(Calendar.MONTH) + 1;
        int nowDay = cal.get(Calendar.DAY_OF_MONTH);

        //Calculating age
        int tempAge = nowYear - bYear;
        if(bMonth > nowMonth)
            tempAge++;
        else if(bMonth == nowMonth && bDay >= nowDay)
            tempAge++;

        return tempAge;
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

    public static void popSignInMenu(double xScale, double yScale, View myView, final Activity activity)
    {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        int locationX = 0;
        int locationY = 0;

        View popupView = activity.getLayoutInflater().inflate(R.layout.pre_signin_layout, null);
        popupView.setBackgroundResource(R.drawable.mr_dialog_material_background_light);
        popupView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_down_open));

        final PopupWindow popupWindow = new PopupWindow(popupView, popWidth, popHeight, true);

        //Disables dismissal on outside touch
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        //Goes back if dismissed
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                MainActivity.manageBackStack(activity.getFragmentManager());
            }
        });

        popupWindow.showAtLocation(myView, Gravity.CENTER, locationX, locationY);

        dimBehind(popupWindow, 0.65f);

        //Sign-in Button Config
        Button signin_btn = (Button) popupView.findViewById(R.id.signin_btn);
        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (General.isNetConnected(activity)) {
                    popupWindow.dismiss();
                    Intent i = new Intent(activity, GoogleSignInActivity.class); //TODO change into SignInActivity
                    activity.startActivity(i);
                } else
                    createNetErrorDialog(activity);
            }
        });

        //No sign-in textview config
        TextView no_signin_tw = (TextView) popupView.findViewById(R.id.no_signin_tw);
        no_signin_tw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popupWindow.dismiss();
                MainActivity.manageBackStack(activity.getFragmentManager());
            }
        });
    }
}
