package com.xplore.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_LOCATION_CODE = 1140;

    private GoogleMap googleMap;
    private final int ZOOM_AMOUNT = 15;
    private LocationManager locationManager;

    private Location lastLocation;
    private Marker currLocationMarker;
    private Marker reserveMarker;
    private SharedPreferences prefs;
    private ImageButton KMLButton;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private SupportMapFragment mapFragment;

    private boolean showReserve;

    private LatLng reserveLocation;
    private String reserveName;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MapsActivity.class);
    }

    //When choosing destination for group
    public static Intent getStartIntent(Context context, Boolean choosingDestination) {
        return new Intent(context, MapsActivity.class)
                .putExtra("choosingDestination", choosingDestination);
    }

    //When showing reserve
    public static Intent getStartIntent(Context context, Boolean showReserve,
                                        String reserveName, double lat, double lng) {
        return new Intent(context, MapsActivity.class)
                .putExtra("showReserve", showReserve)
                .putExtra("reserveName", reserveName)
                .putExtra("reserveLat", lat)
                .putExtra("reserveLng", lng);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setTitle(R.string.activity_maps_title);

        Intent intent = getIntent();
        showReserve = intent.getBooleanExtra("showReserve", false);

        initReserve();

        prefs = getSharedPreferences("firstBoot",0);

        //Check in prefs if first boot
        if(prefs.getBoolean("firstBoot",true)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstBoot",false);
            editor.commit();

/*
            if(!isLocationEnabled(getApplicationContext())) {
                createLocationDialog();
            }
*/

            if(!General.isNetConnected(MapsActivity.this))
            {
                createNetErrorDialog();
            }
        }
        KMLButton = (ImageButton) findViewById(R.id.KMLButton);
        /*KMLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadKML();
            }//TODO uncomment this and do smart loading
        });
        */
        initMap();
    }

    private void initReserve() {
        Intent intent = this.getIntent();
        showReserve = intent.getBooleanExtra("showReserve", false);

        if(showReserve) {
            reserveName = intent.getStringExtra("reserveName");
            reserveLocation = new LatLng(
                    intent.getDoubleExtra("reserveLat",0),
                    intent.getDoubleExtra("reserveLng",0)
            );
        }
    }

    private void initMap() {
        locationManager = (LocationManager) getSystemService((Context.LOCATION_SERVICE));

        //Check Permissions
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public static boolean isLocationEnabled(Context context) {
        return getLocationMode(context) != Settings.Secure.LOCATION_MODE_OFF;
    }

    private static int getLocationMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF);
    }

    //TODO STOP pestering the user to allow location, if they deny -> show dialog explaining why they should enable it
    //TODO keep pestering the user to turn on location
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //TODO:
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                //(just doing it here for now, note that with this code, no explanation is shown)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE);
            }
            return false;
        }
            return true;
    }

    public void showReserveOnMap(String rName, LatLng loc) {
        //TODO load KML file and remove reserveName
        //Place current location marker
        placeMarker(loc,reserveMarker,rName);

        //move map camera
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
    }

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (reserveMarker != null) {
            reserveMarker.remove();
        }
    }

    private void loadKML() {
        try {
            KmlLayer kmlLayer = new KmlLayer(googleMap, R.raw.testeroni, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        this.googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(!isLocationEnabled(getApplicationContext())) {
                    createLocationDialog();
                }
                checkLocationPermission();

                return false;
            }
        });
        //Init Google Play Serices
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                this.googleMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            this.googleMap.setMyLocationEnabled(true);
        }
    }

    //Building google location api
    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
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
        if(showReserve) {
            showReserveOnMap(reserveName, reserveLocation);
        }
        else {
            startLocationUpdates();
            //move map camera to my position
            if(lastLocation !=null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        createNetErrorDialog();
    }

    private void placeMarker(LatLng location, Marker marker, String markerTitle) {
        MarkerOptions markerOptions = new MarkerOptions();//TODO change markerOptions
        markerOptions.position(location);
        if(markerTitle != null && markerTitle != "")
            markerOptions.title(markerTitle);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));//TODO add color?
        marker = googleMap.addMarker(markerOptions);
    }

    @Override
    public void onLocationChanged(Location location) {
            lastLocation = location;
            if (currLocationMarker != null) {
                currLocationMarker.remove();
            }

            //auto-moving the camera
/*        if(!showReserve) {
            //get current location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));

            //place marker on current location
            //PlaceMarker(latLng,currLocationMarker,"");
        }*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        createNetErrorDialog();
    }
}
