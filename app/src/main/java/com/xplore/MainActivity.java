package com.xplore;

import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.xplore.groups.GroupMenuFragment;
import com.xplore.maps.MapFragment;
import com.xplore.user.UserProfileActivity;

import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ENGLISH_LANG_CODE = "en";
    public static final String GEORGIAN_LANG_CODE = "ka";
    public static final String RUSSIAN_LANG_CODE = "ru";

    private DrawerLayout drawer;
    private int[] navMenuItems = {
            R.id.nav_profile,
            R.id.nav_library,
            R.id.nav_map,
            R.id.nav_my_groups,
            R.id.nav_find_create_groups,
            R.id.nav_settings
    };
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
        setContentView(R.layout.activity_main);
        General.InitDisplayMetrics(this);
        General.refreshAccountStatus();

        //Set initial Fragment
        fm = getFragmentManager();
        fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();

/*        //Check for back presses and manage backstack automatically
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int entryCount = fm.getBackStackEntryCount();
                //if entry removed (back pressed or fragment closed)
                if (backstackEntryCount > entryCount) {
                    if (entryCount > 0) {
                        FragmentManager.BackStackEntry lastEntry = fm.getBackStackEntryAt(entryCount - 1);
                        try {
                            previousNavItemId = navMenuItems[Integer.parseInt(lastEntry.getName())];
                        } catch (NumberFormatException | NullPointerException n) {
                            previousNavItemId = R.id.nav_settings;
                        }
                    } else {
                        //if the BSE count is 0 that means only 1 fragment was selected
                        previousNavItemId = R.id.nav_settings;
                    }
                    navigationView.setCheckedItem(previousNavItemId);
                }
                backstackEntryCount = entryCount;
            }
        });*/

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_settings);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

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
    public boolean onPrepareOptionsMenu(Menu menu_) {
        this.menu = menu_;
        return super.onPrepareOptionsMenu(menu);
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
    protected void onResume() {
        super.onResume();
        //TODO show this toast after exitting register activity
        if (General.accountStatus == General.JUST_REGISTERED) {
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show(); //TODO string resources
            General.accountStatus = General.LOGGED_IN;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        /*} else if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();*/
        } else {
            openHomePage();
        }
    }

    public void openHomePage() {
        fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_settings);
        fm.executePendingTransactions();
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
            fm.beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).addToBackStack(null).commit();
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
            case R.id.nav_profile : {
                if (General.isUserSignedIn()) {
                    Intent intent = new Intent(this, UserProfileActivity.class);
                    intent.putExtra("userId", General.currentUserId);
                    startActivity(intent);
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), this);
                }
                break;
            }
            case R.id.nav_library : {
                fm.beginTransaction().replace(R.id.fragment_container, new LibraryFragment())
                        .addToBackStack("2").commit();
                break;
            }
            case R.id.nav_map : {
                fm.beginTransaction().replace(R.id.fragment_container, new MapFragment())
                        .addToBackStack("3").commit();
                break;
            }
            case R.id.nav_my_groups :case R.id.nav_find_create_groups : { //TODO add my groups
                if (General.isUserSignedIn()) {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, new GroupMenuFragment())
                            .addToBackStack("4").commit();
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), this);
                }
                break;
            }
            case R.id.nav_settings : {
                fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment())
                        .addToBackStack("4").commit();
                break;
            }
        }
        fm.executePendingTransactions();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
