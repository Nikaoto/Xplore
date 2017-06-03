package com.explorify.xplore.xplore;

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
import android.view.View;
import android.widget.Button;

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

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MapsActivity extends AppCompatActivity
        implements OnMapReadyCallback, ConnectionCallbacks, LocationListener, GoogleApiClient.OnConnectionFailedListener {
//TODO also implement OnConnectionFailedListener
    //TODO extend AppCompatActivity, http://stackoverflow.com/questions/34582370/how-can-i-show-current-location-on-a-google-map-on-android-marshmallow/34582595#34582595
    private static final int REQUEST_LOCATION_CODE = 1140;

    private GoogleMap mMap;
    private final int ZOOM_AMOUNT = 15;
    private LocationManager locationManager;

    Location lastLocation;
    Marker currLocationMarker;
    Marker reserveMarker;
    SharedPreferences prefs;
    Button KMLButton;

    protected GoogleApiClient googleApiClient;
    protected LocationRequest locationRequest;
    SupportMapFragment mapFragment;

    private boolean showReserve;

    private LatLng reserveLocation;
    private String reserveName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        InitReserve();

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
        KMLButton = (Button) findViewById(R.id.KMLButton);
        KMLButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoadKML();
            }
        });
        InitMap();
    }

    private void InitReserve()
    {
        Intent intent = this.getIntent();
        showReserve = intent.getBooleanExtra("show_reserve",false);

        if(showReserve)
        {
            reserveName = intent.getStringExtra("reserve_name");
            reserveLocation = new LatLng(
                    intent.getDoubleExtra("reserve_latitude",0),
                    intent.getDoubleExtra("reserve_longitude",0)
            );
        }
    }

    private void InitMap()
    {
        locationManager = (LocationManager) getSystemService((Context.LOCATION_SERVICE));

        //Check Permissions
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CheckLocationPermission();
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
        return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_OFF);
    }

    //TODO STOP pestering the user to allow location, if they deny -> show dialog explaining why they should enable it
    //TODO keep pestering the user to turn on location
    public boolean CheckLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
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

    public void showReserveOnMap(String rName, LatLng loc)
    {
        //TODO load KML file and remove reserveName
        //Place current location marker
        placeMarker(loc,reserveMarker,rName);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
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
            InitMap();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        {
            super.onBackPressed();
            ThirdFragment.MAPS_CLOSED = true;
            ThirdFragment.FIRST_TIME = false;
            if(reserveMarker != null)
                reserveMarker.remove();
            if (getFragmentManager().getBackStackEntryCount() > 0 && !showReserve) { //TODO if this fails, turn back fragManager static in MainAct
                getFragmentManager().popBackStack();
            }
            showReserve = false;
        }
    }

    private void LoadKML()
    {
        try {
            KmlLayer kmlLayer = new KmlLayer(mMap, R.raw.testeroni, getApplicationContext());
            kmlLayer.addLayerToMap();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if(!isLocationEnabled(getApplicationContext())) {
                    createLocationDialog();
                }
                CheckLocationPermission();

                return false;
            }
        });
        //Init Google Play Serices
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

  /*  void requestLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation !=null)
            {
                mMap.setMyLocationEnabled(true);
                //Myl
            }
        }
    }*/

    private void startLocationUpdates()
    {
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
                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        createNetErrorDialog();
    }

    private void placeMarker(LatLng location, Marker marker, String markerTitle)
    {
        MarkerOptions markerOptions = new MarkerOptions();//TODO change markerOptions
        markerOptions.position(location);
        if(markerTitle != null && markerTitle != "")
            markerOptions.title(markerTitle);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));//TODO add color?
        marker = mMap.addMarker(markerOptions);
    }

    @Override
    public void onLocationChanged(Location location)
    {
            lastLocation = location;
            if (currLocationMarker != null) {
                currLocationMarker.remove();
            }

            //auto-moving the camera
/*        if(!showReserve) {
            //get current location
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT));

            //place marker on current location
            //PlaceMarker(latLng,currLocationMarker,"");
        }*/
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        createNetErrorDialog();
    }

}
