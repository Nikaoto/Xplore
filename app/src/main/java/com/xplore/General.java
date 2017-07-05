package com.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nikaoto on 2/24/2017.
 */

public class General {

    //TODO create current User Singleton object

    //==== Account status stuff ===
    public static final int LOGGED_IN = 1;
    public static final int JUST_REGISTERED = 2;
    public static final int NOT_LOGGED_IN = 0;
    public static int accountStatus = NOT_LOGGED_IN;
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

    public static void openReserveInfoFragment(int resId, Context context)
    {
        Intent intent= new Intent(context, ReserveInfoFragment.class);
        intent.putExtra("chosen_element", resId);
        context.startActivity(intent);
    }

    //TODO remove this after switching to UNIX time
    public static int getDateLong(int year, int month, int day) {
        return year*10000 + month*100 + day;
    }

    //TODO remove this after switching to UNIX time
    public static int getDateLong(Long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeStamp));
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        return getDateLong(y, m, d);
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

    //Returns the age of a person
    public static int calculateAge(Long timeStamp, int bYear, int bMonth, int bDay) {
        //Getting current date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timeStamp));
        int nowYear = cal.get(Calendar.YEAR);
        int nowMonth = cal.get(Calendar.MONTH) + 1;
        int nowDay = cal.get(Calendar.DAY_OF_MONTH);

        //Calculating age
        int tempAge = nowYear - bYear;
        if(nowMonth > bMonth)
            tempAge++;
        else if(nowMonth == bMonth && nowDay >= bDay)
            tempAge++;
        else
            tempAge--;

        return tempAge;
    }

    //Returns the age of a person, takes string instead of dates
    private static int calculateAge(Long timeStamp, String birthDate)
    {
        //Check if format is correct
        if(birthDate.length() != 8) {
            Log.println(Log.INFO, "calculateAge", "length != 8");
            return 0;
        }

        //Getting birth date
        int bYear = Integer.valueOf(birthDate.substring(0,4));
        int bMonth = Integer.valueOf(birthDate.substring(4,6));
        int bDay = Integer.valueOf(birthDate.substring(6));

        return calculateAge(timeStamp, bYear, bMonth, bDay);
    }

    //The same, but takes integer as birth date instead
    static int calculateAge(Long timeStamp, int birthDate) {
        return calculateAge(timeStamp, String.valueOf(birthDate));
    }

    //Adds slashes to a date given in int (yyyy.mm.dd) without dots
    public static String putSlashesInDate(int date) {
        String dateStr = String.valueOf(date);
        //Checking if format is correct
        if (dateStr.length() != 8) {
            return "";
        }

        StringBuilder sb = new StringBuilder(dateStr);
        sb.insert(4, "/");
        sb.insert(7, "/");

        return sb.toString();
    }

    //TODO make all dates ints and remove this crap
    static String putSlashesInDate(Long date) {
        return putSlashesInDate(Integer.valueOf(String.valueOf(date)));
    }

    public static void openUserProfile(Activity activity, String userId) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    public static void HideKeyboard(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e){
            Log.println(Log.ERROR, "keyboard", "getWindowToken() in HideKeyboard threw a NPE");
        }
    }

    public static boolean isNetConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
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

    static void dimBehind(PopupWindow popupWindow, float dimAmount) {
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

    public static void popSignInMenu(double xScale, double yScale, View parent, final Activity activity)
    {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        int locationX = 0;
        int locationY = 0;

        View popupView = activity.getLayoutInflater().inflate(R.layout.pre_signin_layout, null);
        popupView.setBackgroundResource(R.drawable.popup_fade_background);
        popupView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.slide_down_open));

        final PopupWindow popupWindow = new PopupWindow(popupView, popWidth, popHeight, true);

        //Disables dismissal on outside touch
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);

        //Goes back if dismissed
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() { //TODO change group fragment, this does OnBackPressed() and goes back when dismissed
                activity.getFragmentManager().popBackStack();
            }
        });

        popupWindow.showAtLocation(parent, Gravity.CENTER, locationX, locationY);

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
            }
        });
    }

    public static PopupWindow popLoadingBar(double xScale, double yScale, Activity activity, View view)
    {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        View popupView = activity.getLayoutInflater().inflate(R.layout.loading_layout, null);


        PopupWindow popupWindow = new PopupWindow(popupView, popWidth, popHeight, true);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        dimBehind(popupWindow, 0.65f);

        return popupWindow;
    }
}