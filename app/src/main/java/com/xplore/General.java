package com.xplore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.xplore.account.SignInActivity;
import com.xplore.reserve.ReserveInfoActivity;
import com.xplore.user.UserProfileActivity;

import java.util.Calendar;
import java.util.Date;

import kotlin.jvm.JvmOverloads;

/**
 * Created by Nikaoto on 2/24/2017.
 */

public class General {

    //TODO create current User Singleton object

    /*==== Account status stuff ==== */
    public static final int LOGGED_IN = 1;
    public static final int JUST_LOGGED_IN = 3;
    public static final int NOT_LOGGED_IN = 0;
    public static final int NOT_REGISTERED = 4;

    // Used for managing notifications at MainAct
    public static int accountStatus = NOT_LOGGED_IN; // TODO account
    //=================

    private static int appWidth, appHeight;
    public static String currentUserId;

    // TODO account
    public static String PREFS_REGISTRATION = "prefs_registration";
    public static String PREFS_FULLY_REGISTERED = "fully_registered";
    // TODO account
    public static boolean hasFinishedRegistration(Context context) {
        return context.getSharedPreferences(PREFS_REGISTRATION, 0)
                .getBoolean(PREFS_FULLY_REGISTERED, false);
    }
    // TODO account
    public static void setRegistrationFinished(Context context, boolean finished) {
        context.getSharedPreferences(PREFS_REGISTRATION, 0)
                .edit()
                .putBoolean(PREFS_FULLY_REGISTERED, finished)
                .commit();
    }

    // TODO account
    public static boolean isUserFullyRegistered(Context context) {
        return context.getSharedPreferences(PREFS_REGISTRATION, 0)
                .getBoolean(PREFS_FULLY_REGISTERED, false);
    }

    public static void initDisplayMetrics(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        appWidth = dm.widthPixels;
        appHeight = dm.heightPixels;
    }

    // Refreshes currentUserId and the account status
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

    public static void openReserveInfoFragment(int resId, Context context) {
        Intent intent= new Intent(context, ReserveInfoActivity.class);
        intent.putExtra("chosen_element", resId);
        context.startActivity(intent);
    }

    public static Long convertIntDateToTimeStamp(String date) {
        if (date.length() != 8) {
            return 0L;
        }

        StringBuilder sb = new StringBuilder(date);
        final int y = Integer.valueOf(sb.substring(0, 4));
        final int m = Integer.valueOf(sb.substring(4, 6));
        final int d = Integer.valueOf(sb.substring(6));

        Calendar cal = Calendar.getInstance();
        cal.set(y, m - 1, d);
        return cal.getTimeInMillis();
    }

    public static Long convertIntDateToTimeStamp(int date) {
        return convertIntDateToTimeStamp(String.valueOf(date));
    }

    //TODO remove this after switching to UNIX time
    public static int getDateInt(int year, int month, int day) {
        return year*10000 + month*100 + day;
    }

    //TODO remove this after switching to UNIX time
    // Gets long date from unix long milliseconds
    public static int getDateInt(Long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(timeStamp));
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH) + 1;
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        return getDateInt(y, m, d);
    }

    public static boolean isUserLoggedIn() {
        return accountStatus != NOT_LOGGED_IN && !currentUserId.isEmpty();
    }

    // DOES NOT RETURN CORRECT ANSWER, USED FOR SOMETHING ELSE
    public static boolean isUserSignedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            currentUserId = user.getUid();
            // Try to authenticate with firebase
            return true;
        } else {
            return false;
        }
    }

    // Returns the age of a person
    public static int calculateAge(Long timeStamp, int bYear, int bMonth, int bDay) {
        // Get current date
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timeStamp));
        int nowYear = cal.get(Calendar.YEAR);
        int nowMonth = cal.get(Calendar.MONTH) + 1;
        int nowDay = cal.get(Calendar.DAY_OF_MONTH);

        // Calculate age
        int tempAge = nowYear - bYear;
        if(nowMonth > bMonth)
            tempAge++;
        else if(nowMonth == bMonth && nowDay >= bDay)
            tempAge++;
        else
            tempAge--;

        return tempAge;
    }

    // Returns the age of a person, takes string instead of dates
    private static int calculateAge(Long timeStamp, String birthDate) {
        // Check if format is correct
        if(birthDate.length() != 8) {
            Log.println(Log.INFO, "calculateAge", "length != 8");
            return 0;
        }

        // Get birth date
        int bYear = Integer.valueOf(birthDate.substring(0,4));
        int bMonth = Integer.valueOf(birthDate.substring(4,6));
        int bDay = Integer.valueOf(birthDate.substring(6));

        return calculateAge(timeStamp, bYear, bMonth, bDay);
    }

    // The same, but takes integer as birth date instead
    public static int calculateAge(Long timeStamp, int birthDate) {
        return calculateAge(timeStamp, String.valueOf(birthDate));
    }

    // Gets the difference of given dates in days (doesn't need to be accurate)
    public static int getDateDiffInDays(int now, int then) {
        if (now == then) {
            return 0;
        }
        // Now dates
        final int nowYear = Integer.valueOf(String.valueOf(now).substring(0, 4));
        final int nowMonth = Integer.valueOf(String.valueOf(now).substring(4, 6));
        final int nowDay = Integer.valueOf(String.valueOf(now).substring(6));
        final int nowInDays = nowDay + nowMonth * 30 + nowYear * 365;

        // Then dates
        final int thenYear = Integer.valueOf(String.valueOf(then).substring(0, 4));
        final int thenMonth = Integer.valueOf(String.valueOf(then).substring(4, 6));
        final int thenDay = Integer.valueOf(String.valueOf(then).substring(6));
        final int thenInDays = thenDay + thenMonth * 30 + thenYear * 365;

        return thenInDays - nowInDays;
    }

    public static String putColonInTime(String time) {
        if (time == null || time.isEmpty()) {
            return "";
        } else {
            return time.substring(0, 2) + ":" + time.substring(2);
        }
    }

    public static void openUserProfile(Activity activity, String userId) {
        if (userId == null) {
            activity.startActivity(UserProfileActivity.newIntent(activity, currentUserId));
        } else {
            activity.startActivity(UserProfileActivity.newIntent(activity, userId));
        }
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
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
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
                activity.onBackPressed();
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
                } else {
                    createNetErrorDialog(activity);
                }
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

    public static void toastReputationGain(Context context, int reputationAmount) {
        Resources res = context.getResources();
        Toast.makeText(context, res.getString(R.string.you_gained) + " " + reputationAmount + " "
                        + res.getString(R.string.you_gained_rep_suffix), Toast.LENGTH_SHORT).show();
    }

    public static void vibrateDevice(Context context, Long time) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            v.vibrate(time);
        }
    }

    public static void vibrateDevice(Context context) {
        vibrateDevice(context, 20L);
    }
}
