package com.xplore.groups

/**
 * Created by Nika on 8/7/2017.
 *
 * აღწერა:
 * გამოიყენება რომ მხოლოდ ჯგუფების Idები ავიღოთ Firebase-დან მთლიანი ჯგუფების მაგივრად. ძალზედ
 * აჩქარებს ჩატვირთვას.
 *
 * Description:
 * Used to only get group ids, instead of the whole group from Firebase. Greatly speeds up loading.
 *
 */

class AllGroupIdsForMember(
        val group_ids: HashMap<String, Boolean> = HashMap<String, Boolean>(),
        val invited_group_ids: HashMap<String, Boolean> = HashMap<String, Boolean>()
)