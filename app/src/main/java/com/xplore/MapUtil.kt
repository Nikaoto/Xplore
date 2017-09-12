package com.xplore

import java.util.*

/**
 * Created by Nikaoto on 8/13/2017.
 *
 * აქ ვათავსებთ ყველა რუქასთან დაკავშირებულ გენერალურ ფუნქციას.
 *
 * This is where all map related general and utility functions go.
 *
 */

object MapUtil {

    //Defaults
    const val DEFAULT_WIDTH = 450
    const val DEFAULT_HEIGHT = 300
    const val DEFAULT_ZOOM = 16
    const val DEFAULT_MAP_TYPE = "hybrid"
    const val DEFAULT_MARKER_COLOR = "orange"

    //This is the default latitude or longitude for when no location is chosen
    const val DEFAULT_LAT_LNG = 0.0

    //Used when calculating random hue for map markers
    const val MAX_MARKER_HUE = 330F

    const val MEETUP_MARKER_COLOR = "green"

    @JvmStatic
    fun getMapUrl(lat: Double, lng: Double,
                  width: Int = DEFAULT_WIDTH, height: Int = DEFAULT_HEIGHT,
                  zoom: Int = DEFAULT_ZOOM, mapType: String = DEFAULT_MAP_TYPE,
                  markerColor: String = DEFAULT_MARKER_COLOR): String {
        val coordPair = "$lat,$lng"
            return "http://maps.googleapis.com/maps/api/staticmap?" +
        "&zoom=$zoom" +
        "&size=$width" + "x" + "$height" +
        "&maptype=$mapType" +
        "&center=$coordPair" +
        "&markers=color:$markerColor|$coordPair"
    }

    @JvmStatic
    fun getMeetupMapUrl(lat: Double, lng: Double, markerColor: String = MEETUP_MARKER_COLOR)
            = getMapUrl(lat, lng, markerColor = markerColor)

    @JvmStatic
    fun getRandomMarkerHue() = Random(System.currentTimeMillis()).nextFloat() * MAX_MARKER_HUE
}