package com.xplore.maps

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import java.util.*

/**
 * Created by Nika on 8/18/2017.
 * Used to display a marker on the map at a group member's location while hiking
 */

class UserMarker (var name: String = "",
                  var latitude: Double = -91.0,
                  var longitude: Double = -91.0,
                  val hue: Float = Random(System.currentTimeMillis()).nextFloat() * 330F) {

    @Exclude
    fun getLocation() = LatLng(latitude, longitude)
}