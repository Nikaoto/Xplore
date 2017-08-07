package com.xplore.groups

import com.xplore.General
import com.xplore.TimeManager
import java.util.*

/**
 * Created by Nika on 7/12/2017.
 *
 * აღწერა:
 * ინახავს ინფორმაციას, რომელიც საჭიროა სიაში ჯგუფის კარტის საჩვენებლად (არაა საჭირო მთლიანი ჯგუფის
 * ინფოს შექმნა)
 * დატოვეთ ცვლადების სახელები როგორც არის, საჭიროა Firebase-ს data mapping-ის თვის
 * ასევე საჭიროა დეფაულტ მნიშვნელობები ORM-ის თვის (ცარიელი კონსტრუქტორის მაგიერია კოტლინში)
 *
 * Description:
 * Holds the data needed to display a group on a card (instead of full group)
 * UNCONVENTIONAL VARIABLE ARE NEEDED FOR FIREBASE DATA MAPPING, DO NOT CHANGE.
 * DEFAULT VALUES ENSURE DATA MAPPING SUCCEEDS, DO NOT CHANGE
 *
 */

data class GroupCard(
        var id: String = "",
        var name: String = "",
        var leaderId: String = "",
        var leaderReputation: Int = 0,
        var leaderName: String = "",
        var leaderImageUrl: String = "",
        var reserveImageId: Int = 0,
        var invite: Boolean = false,
        var memberCount: Int = 0,
        //Values intended for Firebase data mapping
        val destination_id: String = "",
        val start_date: Int = 0,
        val end_date: Int = 0,
        val experienced: Boolean = false) {

    //Gets start time in days
    fun getStartInDays(): Int {
        //Start date
        val sYear = start_date.toString().substring(0, 4).toInt()
        val sMonth = start_date.toString().substring(4, 6).toInt()
        val sDay = start_date.toString().substring(6).toInt()
        //Start date calendar
        val startCalendar = Calendar.getInstance()
        startCalendar.set(sYear, sMonth - 1, sDay)

        val diffInMillis = startCalendar.timeInMillis - TimeManager.globalTimeStamp

        return (diffInMillis / 1000 / 3600 / 24).toInt()
    }
}

//TODO add tour name to firebase
//TODO remove reserveImageId
//TODO add current & max groupmates
//TODO add duration