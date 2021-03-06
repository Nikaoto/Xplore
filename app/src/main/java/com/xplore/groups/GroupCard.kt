package com.xplore.groups

import com.xplore.General
import com.xplore.TimeManager

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
        var invite: Boolean = false,
        var memberCount: Int = 0,
        //Values intended for Firebase data mapping
        val destination_id: Int = 0,
        val group_image_url: String = "",
        val start_date: Int = 0,
        val end_date: Int = 0,
        val experienced: Boolean = false) {

    //Gets start time in days from current date
    fun getStartInDays() = General.getDateDiffInDays(TimeManager.intTimeStamp, start_date)

    //Gets duration of the trip in days
    fun getDurationInDays() = General.getDateDiffInDays(start_date, end_date)
}