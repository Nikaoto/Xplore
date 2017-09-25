package com.xplore

/**
 * Created by Nika on 9/25/2017.
 *
 * Used for ORM in Iliauni update
 *
 */

data class Stand(val id: Int, val name: String, val description: String,
                 val latitude: Double, val longitude: Double) {

    companion object {
        val COLUMN_ID = "id"
        val TABLE_NAME = "stands"
        val COLUMN_NAME = "name"
        val COLUMN_DESCRIPTION = "description"
        val COLUMN_LAT = "latitude"
        val COLUMN_LNG = "longitude"
    }
}