package com.xplore.maps

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.xplore.General

/**
 * Created by Nika on 8/20/2017.
 * TODO write description of this class - what it does and why.
 */

class GroupMapActivity : BaseMapActivity() {

    private val TAG = "gmtag"

    private val groupId: String? by lazy { intent.getStringExtra("groupId") }

    //Firebase
    private val F_GROUPS = "groups"
    private val F_LOCATIONS = "locations"
    private val F_LATITUDE = "latitude"
    private val F_LONGITUDE = "longitude"
    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val currentGroupRef: DatabaseReference by lazy {
        firebaseRef.child("$F_GROUPS/$groupId")
    }
    private val groupLocationsRef: DatabaseReference by lazy { currentGroupRef.child(F_LOCATIONS) }
    private val currentUserLocationRef: DatabaseReference by lazy {
        groupLocationsRef.child(General.currentUserId)
    }

    companion object {
        //When opening reserve
        @JvmStatic
        fun getStartIntent(context: Context, zoomToDestination: Boolean, destinationName: String,
                           destinationLat: Double, destinationLng: Double): Intent {
            return Intent(context, GroupMapActivity::class.java)
                    .putExtra("zoomToDestination", zoomToDestination)
                    .putExtra("destinationName", destinationName)
                    .putExtra("destinationLat", destinationLat)
                    .putExtra("destinationLng", destinationLng)
        }

        //When opening group
        @JvmStatic
        fun getStartIntent(context: Context, zoomToDestination: Boolean, groupId: String,
                           destinationName: String, destinationLat: Double,
                           destinationLng: Double): Intent {
            return getStartIntent(context, zoomToDestination, destinationName, destinationLat,
                    destinationLng)
                    .putExtra("groupId", groupId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun buildDestinationMarker(): MarkerOptions {
        val pos = LatLng(intent.getDoubleExtra("destinationLat", 0.0),
                intent.getDoubleExtra("destinationLng", 0.0))
        val markerOptions = MarkerOptions()
        markerOptions.position(pos)
        markerOptions.title(intent.getStringExtra("destinationName"))
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        return markerOptions
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        googleMap.addMarker(buildDestinationMarker())

        if (intent.getBooleanExtra("zoomToDestination", false)) {
            //Move camera to destination
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(
                    LatLng(intent.getDoubleExtra("destinationLat", 0.0),
                            intent.getDoubleExtra("destinationLng", 0.0))))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT.toFloat()))
        }
    }

    override val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                super.onLocationResult(locationResult)

                uploadLocation(locationResult.lastLocation)
            }
        }
    }

    private fun uploadLocation(location: Location) {
        currentUserLocationRef.child(F_LATITUDE).setValue(location.latitude)
        currentUserLocationRef.child(F_LONGITUDE).setValue(location.longitude)
    }
}