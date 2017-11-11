package com.xplore.util

import android.util.Log

/*
 * Created by Nik on 9/6/2017.
 *
 * Helps with parsing and packing dates in format YYYY.MM.DD
 *
 */

object DateUtil {

    private const val TAG = "date-util"

    @JvmStatic
    fun getYear(date: String): Int {
        if (date.length != 8) {
            Log.i(TAG, "getYear: date length != 8; returning 0")
            return 0
        }

        return date.substring(0, 4).toInt()
    }

    @JvmStatic
    fun getMonth(date: String): Int {
        if (date.length != 8) {
            Log.i(TAG, "getMonth: date length != 8; returning 0")
            return 0
        }

        return date.substring(4, 6).toInt()
    }

    @JvmStatic
    fun getDay(date: String): Int {
        if (date.length != 8) {
            Log.i(TAG, "getDay: date length != 8; returning 0")
            return 0
        }

        return date.substring(6).toInt()
    }

    @JvmStatic fun putSlashesInDate(date: Int): String = putSlashesInDate(date.toString())

    @JvmStatic fun putSlashesInDate(date: Long): String = putSlashesInDate(date.toString())

    // Adds slashes to a date given in int (yyyy.mm.dd) without dots
    @JvmStatic fun putSlashesInDate(date: String): String {
        val builder = StringBuilder(date)
        if (builder.length != 8) {
            Log.i(TAG, "putSlashesInDate: date length !=8; returning ''")
            return ""
        }

        builder.insert(4, "/")
        builder.insert(7, "/")

        return builder.toString()
    }

}