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

    //This is the default latitude or longitude for when no location is chosen
    // (-91, -91 goes nowhere on the earth)
    const val DEFAULT_LAT_LNG = -91.0

    //Used when calculating random hue for map markers
    const val MAX_MARKER_HUE = 330F

    @JvmStatic
    fun getMapUrl(lat: Double, lng: Double,
                  width: Int = 450, height: Int = 300, zoom: Int = 16,
                  mapType: String = "hybrid", markerColor: String = "orange"): String {
        val coordPair = "$lat,$lng"
            return "http://maps.googleapis.com/maps/api/staticmap?" +
        "&zoom=$zoom" +
        "&size=$width" + "x" + "$height" +
        "&maptype=$mapType" +
        "&center=$coordPair" +
        "&markers=color:$markerColor|$coordPair"
    }

    @JvmStatic
    fun getRandomMarkerHue() = Random(System.currentTimeMillis()).nextFloat() * MAX_MARKER_HUE
}