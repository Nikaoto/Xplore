package com.xplore.event

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.Exclude
import com.xplore.util.MapUtil

/**
 * Created by Nika on 9/25/2017.
 *
 * Used for Firebase ORM for events
 *
 */

/*
// Old Stand class for Sqlite
data class Stand(val id: Int, val name: String, val description: String, val image: ByteArray,
                 val latitude: Double, val longitude: Double) {

    companion object {
        val COLUMN_ID = "id"
        val TABLE_NAME = "stands"
        val COLUMN_IMAGE = "image"
        val COLUMN_NAME = "name"
        val COLUMN_DESCRIPTION = "description"
        val COLUMN_LAT = "latitude"
        val COLUMN_LNG = "longitude"
    }
}*/

data class Stand(var id: String = "",
                 val show_title: Boolean = false,
                 val description: String = "",
                 val image_url: String = "",
                 val banner_image_url: String = "",
                 val latitude: Double = MapUtil.DEFAULT_LAT_LNG,
                 val longitude: Double = MapUtil.DEFAULT_LAT_LNG,
                 val check_ins: HashMap<String, Boolean> = HashMap<String,Boolean>()) {

    @Exclude
    fun getLatLng() = LatLng(latitude, longitude)

    @Exclude
    fun hasNoLocation() = latitude == MapUtil.DEFAULT_LAT_LNG || longitude == MapUtil.DEFAULT_LAT_LNG
}
