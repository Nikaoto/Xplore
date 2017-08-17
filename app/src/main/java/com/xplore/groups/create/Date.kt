package com.xplore.groups.create

import java.lang.StringBuilder

/**
 * Created by Nikaoto on 8/15/2017.
 *
 * Stores start and end dates/times for when selecting dates in group edit or
 * creation.
 *
 */

class Date {
    //Start
    var startYear = 0
    var startMonth = 0
    var startDay = 0
    var startTime = ""

    //End
    var endYear = 0
    var endMonth = 0
    var endDay = 0
    var endTime = ""

    //Setting dates
    fun setStartDate(y: Int, m: Int, d: Int) {
        this.startYear = y
        this.startMonth = m
        this.startDay = d
    }

    fun setStartDate(date: Long) {
        val s = StringBuilder(date.toString())
        this.setStartDate(s.substring(0, 4).toInt(), s.substring(4, 6).toInt(),
                s.substring(6).toInt())
    }

    fun setEndDate(y: Int, m: Int, d: Int) {
        this.endYear = y
        this.endMonth = m
        this.endDay = d
    }

    fun setEndDate(date: Long) {
        val s = StringBuilder(date.toString())
        this.setEndDate(s.substring(0, 4).toInt(), s.substring(4, 6).toInt(),
                s.substring(6).toInt())
    }

    //Setting times
    fun setTime(hour: Int, minute: Int): String {
        var str = ""
        if (hour < 10) {
            str += "0"
        }
        str += hour.toString()
        if (minute < 10) {
            str += "0"
        }
        str += minute.toString()
        return str
    }

    fun setStartTime(hour: Int, minute: Int) {
        startTime = setTime(hour, minute)
    }

    fun setEndTime(hour: Int, minute: Int) {
        endTime = setTime(hour, minute)
    }

    //Getting times as strings
    private fun getTimeText(s: String) = s.substring(0, 2) + ":" + s.substring(2)
    fun getStartTimeText() = getTimeText(startTime)
    fun getEndTimeText() = getTimeText(endTime)

    //Getting dates as longs
    private fun getDateLong(y: Int, m: Int, d: Int) = (y*10000 + m*100 + d).toLong()

    fun getStartDate() = getDateLong(startYear, startMonth, startDay)
    fun getEndDate() = getDateLong(endYear, endMonth, endDay)

    //Getting dates as strings (to display on TextViews)
    private fun getDateString(y: Int, m: Int, d: Int): String {
        var month = ""
        var day = ""

        if (d < 10) day = "0"
        day += d.toString()
        if (m < 10) month = "0"
        month += m.toString()

        return "$y/$month/$day"
    }
    fun getStartDateString() = getDateString(startYear, startMonth, startDay)
    fun getEndDateString() = getDateString(endYear, endMonth, endDay)
}