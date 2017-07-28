package com.xplore;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.xplore.groups.my.LoadingMyGroupsFragment;
import com.xplore.groups.search.SearchGroupsFragment;
import com.xplore.maps.MapsActivity;
import com.xplore.notifications.BadgeDrawerArrowDrawable;
import com.xplore.notifications.NotificationManager;
import com.xplore.reserve.LibraryFragment;
import com.xplore.settings.SettingsActivity;
import com.xplore.user.User;

import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String ENGLISH_LANG_CODE = "en";
    public static final String GEORGIAN_LANG_CODE = "ka";
    public static final String RUSSIAN_LANG_CODE = "ru";

    //TODO add this in singleton PreferenceManager
    public static boolean languagePrefsChanged = false;

    private NotificationManager notificationManager;

    private DrawerLayout drawer;

    private ImageView userImageView;
    private int userImageViewSize;
    private TextView userFullNameTextView;
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

        //Setting up drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_about);

        //Setting up user profile inside drawer header
        userImageViewSize =
                Math.round(getResources().getDimension(R.dimen.user_profile_image_medium_size));
        View navHeaderView = navigationView.getHeaderView(0);
        userImageView = (ImageView) navHeaderView.findViewById(R.id.drawer_image);
        userFullNameTextView = (TextView) navHeaderView.findViewById(R.id.userFullNameTextView);
        userImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (General.isUserSignedIn()) {
                    General.openUserProfile(MainActivity.this, General.currentUserId);
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), MainActivity.this);
                }
            }
        });
        //

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        //TODO create a timer which checks for notifs every 30s and gives notification + updates badges

        BadgeDrawerArrowDrawable drawerBadge = new BadgeDrawerArrowDrawable(toolbar.getContext());

        notificationManager = new NotificationManager(
                drawerBadge,
                (TextView) MenuItemCompat.getActionView(navigationView.getMenu()
                        .findItem(R.id.nav_my_groups)).findViewById(R.id.myGroupsBadge)
        );

        toggle.setDrawerArrowDrawable(drawerBadge);

        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    //Sets up the user image inside the drawer


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
            //TODO do below code for low-end devices
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

        //Checks if language preferences changed and reloads
        if (languagePrefsChanged) {
            languagePrefsChanged = false;
            recreate();
        }

        //TODO show this toast after exiting register activity
        if (General.accountStatus == General.JUST_REGISTERED) {
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show(); //TODO string resources
            General.accountStatus = General.JUST_LOGGED_IN;
        }

        if (General.accountStatus == General.JUST_LOGGED_IN) {
            notificationManager.reset();
            General.accountStatus = General.LOGGED_IN;
        }

        if (General.accountStatus == General.NOT_LOGGED_IN) {
            notificationManager.disable();
            openHomePage();
        }

        //TODO add boolean to stop redundant loading
        if (userImageView != null) {
            if (General.isUserSignedIn()) {
                refreshUserProfileViews(this);
            } else {
                userFullNameTextView.setVisibility(View.GONE);
                Picasso.with(this)
                        .load(R.drawable.user_default_profile_image)
                        .transform(new CircleTransformation(userImageViewSize, userImageViewSize))
                        .into(userImageView);
            }
        }
    }

    public void refreshUserProfileViews(final Context context) {
        FirebaseDatabase.getInstance().getReference().child("users").orderByKey()
                .equalTo(General.currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && context != null) {
                    User tempUser = dataSnapshot.getChildren().iterator().next().getValue(User.class);
                    Picasso.with(context)
                            .load(tempUser.getProfile_picture_url())
                            .transform(new CircleTransformation(userImageViewSize, userImageViewSize))
                            .into(userImageView);
                    userFullNameTextView.setVisibility(View.VISIBLE);
                    userFullNameTextView.setText(tempUser.getFname()+" "+tempUser.getLname());
                } else {
                    Toast.makeText(context,"Error loading your profile image", Toast.LENGTH_SHORT).show();//TODO String resources
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            openHomePage();
        }
    }

    public void openHomePage() {
        fm.beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_about);
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
            //fm.beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
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
                    General.openUserProfile(MainActivity.this, General.currentUserId);
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), this);
                }
                break;
            }
            case R.id.nav_library : {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, new LibraryFragment()).commit();
                break;
            }
            case R.id.nav_map : {
                startActivity(MapsActivity.getStartIntent(this, false));
                break;
            }
            case R.id.nav_my_groups : {
                if (General.isUserSignedIn()) {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, new LoadingMyGroupsFragment()).commit();
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), this);
                }
                break;
            }
            case R.id.nav_find_create_groups : {
                if (General.isUserSignedIn()) {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, new SearchGroupsFragment()).commit();
                } else {
                    General.popSignInMenu(0.8, 0.6, getCurrentFocus(), this);
                }
                break;
            }
            case R.id.nav_settings : {
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            }
            case R.id.nav_about : {
                fm.beginTransaction()
                        .replace(R.id.fragment_container, new AboutFragment()).commit();
            }
        }
        fm.executePendingTransactions();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
