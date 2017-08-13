package com.xplore.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;
import com.xplore.General;
import com.xplore.R;

/*
* Created by Nikaoto
*
* აღწერა:
* ეს კლასი ხნის გუგლის რუკას და აწესრიგებს ადგილმდებარეობის ძებნას. ჯერ-ჯერობით სატესტო რეჟიმშია.
*
* Description:
* This class opens a map with google maps api and manages location tracking. For now, it is only in
* experimental mode.
*/

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int ZOOM_AMOUNT = 15;
    private static final int REQUEST_LOCATION_CODE = 1140;

    public static final String RESULT_DEST_LAT = "destinationLat";
    public static final String RESULT_DEST_LNG = "destinationLng";

    private GoogleApiClient googleApiClient;
    private GoogleMap googleMap;
    private Location lastLocation;

    //When choosing destination
    private boolean choosingDestination = false;
    private Marker destinationMarker = null;

    //When viewing reserve
    private boolean showReserve = false;
    private LatLng reserveLocation;
    private String reserveName;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MapActivity.class);
    }

    //When choosing destination for group
    public static Intent getStartIntent(Context context, Boolean choosingDestination) {
        return new Intent(context, MapActivity.class)
                .putExtra("choosingDestination", choosingDestination);
    }

    //When showing reserve
    public static Intent getStartIntent(Context context, Boolean showReserve,
                                        String reserveName, double lat, double lng) {
        return new Intent(context, MapActivity.class)
                .putExtra("showReserve", showReserve)
                .putExtra("reserveName", reserveName)
                .putExtra("reserveLat", lat)
                .putExtra("reserveLng", lng);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        choosingDestination = getIntent().getBooleanExtra("choosingDestination", false);
        setContentView(R.layout.activity_maps);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        checkFirstBoot(getSharedPreferences("firstBoot", 0));

        //TODO KML button onclick w/ smart loading
        ImageButton KMLButton = (ImageButton) findViewById(R.id.KMLButton);

        //choosingDestination = true; //temp
        if (choosingDestination) {
            showReserve = false;
            setTitle(R.string.activity_choose_destination_title);

            KMLButton.setVisibility(View.GONE);
            KMLButton.setEnabled(false);
            showPinDropHelp();
        } else {
            setTitle(R.string.activity_maps_title);
            showReserve = getIntent().getBooleanExtra("showReserve", false);
            if (showReserve) {
                initReserve();
            }
        }

        initMap();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (choosingDestination) {
            getMenuInflater().inflate(R.menu.done, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    //Displays a dialog with instructions to choose destination
    private void showPinDropHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.choose_destination)
                .setMessage(R.string.drop_pin_help)
                .setPositiveButton(R.string.okay, null)
                .create().show();
    }

    private void initReserve() {
        Intent intent = this.getIntent();
        reserveName = intent.getStringExtra("reserveName");
        reserveLocation = new LatLng(
                intent.getDoubleExtra("reserveLat", 0),
                intent.getDoubleExtra("reserveLng", 0));
    }

    //Check in prefs if first boot and start pop net error if not connected
    private void checkFirstBoot(SharedPreferences prefs) {
        if (prefs.getBoolean("fistBoot", true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstBoot", false);
            editor.commit();

            if(!General.isNetConnected(MapActivity.this)) {
                createNetErrorDialog();
            }
        }
    }

    private void initMap() {
        //locationManager = (LocationManager) getSystemService((Context.LOCATION_SERVICE));
        checkLocationPermission();

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i("brejk", "map ready");
        googleMap = map;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setOnMyLocationButtonClickListener(
                new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        checkLocationPermission();
                        if(!isLocationEnabled(getApplicationContext())) {
                            createLocationDialog();
                        }/* else {
                            moveCameraToUser();
                        }*/
                        return false;
                    }
                });

        if (choosingDestination) {
            googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    General.vibrateDevice(MapActivity.this, 20L);
                    if (destinationMarker != null) {
                        destinationMarker.remove();
                    }
                    destinationMarker = placeMarker(getString(R.string.destination), latLng);
                }
            });
        }

        //Start location updates
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                googleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            googleMap.setMyLocationEnabled(true);
        }
    }

    //Building google location api
    protected synchronized void buildGoogleApiClient() {
        Log.i("brejk", "building google api client");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    public static boolean isLocationEnabled(Context context) {
        return getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF;
    }

    private static int getLocationMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
    }

    //TODO STOP pestering the user to allow location, if they deny -> show dialog explaining why they should enable it
    //TODO finish() if location req denied twice
    //Checks location permission if Android version is 5.0+
    public void checkLocationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                //Check if we should show explanation
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    //TODO:
                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    //Prompt the user once explanation has been shown
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_CODE);

                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_LOCATION_CODE);
                }
            }
        }
    }

    public void showReserveOnMap(String name, LatLng location) {
        //TODO smart load KML file
        //Place current location marker
        placeMarker(name, location);

        //move map camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
    }

    //Prompts the user to enable location on the device
    protected void createLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.please_enable_location)
                .setTitle(R.string.enable_location)
                .setCancelable(false)
                .setPositiveButton(R.string.location,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void createNetErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.wifi_connect_dialog_maps)
                .setTitle(R.string.unable_to_connect)
                .setCancelable(false)
                .setPositiveButton(R.string.action_settings,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                onBackPressed();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onPause() {
        //stop location updates when Activity is no longer active
        if (googleApiClient!= null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        initMap();
        super.onResume();
    }

    private void loadKML() {
        try {
            KmlLayer kmlLayer = new KmlLayer(googleMap, R.raw.testeroni, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //TODO let user choose BALANCED POWER ACCURACY or HIGH ACURRACY

        if (googleApiClient.isConnected()){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("brejk", "Connected");
        if(showReserve) {
            showReserveOnMap(reserveName, reserveLocation);
        } else {
            Log.i("brejk", "starting loc updates");
            startLocationUpdates();
            moveCameraToUser();
        }
    }

    public void moveCameraToUser() {
        Log.i("brejk", "moving camera to user");
        //Move camera to user position
        if (lastLocation != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(
                    new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        createNetErrorDialog();
    }

    private Marker placeMarker(String markerTitle, LatLng location) {
        MarkerOptions markerOptions = new MarkerOptions();//TODO change markerOptions
        markerOptions.draggable(true);
        markerOptions.position(location);
        if(markerTitle != null && !markerTitle.isEmpty()) {
            markerOptions.title(markerTitle);
        }
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        return googleMap.addMarker(markerOptions);
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        //auto-moving the camera
        /*if(!showReserve) {
            //get current location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));

            //place marker on current location
            //PlaceMarker(latLng,currLocationMarker,"");
        }*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (choosingDestination) {
            if (item.getItemId() == R.id.action_done) {
                Intent resultIntent = new Intent()
                        .putExtra(RESULT_DEST_LAT, destinationMarker.getPosition().latitude)
                        .putExtra(RESULT_DEST_LNG, destinationMarker.getPosition().longitude);

                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                onBackPressed();
            }
        } else {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        createNetErrorDialog();
    }

    @Override
    protected void onDestroy() {
        if (destinationMarker != null) {
            destinationMarker.remove();
        }
        Fragment f = getFragmentManager().findFragmentById(R.id.map);
        if (f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }
        super.onDestroy();
    }
}
