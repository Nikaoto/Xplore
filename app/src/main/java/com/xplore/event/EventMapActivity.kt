package com.xplore.event

import android.app.AlertDialog
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.R
import com.xplore.maps.BaseMapActivity
import com.xplore.maps.LocationUpdater
import com.xplore.util.FirebaseUtil

/**
 * Created by Nika on 9/29/2017.
 *
 * Used for temporary expos and events.
 *
 */

class EventMapActivity : BaseMapActivity() {

    // Used to measure checkin
    private val CHECK_IN_DISTANCE = 0.00025

    // Event location & zoom
    private val ZOOM_LATITUDE = 41.742189247800916
    private val ZOOM_LONGITUDE = 44.776381216943264
    override val ZOOM_AMOUNT = 19.5f

    private val zoomLatLng = LatLng(ZOOM_LATITUDE, ZOOM_LONGITUDE)

    private val TAG = "startup-market"

    private var standMarkers = HashMap<String, StandMarker>()
    private var currentLocation = Location("")

    private val locationRequest = LocationRequest()
            .setInterval(3000L)
            .setFastestInterval(1000L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    private val locationUpdater: LocationUpdater by lazy {
        LocationUpdater(this, locationRequest) { locationResult ->
            onLocationUpdate(locationResult)
        }
    }

    private fun buildStandMarkerOptions(title: String, standMarker: StandMarker): MarkerOptions {
        val markerOptions = MarkerOptions()
        Log.i(TAG, "marker: lat=${standMarker.latitude}; lng=${standMarker.longitude}")
        val newloc = LatLng(standMarker.latitude + 0.000161, standMarker.longitude - 0.0000164)
        markerOptions.position(standMarker.getLatLng())
        markerOptions.title(title)
        if (standMarker.checkedIn) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag))
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(standMarker.hue))
        }
        return markerOptions
    }

    private fun buildGroundOverlayOptions(resId: Int): GroundOverlayOptions {
        val goo = GroundOverlayOptions()
        goo.image(BitmapDescriptorFactory.fromResource(resId))
        goo.positionFromBounds(
                LatLngBounds(
                        LatLng(41.742117, 44.775877), // South west corner
                        LatLng(41.742512, 44.776728) // North east corner
                ))
        goo.transparency(0.0f)
        return goo
    }

    override fun configureMap(googleMap: GoogleMap) {

        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            General.vibrateDevice(this)
            Log.i(TAG, "mark: lat=${latLng.latitude}; lng=${latLng.longitude}")
        }

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        super.configureMap(googleMap)

        // Add overlay
        googleMap.addGroundOverlay(buildGroundOverlayOptions(R.drawable.startup_market_overlay))

        // Zoom camera to event location
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(zoomLatLng, ZOOM_AMOUNT)
        googleMap.animateCamera(cameraUpdate)

        // Get all stand info
        FirebaseUtil.standsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(standSnapshot: DataSnapshot?, p1: String?) {
                if (standSnapshot != null) {
                    val isMarker = standSnapshot.hasChild("latitude") && standSnapshot.hasChild("longitude")
                    if (isMarker) {
                        val temp = standSnapshot.getValue(StandMarker::class.java)
                        if (temp != null) {
                            val standId = standSnapshot.key

                            standMarkers[standId] = temp

                            // Get whether already checked-in
                            val checkedIn = standSnapshot.child(FirebaseUtil.F_CHECKINS)
                                    .child(General.currentUserId).getValue(Boolean::class.java)

                            if (checkedIn != null && checkedIn) {
                                standMarkers[standId]?.checkedIn = true
                                Log.i(TAG, "Stand $standId checkedIn == true")
                            } else {
                                Log.i(TAG, "Stand $standId checkedIn == false")
                            }

                            // Add marker to map
                            standMarkers[standId]!!.mapMarker = googleMap.addMarker(
                                    buildStandMarkerOptions(standId, standMarkers[standId]!!))
                        } else {
                            Log.i(TAG, "standMarkers == null")
                            finish()
                        }
                    }
                }
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) { }
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) { }
            override fun onChildRemoved(p0: DataSnapshot?) { }
            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    override fun onResume() {
        super.onResume()

        if (permissionsGranted()) {
            locationUpdater.start()
        }
    }

    override fun onPause() {
        super.onPause()

        if (permissionsGranted()) {
            locationUpdater.stop()
        }
    }

    private fun onLocationUpdate(locationResult: LocationResult?) {
        locationResult?.let {
            Log.i(TAG, "Current location: lat:${it.lastLocation.latitude}; lng:${it.lastLocation.longitude}")
            for ((key, standMarker) in standMarkers) {
                val dist = standMarker.getDistanceFrom(it.lastLocation)

                Log.i(TAG, "Distance from $key is $dist")

                if (!standMarker.checkedIn && dist <= CHECK_IN_DISTANCE) {
                    // Upload checkin to Firebase
                    FirebaseUtil.checkIn(key)

                    Toast.makeText(this@EventMapActivity, "თქვენ დაჩეკინდით $key-ში!",
                            Toast.LENGTH_SHORT).show()

                    // Place flag instead of pin
                    standMarker.mapMarker?.setIcon(
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_flag))

                    standMarker.checkedIn = true

                    if (everyStandCheckedIn()) {
                        showVictoryMessage()
                    }
                }
            }
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                Log.i(TAG, "Current location: lat:${it.lastLocation.latitude}; lng:${it.lastLocation.longitude}")
                for ((key, value) in standMarkers) {
                    val dist = value.getDistanceFrom(it.lastLocation)
                    Log.i(TAG, "Distance from $key is $dist")

                    if (!value.checkedIn && dist <= CHECK_IN_DISTANCE) {
                        // Upload checkin to Firebase
                        FirebaseUtil.checkIn(key)

                        Toast.makeText(this@EventMapActivity, "Checked Into $key!",
                                Toast.LENGTH_SHORT).show()

                        // Place flag instead of pin
                        value.mapMarker?.setIcon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_flag))

                        value.checkedIn = true

                        if (everyStandCheckedIn()) {
                            showVictoryMessage()
                        }
                    }
                }
            }
        }
    }

    private fun everyStandCheckedIn(): Boolean {
        for ((_, value) in standMarkers) {
            if (!value.checkedIn) {
                return false
            }
        }
        return true
    }

    private fun showVictoryMessage() {
        FirebaseUtil.uploadWinnerId(General.currentUserId)
        AlertDialog.Builder(this@EventMapActivity)
                .setTitle("ყოჩაღ!")
                .setMessage("შენ დაჩექინდი ყველა მონიშნულ სტენდში. " +
                        "დაბრუნდი Xplore-ის სტენდთან რომ მიიღო ექსპლორის მაისურის " +
                        "მოგების შანსი!")
                .setPositiveButton("[ორ წამში მანდ ვარ!]", null)
                .setNegativeButton("[კაი რა მაისური, ყველაფერს გაფიცებ]", null)
                .create().show()
    }
}