package com.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
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
import com.xplore.account.SignInActivity;
import com.xplore.reserve.ReserveInfoActivity;
import com.xplore.user.UserProfileActivity;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Nikaoto on 2/24/2017.
 */

public class General {

    //TODO create current User Singleton object

    //==== Account status stuff ===
    public static final int LOGGED_IN = 1;
    public static final int JUST_LOGGED_IN = 3;
    public static final int NOT_LOGGED_IN = 0;

    //Used for managing notifications at MainAct
    public static int accountStatus = NOT_LOGGED_IN;
    //=================

    public static int appWidth, appHeight;
    public static String currentUserId;
    public static String DB_TABLE; //The language table to use from database

    public static void InitDisplayMetrics(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        appWidth = dm.widthPixels;
        appHeight = dm.heightPixels;
    }

    //Refreshes currentUserId and the account status
    public static void refreshAccountStatus() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            accountStatus = LOGGED_IN;
            currentUserId = currentUser.getUid();
        } else {
            accountStatus = NOT_LOGGED_IN;
            currentUserId = "";
        }
    }

    public static void setCurrentTable(Context context) {
        if(context != null) {
            DB_TABLE = context.getSharedPreferences("lang", 0).getString("lang", "en");
        } else {
            DB_TABLE = "en";
        }
    }

    public static void openReserveInfoFragment(int resId, Context context)
    {
        Intent intent= new Intent(context, ReserveInfoActivity.class);
        intent.putExtra("chosen_element", resId);
        context.startActivity(intent);
    }

    //TODO remove this after switching to UNIX time
    public static int getDateLong(int year, int month, int day) {
        return year*10000 + month*100 + day;
    }

    //TODO remove this after switching to UNIX time
    //Gets long date from unix long milliseconds
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
    public static int calculateAge(Long timeStamp, int birthDate) {
        return calculateAge(timeStamp, String.valueOf(birthDate));
    }

    //Gets the difference of given dates in days (doesn't need to be accurate)
    public static int getDateDiffInDays(int now, int then) {
        if (now == then) {
            return 0;
        }
        //Now dates
        final int nowYear = Integer.valueOf(String.valueOf(now).substring(0, 4));
        final int nowMonth = Integer.valueOf(String.valueOf(now).substring(4, 6));
        final int nowDay = Integer.valueOf(String.valueOf(now).substring(6));
        final int nowInDays = nowDay + nowMonth * 30 + nowYear * 365;

        //Then dates
        final int thenYear = Integer.valueOf(String.valueOf(then).substring(0, 4));
        final int thenMonth = Integer.valueOf(String.valueOf(then).substring(4, 6));
        final int thenDay = Integer.valueOf(String.valueOf(then).substring(6));
        final int thenInDays = thenDay + thenMonth * 30 + thenYear * 365;

        return thenInDays - nowInDays;
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
    public static String putSlashesInDate(Long date) {
        return putSlashesInDate(Integer.valueOf(String.valueOf(date)));
    }

    public static String putColonInTime(String time) {
        return time.substring(0, 2) + ":" + time.substring(2);
    }

    public static void openUserProfile(Activity activity, String userId) {
        Intent intent = new Intent(activity, UserProfileActivity.class);
        intent.putExtra("userId", userId);
        activity.startActivity(intent);
    }

    public static void hideKeyboard(Activity context) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager)
                    context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException e){
            Log.println(Log.ERROR, "keyboard", "getWindowToken() in hideKeyboard threw a NPE");
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

    public static void popSignInMenu(double xScale, double yScale, View parent, final Activity activity) {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        int locationX = 0;
        int locationY = 0;

        View popupView = activity.getLayoutInflater().inflate(R.layout.signin_dialog, null);
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
                    Intent i = new Intent(activity, SignInActivity.class); //TODO change into SignInActivity
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

    /*
    public static void popLogInMenu(final Context context, ViewGroup parent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View v = LayoutInflater.from(context).inflate(R.layout.signin_dialog, parent);
        builder.setView(v);
        final AlertDialog ad =  builder.create();
        //TODO ad.OnDismiss()

        //No thanks TextView
        TextView noThanks = (TextView) ad.findViewById(R.id.no_signin_tw);
        noThanks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ad.dismiss();
            }
        });

        //Login / Register Button
        Button logIn = (Button) ad.findViewById(R.id.signin_btn);
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (General.isNetConnected(context)) {
                    ad.dismiss();
                    Intent i = new Intent(context, GoogleSignInActivity.class); //TODO change into SignInActivity
                    context.startActivity(i);
                } else
                    createNetErrorDialog(context);
            }
        });
    }*/


    public static PopupWindow popLoadingBar(double xScale, double yScale, Activity activity) {
        int popWidth = (int) (appWidth * xScale);
        int popHeight = (int) (appHeight * yScale);

        View popupView = activity.getLayoutInflater().inflate(R.layout.loading_layout, null);

        PopupWindow popupWindow = new PopupWindow(popupView, popWidth, popHeight, true);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        dimBehind(popupWindow, 0.65f);

        return popupWindow;
    }

    public static boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static void vibrateDevice(Context context, Long time) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (time != null) {
            v.vibrate(time);
        } else {
            v.vibrate(20L);
        }
    }
}
