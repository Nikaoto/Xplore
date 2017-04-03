package com.explorify.xplore.xplore_demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ENGLISH_LANG_CODE = "en";
    public static final String GEORGIAN_LANG_CODE = "ka";
    public static final String RUSSIAN_LANG_CODE = "ru";

    public static FragmentManager fm;
    public static int[] navMenuItems = new int[6];
    public static int previousNavItemId;
    public static NavigationView navigationView;
    public static Menu menu;
    public static final int RESERVE_NUM = 9;


    private SharedPreferences.Editor prefEditor;
    private SharedPreferences prefs;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitPreferences();
        General.InitDisplayMetrics(this);
        General.InitDBManager(this);
        InitNavMenuItems();
        setContentView(R.layout.activity_main);

        General.InitDisplayMetrics(this);

        //Set initial Fragment
        fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, new FifthFragment()).commit();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_fifth_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        ThirdFragment.MAPS_CLOSED = false;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                HideKeyboard();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                HideKeyboard();
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu_)
    {
        this.menu = menu_;
        return super.onPrepareOptionsMenu(menu);
    }

    //Initializes navigation menu item IDs for backstack
    private void InitNavMenuItems() {
        navMenuItems[1] = R.id.nav_first_layout;
        navMenuItems[2] = R.id.nav_second_layout;
        navMenuItems[3] = R.id.nav_third_layout;
        navMenuItems[4] = R.id.nav_fourth_layout;
        navMenuItems[5] = R.id.nav_fifth_layout;
    }

    public static void RefreshApplication(Context context) { //TODO COMMENT AFTER adding recreate() to optionsFragment
        //Refresh nav items
        navigationView.getMenu().getItem(0).setTitle(R.string.nav_profile_title);
        navigationView.getMenu().getItem(1).setTitle(R.string.nav_lib_title);
        navigationView.getMenu().getItem(2).setTitle(R.string.nav_map_title);
        navigationView.getMenu().getItem(3).setTitle(R.string.nav_party_title);
        navigationView.getMenu().getItem(4).setTitle(R.string.nav_about_title);

        //Refresh navigation header
        TextView tw = (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_app_description);
        tw.setText(R.string.nav_description);

        //Refresh options button
        MenuItem mi = menu.findItem(R.id.action_settings);
        mi.setTitle(R.string.action_settings);
    }

    //Initializes prefrences
    private void InitPreferences() {
        prefs = getSharedPreferences("lang", 0);

        //if the pref is not found (means it's the first bootup)
        if (prefs.getString("lang", "null").equals("null")) {

            prefEditor = getSharedPreferences("lang", 0).edit();
            String config = getResources().getConfiguration().locale.toString().toLowerCase();

            if (config.contains(GEORGIAN_LANG_CODE)) {
                prefEditor.putString("lang", GEORGIAN_LANG_CODE);
            }
            else if(config.contains(RUSSIAN_LANG_CODE)) {
                    prefEditor.putString("lang",RUSSIAN_LANG_CODE);
            }
            else {
                prefEditor.putString("lang", ENGLISH_LANG_CODE);
            }
            prefEditor.commit();
        }
        else {
            //User gets here after switching language for the first time ever
            //Setting the locale to match prefrences
            ChangeLocale(prefs.getString("lang", ENGLISH_LANG_CODE));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    public static void manageBackStack() {
        fm.popBackStack();
        if (fm.getBackStackEntryCount() > 1) {
            FragmentManager.BackStackEntry bse = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2);

            try {
                previousNavItemId = navMenuItems[Integer.parseInt(bse.getName())];
            } catch (NumberFormatException | NullPointerException n) {
                previousNavItemId = R.id.nav_fifth_layout;
            }
        } else {
            //BSE = BackStackEntry
            //if the BSE count is 1 that means the app was just launched, so I just highlight the start page
            previousNavItemId = R.id.nav_fifth_layout;
        }
        navigationView.setCheckedItem(previousNavItemId);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() > 0) {
            manageBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        HideKeyboard();
        if (id == R.id.action_settings) {
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, new OptionsFragment()).addToBackStack(null).commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

/* //TODO May delete this
    public static String[] MergeArrays(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = new String[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_first_layout) {
            fm.beginTransaction().replace(R.id.fragment_container, new FirstFragment()).addToBackStack("1").commit();
        } else if (id == R.id.nav_second_layout) {
            fm.beginTransaction().replace(R.id.fragment_container, new SecondFragment()).addToBackStack("2").commit();
        } else if (id == R.id.nav_third_layout) {
            fm.beginTransaction().replace(R.id.fragment_container, new ThirdFragment()).addToBackStack("3").commit();
        } else if (id == R.id.nav_fourth_layout) {
            fm.beginTransaction().replace(R.id.fragment_container, new FourthFragment()).addToBackStack("4").commit();
        } else if (id == R.id.nav_fifth_layout) {
            fm.beginTransaction().replace(R.id.fragment_container, new FifthFragment()).addToBackStack("5").commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void ChangeLocale(String language_code) { //TODO change this later
        Resources res = MainActivity.this.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language_code.toLowerCase());
        res.updateConfiguration(conf, dm);
    }

    //hides the sotft keyboard
    public void HideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }
}
