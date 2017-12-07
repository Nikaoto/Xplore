package com.xplore.maps

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.xplore.General
import com.xplore.R
import com.xplore.util.MapUtil

/**
 * Created by Nika on 9/4/2017.
 * TODO write description of this class - what it does and why.
 */

class SetDestinationMapActivity : BaseMapActivity() {

    companion object {
        const val RESULT_DEST_LAT = "destinationLat"
        const val RESULT_DEST_LNG = "destinationLng"

        private const val DEST_NAME_ARG = "destinationName"
        private const val DEST_LAT_ARG = "destinationLat"
        private const val DEST_LNG_ARG = "destinationLng"
        private const val ARG_MARKER_HUE = "markerHue"

        @JvmStatic
        fun getStartIntent(context: Context) = Intent(context, SetDestinationMapActivity::class.java)

        @JvmStatic
        fun getStartIntent(context: Context, name: String, lat: Double = 0.0, lng: Double = 0.0)
                : Intent {
            return getStartIntent(context)
                    .putExtra(DEST_NAME_ARG, name)
                    .putExtra(DEST_LAT_ARG, lat)
                    .putExtra(DEST_LNG_ARG, lng)
        }

        @JvmStatic
        fun getStartIntent(context: Context, name: String, lat: Double = 0.0, lng: Double = 0.0,
                           markerHue: Float): Intent
                =  getStartIntent(context, name, lat, lng).putExtra(ARG_MARKER_HUE, markerHue)
    }

    private val markerHue: Float by lazy {
        intent.getFloatExtra(ARG_MARKER_HUE, MapUtil.DEFAULT_MARKER_HUE)
    }

    private var destinationMarker: Marker? = null
    private var editingGroup = false

    override val titleId = R.string.activity_choose_destination_title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //KMLButton.visibility = View.GONE
        //KMLButton.isEnabled = false

        configureDestination()
        showPinDropHelp()
    }

    private fun configureDestination() {
        if (intent.getStringExtra(DEST_LAT_ARG) != null) {
            editingGroup = true
        }
    }

    private fun showPinDropHelp() =
            AlertDialog.Builder(this)
                    .setTitle(R.string.choose_destination)
                    .setMessage(R.string.drop_pin_help)
                    .setPositiveButton(R.string.okay, null)
                    .create().show()

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        // Zooms to destination
        if (editingGroup) {
            val destName = intent.getStringExtra(DEST_NAME_ARG)
            val destLocation = LatLng(intent.getDoubleExtra(DEST_LAT_ARG, 0.0),
                    intent.getDoubleExtra(DEST_LNG_ARG, 0.0))

            // Display marker at destination
            destinationMarker = googleMap.addMarker(buildMarker(destLocation, destName))

            // Move camera to destination
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(destLocation, ZOOM_AMOUNT)
            googleMap.animateCamera(cameraUpdate)
        }
        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            General.vibrateDevice(this@SetDestinationMapActivity)
            destinationMarker?.remove()
            destinationMarker = googleMap.addMarker(buildMarker(latLng, getString(R.string.destination)))
        }
    }

    private fun buildMarker(location: LatLng, markerTitle: String?): MarkerOptions {
        val markerOptions = MarkerOptions()
                .draggable(true)
                .position(location)
                .icon(BitmapDescriptorFactory.defaultMarker(markerHue))

        if (markerTitle != null && markerTitle.isNotEmpty()) {
            markerOptions.title(markerTitle)
        }

        return markerOptions
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.done, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_done && destinationMarker != null) {
            val resultIntent = Intent()
                    .putExtra(RESULT_DEST_LAT, destinationMarker!!.position.latitude)
                    .putExtra(RESULT_DEST_LNG, destinationMarker!!.position.longitude)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        destinationMarker?.remove()

        super.onDestroy()
    }
}