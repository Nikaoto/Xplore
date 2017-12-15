package com.xplore.event

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.Exclude
import com.xplore.util.MapUtil

/**
 * Created by Nika on 12/15/2017.
 *
 * Used to mark locations ("stands") for events on the map.
 *
 */

class StandMarker (val latitude: Double = MapUtil.DEFAULT_LAT_LNG,
                           val longitude: Double = MapUtil.DEFAULT_LAT_LNG,
                           val hue: Float = MapUtil.getRandomMarkerHue(),
                           var checkedIn: Boolean = false,
                           var mapMarker: Marker? = null) {
    @Exclude
    fun getLatLng() = LatLng(latitude, longitude)

    @Exclude
    fun getDistanceFrom(latLng: LatLng): Double {
        return Math.sqrt(
                Math.pow(latitude - latLng.latitude, 2.0)
                        + Math.pow(longitude - latLng.longitude, 2.0))
    }

    @Exclude
    fun getDistanceFrom(location: Location): Double {
        return Math.sqrt(
                Math.pow(latitude - location.latitude, 2.0)
                        + Math.pow(longitude - location.longitude, 2.0))
    }
}