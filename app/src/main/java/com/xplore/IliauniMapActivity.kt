package com.xplore

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.xplore.maps.BaseMapActivity
import com.xplore.util.FirebaseUtil
import com.xplore.util.MapUtil

/**
 * Created by Nika on 9/29/2017.
 * TODO write description of this class - what it does and why.
 */

class IliauniMapActivity : BaseMapActivity() {

    companion object {
        private const val ARG_ZOOM_LAT = "zoom_latitude"
        private const val ARG_ZOOM_LNG = "zoom_longitude"

        @JvmStatic
        fun getStartIntent(context: Context, zoomLat: Double, zoomLng: Double): Intent =
                Intent(context, IliauniMapActivity::class.java)
                        .putExtra(ARG_ZOOM_LAT, zoomLat)
                        .putExtra(ARG_ZOOM_LNG, zoomLng)
    }

    private val zoomLat by lazy {
        intent.getDoubleExtra(ARG_ZOOM_LAT, ZOOM_LATITUDE)
    }

    private val zoomLng by lazy {
        intent.getDoubleExtra(ARG_ZOOM_LNG, ZOOM_LONGITUDE)
    }

    // Used to measure checkin
    private val CHECK_IN_DISTANCE = 0.00025

    // Event location & zoom
    private val ZOOM_LATITUDE = 41.710634
    private val ZOOM_LONGITUDE = 44.750900
    override val ZOOM_AMOUNT = 17.8f

    private val TAG = "iliauniMap"

    private var standMarkers = HashMap<String, StandMarker>()

    private fun buildStandMarkerOptions(title: String, standMarker: StandMarker): MarkerOptions {
        val markerOptions = MarkerOptions()
        markerOptions.position(standMarker.getLocation())
        markerOptions.title(title)
        if (standMarker.checkedIn) {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_flag))
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(standMarker.hue))
        }
        return markerOptions
    }

    override fun configureMap(googleMap: GoogleMap) {
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        super.configureMap(googleMap)

        // Move camera to destination
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(zoomLat, zoomLng), ZOOM_AMOUNT)
        googleMap.animateCamera(cameraUpdate)

        // Get all stand info
        FirebaseUtil.standsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(standSnapshot: DataSnapshot?, p1: String?) {
                if (standSnapshot != null) {
                    val temp = standSnapshot.getValue(StandMarker::class.java)
                    if (temp != null) {
                        val standId = standSnapshot.key
                        standMarkers[standId] = temp

                        // Get whether already checked-in
                        val checkedIn = standSnapshot.child(FirebaseUtil.F_CHECKINS)
                                .child(General.currentUserId).getValue(Boolean::class.java)

                        if (checkedIn != null && checkedIn) {
                            standMarkers[standId]?.checkedIn = true
                            Log.i(TAG, "Stand $standId checkedIn = true")
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

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) { }
            override fun onChildChanged(p0: DataSnapshot?, p1: String?) { }
            override fun onChildRemoved(p0: DataSnapshot?) { }
            override fun onCancelled(p0: DatabaseError?) { }
        })
    }

    override val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.let {
                super.onLocationResult(locationResult)
                Log.i(TAG, "Current location: lat:${locationResult.lastLocation.latitude}; lng:${locationResult.lastLocation.longitude}")
                for ((key, value) in standMarkers) {
                    val dist = value.getDistanceFrom(locationResult.lastLocation)
                    Log.i(TAG, "Distance from $key is $dist")

                    if (!value.checkedIn && dist <= CHECK_IN_DISTANCE) {
                        // Upload checkin to Firebase
                        FirebaseUtil.checkIn(key)

                        Toast.makeText(this@IliauniMapActivity, "Checked Into $key!",
                                Toast.LENGTH_SHORT).show() //TODO string resources

                        // Place flag instead of pin
                        value.mapMarker?.setIcon(
                                BitmapDescriptorFactory.fromResource(R.drawable.ic_flag))

                        value.checkedIn = true
                    }
                }
            }
        }
    }

    private class StandMarker (val latitude: Double = MapUtil.DEFAULT_LAT_LNG,
                               val longitude: Double = MapUtil.DEFAULT_LAT_LNG,
                               val hue: Float = MapUtil.getRandomMarkerHue(),
                               var checkedIn: Boolean = false,
                               var mapMarker: Marker? = null) {
        @Exclude
        fun getLocation() = LatLng(latitude, longitude)

        @Exclude
        fun getDistanceFrom(latLng: LatLng): Double {
            return Math.sqrt(
                    Math.pow(latitude - latLng.latitude, 2.0)
                            + Math.pow(longitude - latLng.longitude, 2.0))
        }

        @Exclude
        fun getDistanceFrom(location: Location): Double {
            return Math.sqrt(
                    Math.pow(latitude - location.latitude, 2.0)
                            + Math.pow(longitude - location.longitude, 2.0))
        }
    }
}