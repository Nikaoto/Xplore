package com.xplore;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.FragmentManager;
import android.support.annotation.NonNull;
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

import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ENGLISH_LANG_CODE = "en";
    public static final String GEORGIAN_LANG_CODE = "ka";
    public static final String RUSSIAN_LANG_CODE = "ru";
    public static final int RESERVE_NUM = 9; //TODO change to dbManager.getRowCount()

    private DrawerLayout drawer;
    private int[] navMenuItems = new int[6];
    private int previousNavItemId;
    private int backstackEntryCount = 1;
    private NavigationView navigationView;
    private FragmentManager fm;
    private SharedPreferences.Editor prefEditor;
    private SharedPreferences prefs;
    private Menu menu; //TODO check if this is needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitPreferences(this);
        General.setCurrentTable(this);
        InitNavMenuItems();
        setContentView(R.layout.activity_main);
        General.InitDisplayMetrics(this);

        //Set initial Fragment
        fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();

        //Check for back presses and manage backstack automatically
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int entryCount = fm.getBackStackEntryCount();
                //if entry removed (back pressed, fragment closed)
                if(backstackEntryCount > entryCount){
                    if (entryCount > 0) {
                        FragmentManager.BackStackEntry lastEntry = fm.getBackStackEntryAt(entryCount - 1);
                        try {
                            previousNavItemId = navMenuItems[Integer.parseInt(lastEntry.getName())];
                        } catch (NumberFormatException | NullPointerException n) {
                            previousNavItemId = R.id.nav_fifth_layout;
                        }
                    } else {
                        //if the BSE count is 0 that means only 1 fragment was selected
                        previousNavItemId = R.id.nav_fifth_layout;
                    }
                    navigationView.setCheckedItem(previousNavItemId);
                }
                backstackEntryCount = entryCount;
            }
        });

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

        MapFragment.MAPS_CLOSED = false;

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                General.HideKeyboard(MainActivity.this);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                General.HideKeyboard(MainActivity.this);
            }
        };

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
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

    //Initializes prefrences
    private void InitPreferences(Context context) {
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
            ChangeLocale(prefs.getString("lang", ENGLISH_LANG_CODE), context);
        }
    }

    private void ChangeLocale(String language_code, Context context) { //TODO change this later
        Resources res = MainActivity.this.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(language_code.toLowerCase());
        res.updateConfiguration(conf, dm);
        General.setCurrentTable(context);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //No call for super(). Bug on API Level > 11.
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        /*} else if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();*/
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
        General.HideKeyboard(MainActivity.this);
        if (id == R.id.action_settings) {
            fm.beginTransaction().replace(R.id.fragment_container, new OptionsFragment()).addToBackStack(null).commit();
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //TODO change the nav item names
        switch (id){
            case R.id.nav_first_layout : {
                fm.beginTransaction().replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack("1").commit();
                break;
            }
            case R.id.nav_second_layout : {
                fm.beginTransaction().replace(R.id.fragment_container, new LibraryFragment())
                        .addToBackStack("2").commit();
                break;
            }
            case R.id.nav_third_layout : {
                fm.beginTransaction().replace(R.id.fragment_container, new MapFragment())
                        .addToBackStack("3").commit();
                break;
            }
            case R.id.nav_fourth_layout : {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, new GroupMenuFragment())
                        .addToBackStack("4").commit();
                break;
            }
            case R.id.nav_fifth_layout : {
                fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment())
                        .addToBackStack("4").commit();
                break;
            }
        }
        getFragmentManager().executePendingTransactions();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
