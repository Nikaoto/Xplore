package com.xplore.util

import android.util.Log
import com.xplore.TimeManager
import java.util.*

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

    @JvmStatic
    fun calculateAge(bYear: Int, bMonth: Int, bDay: Int): Int {
        // Get current date
        val cal = Calendar.getInstance()
        cal.time = Date(TimeManager.globalTimeStamp)
        val nowYear = cal.get(Calendar.YEAR);
        val nowMonth = cal.get(Calendar.MONTH) + 1;
        val nowDay = cal.get(Calendar.DAY_OF_MONTH);

        // Subtract current and given dates and
        var tempAge = nowYear - bYear;

        // Calculate month & day differences
        if(nowMonth > bMonth)
            tempAge++;
        else if(nowMonth == bMonth && nowDay >= bDay)
            tempAge++;
        else
            tempAge--;

        return tempAge;
    }

    // Adds zero to Day or Month number if needed
    fun addZero(num: Int) = if(num < 10) "0$num" else "$num"

    // Returns displayable date string with slashes
    @JvmStatic
    fun formatDate(year: Int, month: Int, day: Int): String {
        val temp = year.toString() + addZero(month) + addZero(day)
        return putSlashesInDate(temp)
    }

    // TODO finish this
    @JvmStatic
    fun getDifferenceInDays(y1: Int, m1: Int, d1: Int, y2: Int, m2: Int, d2: Int): Int {
        var ans = Math.abs(y1 - y2)
        return y1 - y2
    }
}