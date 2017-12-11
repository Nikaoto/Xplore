package com.xplore.maps.live_hike

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Build
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
import com.xplore.R
import com.xplore.maps.BaseMapActivity
import com.xplore.maps.LocationUpdateService
import com.xplore.maps.LocationUpdater
import com.xplore.maps.UserMarker
import com.xplore.util.FirebaseUtil
import kotlinx.android.synthetic.main.live_hike.*

/**
 * Created by Nik on 12/5/2017.
 * TODO write description of this class - what it does and why.
 */

class LiveHikeMapActivity : BaseMapActivity() {

    private val TAG = "live-hike-act"
    private fun log(s: String) = Log.i(TAG, s)

    // TODO change lat and lng args to latlng
    companion object {
        private const val ARG_GROUP_ID = "groupId"
        private const val ARG_DEST_NAME = "destName"
        private const val ARG_DEST_LAT = "destLat"
        private const val ARG_DEST_LNG = "destLng"

        private const val PREFS_NAME = "com.xplore.maps.LiveHikeMapAct"
        private const val PREFS_AUTO_LIVE_HIKE_ENABLED = "auto-live-hike-enabled"

        @JvmStatic
        fun newIntent(context: Context, groupId: String, destinationName: String,
                      destinationLat: Double, destinationLng: Double): Intent {
            return Intent(context, LiveHikeMapActivity::class.java)
                    .putExtra(ARG_GROUP_ID, groupId)
                    .putExtra(ARG_DEST_NAME, destinationName)
                    .putExtra(ARG_DEST_LAT, destinationLat)
                    .putExtra(ARG_DEST_LNG, destinationLng)
        }
    }

    private val PASSIVE_LOCAITON_REQUEST_INTERVAL = 2000L //* 60 * 2
    private val PASSIVE_LOCAITON_REQUEST_FASTEST_INTERVAL = 1000L //* 30
    private val PASSIVE_LOCAITON_REQUEST_PRIORITY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

    private val ACTIVE_LOCAITON_REQUEST_INTERVAL = 5000L
    private val ACTIVE_LOCAITON_REQUEST_FASTEST_INTERVAL = 1000L
    private val ACTIVE_LOCAITON_REQUEST_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY

    // Passed arguments
    private val groupId: String by lazy {
        intent.getStringExtra(ARG_GROUP_ID)
    }

    private val destinationLocation: LatLng by lazy {
        LatLng(intent.getDoubleExtra(ARG_DEST_LAT, 0.0),
                intent.getDoubleExtra(ARG_DEST_LNG, 0.0))
    }

    private val destinationName: String by lazy {
        intent.getStringExtra(ARG_DEST_NAME)
    }

    // Location Requests
    private fun uploadLocation(location: Location) {
        currentUserLocationRef.child(FirebaseUtil.F_LATITUDE).setValue(location.latitude)
        currentUserLocationRef.child(FirebaseUtil.F_LONGITUDE).setValue(location.longitude)
    }

    private val passiveLocationRequest = LocationRequest()
            .setInterval(PASSIVE_LOCAITON_REQUEST_INTERVAL)
            .setFastestInterval(PASSIVE_LOCAITON_REQUEST_FASTEST_INTERVAL)
            .setPriority(PASSIVE_LOCAITON_REQUEST_PRIORITY)

    private val locationUpdateServiceIntent: Intent by lazy {
        LocationUpdateService.newIntent(this, passiveLocationRequest, groupId,
                destinationName, destinationLocation)
    }

    private val activeLocationRequest = LocationRequest()
            .setInterval(ACTIVE_LOCAITON_REQUEST_INTERVAL)
            .setFastestInterval(ACTIVE_LOCAITON_REQUEST_FASTEST_INTERVAL)
            .setPriority(ACTIVE_LOCAITON_REQUEST_PRIORITY)

    private val locationUpdater: LocationUpdater by lazy {
        LocationUpdater(this, activeLocationRequest, { lr -> uploadLocation(lr.lastLocation) })
    }

    // Markers for member tracking
    private val mapMarkers = HashMap<String, Marker>()
    // Holds references to each members' firebase location listener so we can disable
    private val listenerMap = HashMap<String, ChildEventListener>()

    // Firebase
    private val groupLocationsRef: DatabaseReference by lazy {
        FirebaseUtil.getGroupRef(groupId).child(FirebaseUtil.F_LOCATIONS)
    }
    private val currentUserLocationRef: DatabaseReference by lazy {
        groupLocationsRef.child(General.currentUserId)
    }

    // Prefs
    private val prefs: SharedPreferences by lazy {
        this@LiveHikeMapActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Layout
    override val layoutId = R.layout.live_hike
    override val titleId = R.string.activity_live_hike_title

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("onCreate")

        autoLiveHikeSwitch.isChecked = prefs.getBoolean(PREFS_AUTO_LIVE_HIKE_ENABLED, false)
        autoLiveHikeSwitch.setOnCheckedChangeListener { _, isEnabled ->
            prefs.edit().putBoolean(PREFS_AUTO_LIVE_HIKE_ENABLED, isEnabled).apply()
        }

        firstUploadData()
    }

