package com.xplore.maps

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import com.xplore.MapUtil
import java.util.*

/**
 * Created by Nika on 8/18/2017.
 * Used to display a marker on the map at a group member's location while hiking
 *
 * NOTE: KEEP THE CONSTRUCTOR AS IS, NEEDED FOR FIREBASE DATA BINDING
 */

class UserMarker (var name: String = "",
                  var latitude: Double = MapUtil.DEFAULT_LAT_LNG,
                  var longitude: Double = MapUtil.DEFAULT_LAT_LNG,
                  val hue: Float = MapUtil.getRandomMarkerHue()) {

    @Exclude
    fun getLocation() = LatLng(latitude, longitude)
}