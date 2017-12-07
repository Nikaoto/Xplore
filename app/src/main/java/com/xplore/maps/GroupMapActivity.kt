package com.xplore.maps

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.util.FirebaseUtil.F_GROUP_NAME
import com.xplore.util.FirebaseUtil.F_LATITUDE
import com.xplore.util.FirebaseUtil.F_LOCATIONS
import com.xplore.util.FirebaseUtil.F_LONGITUDE
import com.xplore.util.FirebaseUtil.getCurrentUserRef
import com.xplore.util.FirebaseUtil.getGroupRef
import com.xplore.util.MapUtil

/**
 * Created by Nikaoto on 8/20/2017.
 *
 * When viewing reserve or destination -> opens map with marker at reserve
 * When viewing live hike -> opens map and creates listeners for each member and puts them in a
 * hashmap. Creates markers from member location data and puts them in a separate hashmap (each
 * marker has the same key as its listener). Enables each listener and removes them in onDestroy()
 *
 */

class GroupMapActivity : BaseMapActivity() {

    private val TAG = "group-map-act"

    companion object {
        //Arguments
        private const val ARG_DESTINATION_NAME = "destinationName"
        private const val ARG_DESTINATION_LAT = "destinationLat"
        private const val ARG_DESTINATION_LNG = "destinationLng"
        private const val ARG_ZOOM_TO_DESTINATION = "zoomToDestination"
        private const val ARG_MARKER_HUE = "markerHue"

        //When opening reserve
        @JvmStatic
        fun getStartIntent(context: Context, zoomToDestination: Boolean, destinationName: String,
                           destinationLat: Double, destinationLng: Double): Intent {
            return Intent(context, GroupMapActivity::class.java)
                    .putExtra(ARG_ZOOM_TO_DESTINATION, zoomToDestination)
                    .putExtra(ARG_DESTINATION_NAME, destinationName)
                    .putExtra(ARG_DESTINATION_LAT, destinationLat)
                    .putExtra(ARG_DESTINATION_LNG, destinationLng)
        }

        @JvmStatic
        fun getStartIntent(context: Context, zoomToDestination: Boolean, destinationName: String,
                           destinationLat: Double, destinationLng: Double, markerHue: Float)
                : Intent = getStartIntent(context, zoomToDestination, destinationName,
                destinationLat, destinationLng).putExtra(ARG_MARKER_HUE, markerHue)
    }

    private val destinationLocation: LatLng by lazy {
        LatLng(intent.getDoubleExtra(ARG_DESTINATION_LAT, 0.0),
                intent.getDoubleExtra(ARG_DESTINATION_LNG, 0.0))
    }
    private val destinationName: String by lazy {
        intent.getStringExtra(ARG_DESTINATION_NAME)
    }
    private val zoomToDestination: Boolean by lazy {
        intent.getBooleanExtra(ARG_ZOOM_TO_DESTINATION, false)
    }
    private val markerHue: Float by lazy {
        intent.getFloatExtra(ARG_MARKER_HUE, MapUtil.DEFAULT_MARKER_HUE)
    }

    private lateinit var locationUpdater: LocationUpdater

    // Markers for member tracking
    private val mapMarkers = HashMap<String, Marker>()
    // Holds references to each members' location so we can disable them OnDestroy()
    private val listenerMap = HashMap<String, ChildEventListener>()

    override fun onStartLocationUpdates() {
        super.onStartLocationUpdates()

        locationUpdater.start()
    }

    private fun buildDestinationMarker(): MarkerOptions {
        val markerOptions = MarkerOptions()
        markerOptions.position(destinationLocation)
        markerOptions.title(destinationName)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(markerHue))
        return markerOptions
    }

    // Zooms the map to a position
    private fun zoomTo(location: LatLng?, map: GoogleMap) {
        if (location != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(location))
            map.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT))
        }
    }

    override fun configureMap(googleMap: GoogleMap) {
        super.configureMap(googleMap)

        // Display marker at destination
        googleMap.addMarker(buildDestinationMarker())

        // Zoom to destination
        if (zoomToDestination) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(destinationLocation, ZOOM_AMOUNT)
            googleMap.animateCamera(cameraUpdate)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun stopLocationUpdates() {
        super.stopLocationUpdates()

        locationUpdater.stop()
    }
}