    /* Uploads a UserCard to this user's location node if it doesn't exist.
    When a new user joins a group, a new location node won't be created, so we upload a UserMarker
    in the locations node with default location (0,0)*/
    private fun firstUploadData() {
        currentUserLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(locationSnapshot: DataSnapshot?) {
                if (locationSnapshot == null || !locationSnapshot.hasChild(FirebaseUtil.F_GROUP_NAME)) {
                    FirebaseUtil.getCurrentUserRef().addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            dataSnapshot?.let {
                                val fname = it.child(FirebaseUtil.F_GROUP_NAME).getValue(String::class.java)
                                if (fname != null) {
                                    currentUserLocationRef.setValue(UserMarker(fname))
                                }
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {}
                    })
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    override fun configureMap(googleMap: GoogleMap) {
        super.configureMap(googleMap)

        // Display marker at destination
        googleMap.addMarker(buildDestinationMarker())

        // Zoom to current user
        locationUpdater.lastLocation?.let {
            val latLng = LatLng(it.latitude, it.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_AMOUNT)
            googleMap.animateCamera(cameraUpdate)
        }

        startListeningForGroupLocations(googleMap)
    }

    override fun onResume() {
        log("onResume")
        super.onResume()

        if (permissionsGranted()) {
            if (!listeningForGroupLocations() && map != null) {
                startListeningForGroupLocations(map!!)
            }
            stopPassiveLocationUpdates()
            startActiveLocationUpdates()
        }
    }

    private fun buildDestinationMarker(): MarkerOptions {
        // TODO add meetup location marker
        val markerOptions = MarkerOptions()
        markerOptions.position(destinationLocation)
        markerOptions.title(destinationName)
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker())
        return markerOptions
    }

    // Sets up listeners for member locations
    private fun startListeningForGroupLocations(googleMap: GoogleMap) {
        log("startListeningForGroupLocations")
        groupLocationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    it.children.forEach { markerSnapshot ->
                        val key = markerSnapshot.key

                        if (key != General.currentUserId) {
                            val marker = markerSnapshot.getValue(UserMarker::class.java)

                            if (marker != null) {
                                // Creating listener for member
                                val listener = object : ChildEventListener {
                                    // Creates new UserMarker from data and puts it into the hashmap
                                    override fun onChildAdded(data: DataSnapshot?, p1: String?) {
                                        Log.i(TAG, "added child")
                                        val mo = MarkerOptions()
                                        mo.title(marker.name)
                                        mo.position(LatLng(marker.latitude, marker.longitude))
                                        mo.icon(BitmapDescriptorFactory.defaultMarker(marker.hue))

                                        if (!mapMarkers.containsKey(key)) {
                                            // Add marker if it's new
                                            mapMarkers.put(key, googleMap.addMarker(mo))
                                        } else {
                                            // Update position
                                            mapMarkers[key]?.position = mo.position
                                        }
                                    }

                                    //Updates markers when data changes
                                    override fun onChildChanged(data: DataSnapshot?, p1: String?) {
                                        data?.let {
                                            it.value?.let {
                                                if (it is Double) {
                                                    if (data.key == FirebaseUtil.F_LATITUDE) {
                                                        val lng = mapMarkers[key]?.position?.longitude
                                                        mapMarkers[key]?.position= LatLng(it, lng!!)
                                                    } else if (data.key == FirebaseUtil.F_LONGITUDE) {
                                                        val lat = mapMarkers[key]?.position?.latitude
                                                        mapMarkers[key]?.position= LatLng(lat!!, it)
                                                    }
                                                } else if (it is UserMarker) {
                                                    mapMarkers[key]?.position = it.getLocation()
                                                }
                                            }
                                        }
                                    }

                                    override fun onChildMoved(p0: DataSnapshot?, p1: String?) {}

                                    override fun onCancelled(p0: DatabaseError?) {}

                                    override fun onChildRemoved(p0: DataSnapshot?) {}
                                }
                                startListeningForMemberLocation(key, listener)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError?) {}
        })
    }

    private fun startListeningForMemberLocation(uId: String, listener: ChildEventListener) {
        listenerMap.put(uId, listener)
        groupLocationsRef.child(uId).addChildEventListener(listenerMap[uId])
        /*TODO this^:
         looks dumb and probably is dumb, but needs testing to confirm that in
         OnDestroy() the correct listeners are removed (referenced from the map and not the method
         argument) */
    }

    private fun listeningForGroupLocations() = listenerMap.isNotEmpty()

    private fun stopListeningForGroupLocations() {
        listenerMap.forEach { entry ->
            groupLocationsRef.child(entry.key).removeEventListener(entry.value)
        }
        listenerMap.clear()
    }

    override fun onStartLocationUpdates() {
        super.onStartLocationUpdates()

        //stopPassiveLocationUpdates()
        //startActiveLocationUpdates()
    }

    private fun startActiveLocationUpdates() {
        locationUpdater.start()
    }

    private fun stopActiveLocationUpdates() {
        locationUpdater.stop()
    }

    private fun startPassiveLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(locationUpdateServiceIntent)
        } else {
            startService(locationUpdateServiceIntent)
        }
    }

    private fun stopPassiveLocationUpdates() {
        stopService(locationUpdateServiceIntent)
    }

    private fun passiveLocationUpdatesEnabled() = prefs.getBoolean(PREFS_AUTO_LIVE_HIKE_ENABLED, false)

    override fun onPause() {
        super.onPause()
        if (permissionsGranted()) {
            stopListeningForGroupLocations()
            stopActiveLocationUpdates()
            if (passiveLocationUpdatesEnabled()) {
                startPassiveLocationUpdates()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!passiveLocationUpdatesEnabled()) {
            stopPassiveLocationUpdates()
        }
    }
}
