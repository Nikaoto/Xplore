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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.xplore.General

/**
 * Created by Nikaoto on 8/20/2017.
 * TODO write description of this class - what it does and why.
 */

class GroupMapActivity : BaseMapActivity() {

    private val TAG = "gmap"

    //Markers for member tracking
    private val mapMarkers = HashMap<String, Marker>()
    //Holds references to each members' location so we can disable them OnDestroy()
    private val listenerMap = HashMap<String, ChildEventListener>()

    private val groupId: String? by lazy {
        intent.getStringExtra(ARG_GROUP_ID)
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

    //Firebase
    private val F_GROUPS = "groups"
    private val F_LOCATIONS = "locations"
    private val F_LATITUDE = "latitude"
    private val F_LONGITUDE = "longitude"
    private val F_USERS = "users"
    private val F_FNAME = "fname"
    private val F_NAME = "name"
    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val currentUserRef = firebaseRef.child(F_USERS).child(General.currentUserId)
    private val currentGroupRef: DatabaseReference by lazy {
        firebaseRef.child("$F_GROUPS/$groupId")
    }
    private val groupLocationsRef: DatabaseReference by lazy {
        currentGroupRef.child(F_LOCATIONS)
    }
    private val currentUserLocationRef: DatabaseReference by lazy {
        groupLocationsRef.child(General.currentUserId)
    }

    companion object {
        //Arguments
        private const val ARG_GROUP_ID = "groupId"
        private const val ARG_DESTINATION_NAME = "destinationName"
        private const val ARG_DESTINATION_LAT = "destinationLat"
        private const val ARG_DESTINATION_LNG = "destinationLng"
        private const val ARG_ZOOM_TO_DESTINATION = "zoomToDestination"

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (groupId != null) {
            firstUploadData()
        }
    }

    /* Uploads a UserCard to this user's location node if it doesn't exist.
       When a new user joins a group, a new node isn't created, so we upload a UserMarker in the
       locations node for the first time */
    private fun firstUploadData() {
        currentUserLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(locationSnapshot: DataSnapshot?) {
                if (locationSnapshot == null || !locationSnapshot.hasChild(F_NAME)) {
                    currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot?) {
                            dataSnapshot?.let {
                                val fname = it.child(F_FNAME).getValue(String::class.java)
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
        markerOptions.title(intent.getStringExtra(destinationName))
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
        return markerOptions
    }

    //Zooms the map to a position
    private fun zoomTo(location: LatLng?, map: GoogleMap) {
        if (location != null) {
            map.moveCamera(CameraUpdateFactory.newLatLng(location))
            map.animateCamera(CameraUpdateFactory.zoomTo(ZOOM_AMOUNT.toFloat()))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        //Displays marker at destinaiton
        googleMap.addMarker(buildDestinationMarker())
        //Zooms to destination
        if (zoomToDestination) {
            //Move camera to destination
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(destinationLocation,
                    ZOOM_AMOUNT.toFloat())
            googleMap.animateCamera(cameraUpdate)
        }

        if (groupId != null) {
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

    private fun uploadLocation(location: Location) {
        if (groupId != null) {
            currentUserLocationRef.child(F_LATITUDE).setValue(location.latitude)
            currentUserLocationRef.child(F_LONGITUDE).setValue(location.longitude)
        }
    }

    //Sets up listeners for member locations
    private fun startListeningForGroupLocations(googleMap: GoogleMap) {
        groupLocationsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                dataSnapshot?.let {
                    for (markerSnapshot in it.children) {
                        val key = markerSnapshot.key

                        if (key != General.currentUserId) {
                            val marker = markerSnapshot.getValue(UserMarker::class.java)

                            if (marker != null) {
                                val listener = object : ChildEventListener {
                                    //Creates new UserMarker from data and puts it into the hashmap
                                    override fun onChildAdded(data: DataSnapshot?, p1: String?) {
                                        Log.i(TAG, "added child")
                                        val mo = MarkerOptions()
                                        mo.title(marker.name)
                                        mo.position(LatLng(marker.latitude, marker.longitude))
                                        mo.icon(BitmapDescriptorFactory.defaultMarker(marker.hue))

                                        if (!mapMarkers.containsKey(key)) {
                                            //Add marker if it's new
                                            mapMarkers.put(key, googleMap.addMarker(mo))
                                        } else {
                                            //Update position
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