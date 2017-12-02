package com.xplore.maps

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.xplore.General
import com.xplore.util.FirebaseUtil.F_GROUP_NAME
import com.xplore.util.FirebaseUtil.F_LOCATIONS
import com.xplore.util.FirebaseUtil.F_LATITUDE
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
 *
 */

class GroupMapActivity : BaseMapActivity() {

    private val TAG = "gmap"
    private val NO_GROUP_ID = " "

    companion object {
        //Arguments
        private const val ARG_GROUP_ID = "groupId"
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

        //When opening group
        @JvmStatic
        fun getStartIntent(context: Context, zoomToDestination: Boolean, groupId: String,
                           destinationName: String, destinationLat: Double,
                           destinationLng: Double): Intent {
            return getStartIntent(context, zoomToDestination, destinationName, destinationLat,
                    destinationLng)
                    .putExtra(ARG_GROUP_ID, groupId)
        }
    }

    // Passed variables
    private val groupId: String by lazy {
        getPassedGroupId()
    }
    private fun getPassedGroupId(): String {
        if (intent.getStringExtra(ARG_GROUP_ID) == null) {
            return NO_GROUP_ID
        } else {
            return intent.getStringExtra(ARG_GROUP_ID)
        }
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

    private val groupLocationsRef: DatabaseReference by lazy {
        getGroupRef(groupId).child(F_LOCATIONS)
    }
    private val currentUserLocationRef: DatabaseReference by lazy {
        groupLocationsRef.child(General.currentUserId)
    }

    // Markers for member tracking
    private val mapMarkers = HashMap<String, Marker>()
    // Holds references to each members' location so we can disable them OnDestroy()
    private val listenerMap = HashMap<String, ChildEventListener>()

    override var shouldStopLocationUpdatesOnDestroy: Boolean = false //TODO : TESTING FOR NOW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (groupId != NO_GROUP_ID) {
            firstUploadData()
        }
    }

    /* Uploads a UserCard to this user's location node if it doesn't exist.
       When a new user joins a group, a new node isn't created, so we upload a UserMarker in the
       locations node for the first time */
    private fun firstUploadData() {
        currentUserLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(locationSnapshot: DataSnapshot?) {
                if (locationSnapshot == null || !locationSnapshot.hasChild(F_GROUP_NAME)) {
                    getCurrentUserRef().addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            dataSnapshot?.let {
                                val fname = it.child(F_GROUP_NAME).getValue(String::class.java)
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
            //Move camera to destination
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(destinationLocation,
                    ZOOM_AMOUNT)
            googleMap.animateCamera(cameraUpdate)
        }

        if (groupId != NO_GROUP_ID) {
            startListeningForGroupLocations(googleMap)
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

    fun startRequestingLocationUpdates(client: FusedLocationProviderClient,
                                                request: LocationRequest,
                                                callback: LocationCallback,
                                                looper: Looper) {
        // TODO change this
        startService(Intent(this, LocationUpdateService::class.java))
    }

    private fun uploadLocation(location: Location) {
        if (groupId != NO_GROUP_ID) {
            currentUserLocationRef.child(F_LATITUDE).setValue(location.latitude)
            currentUserLocationRef.child(F_LONGITUDE).setValue(location.longitude)
        }
    }

    // Sets up listeners for member locations
    private fun startListeningForGroupLocations(googleMap: GoogleMap) {
        groupLocationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (markerSnapshot in it.children) {
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
                                                    if (data.key == F_LATITUDE) {
                                                        val lng = mapMarkers[key]?.position?.longitude
                                                        mapMarkers[key]?.position= LatLng(it, lng!!)
                                                    } else if (data.key == F_LONGITUDE) {
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

    private fun stopListeningForMemberLocations() {
        if (listenerMap.isNotEmpty()) {
            for (entry in listenerMap) {
                groupLocationsRef.child(entry.key).removeEventListener(entry.value)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListeningForMemberLocations()
    }
}