package com.xplore.maps

import android.content.Context
import android.content.Intent
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.xplore.util.MapUtil

/**
 * Created by Nikaoto on 8/20/2017.
 *
 * When viewing reserve -> Opens map with marker at reserve
 * When viewing destination -> Opens map with markers at destination and stops (incl. meetup) TODO add stops
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

        //When viewing reserve or marker
        @JvmStatic
        fun newIntent(context: Context, zoomToDestination: Boolean, destinationName: String,
                      destinationLat: Double, destinationLng: Double): Intent {
            return Intent(context, GroupMapActivity::class.java)
                    .putExtra(ARG_ZOOM_TO_DESTINATION, zoomToDestination)
                    .putExtra(ARG_DESTINATION_NAME, destinationName)
                    .putExtra(ARG_DESTINATION_LAT, destinationLat)
                    .putExtra(ARG_DESTINATION_LNG, destinationLng)
        }

        // When marking location on map
        @JvmStatic
        fun newIntent(context: Context, zoomToDestination: Boolean, destinationName: String,
                      destinationLat: Double, destinationLng: Double, markerHue: Float)
                : Intent = newIntent(context, zoomToDestination, destinationName,
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
}