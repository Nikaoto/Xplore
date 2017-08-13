package com.xplore

/**
 * Created by Nikaoto on 8/13/2017.
 *
 * აქ ვათავსებთ ყველა რუქასთან დაკავშირებულ გენერალურ ფუნქციას.
 *
 * This is where all map related general and utility functions go.
 *
 */

class MapUtil {
    companion object {
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
    }
}