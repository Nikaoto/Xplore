package com.xplore.groups

/**
 * Created by Nika on 8/7/2017.
 *
 * A class to just fetch the invited and joined member ids for group
 */
class GroupAllMemberIds (
        val member_ids: HashMap<String, Boolean> = HashMap<String, Boolean>(),
        val invited_member_ids: HashMap<String, Boolean> = HashMap<String,Boolean>()
)