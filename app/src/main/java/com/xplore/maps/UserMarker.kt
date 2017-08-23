package com.xplore.maps

import com.google.android.gms.maps.model.LatLng
import java.util.*

/**
 * Created by Nika on 8/18/2017.
 * Used to display a marker on the map at a group member's location while hiking
 */

class UserMarker (var name: String = "",
                  var latitude: Double = 0.0,
                  var longitude: Double = 0.0,
                  val hue: Float = Random(System.currentTimeMillis()).nextFloat() * 330F) {

    fun setLocation(lat: Double , lng: Double) {
        this.latitude = lat
        this.longitude = lng
    }

    fun getLocation() = LatLng(latitude, longitude)
}