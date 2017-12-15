package com.xplore.event

import android.location.Location
import android.os.Bundle
import android.widget.ImageButton
import com.google.android.gms.location.LocationRequest
import com.google.firebase.database.FirebaseDatabase
import com.xplore.R
import com.xplore.maps.BaseMapActivity
import com.xplore.maps.LocationUpdater

/**
 * Created by Nika on 12/15/2017.
 *
 * Used to mark locations for events
 *
 */

private class LocationMarkerAct : BaseMapActivity() {

    private val db = FirebaseDatabase.getInstance().reference

    private val activeLocationRequest = LocationRequest()
            .setInterval(1000L)
            .setFastestInterval(500L)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

    private val locationUpdater: LocationUpdater by lazy {
        LocationUpdater(this, activeLocationRequest, { lr -> last_loc = lr.lastLocation })
    }

    //override val layoutId = R.layout.location_mark
    private lateinit var markLocationButton: ImageButton
    private var last_loc = Location("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

/*        markLocationButton = findViewById<ImageButton>(R.id.markLocationButton)
        markLocationButton.setOnClickListener {
            uploadLocation(last_loc)
        }*/
    }

    // Location Requests
    private fun uploadLocation(location: Location) {
        val key = db.child("locs").push().key
        db.child("locs").child(key).child("lat").setValue(location.latitude)
        db.child("locs").child(key).child("lng").setValue(location.longitude)
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
}