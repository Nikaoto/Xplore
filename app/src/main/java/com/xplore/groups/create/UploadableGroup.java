package com.xplore.groups.create;

import com.google.firebase.database.Exclude;
import com.xplore.groups.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* Created by Nika on 7/17/2017.
*
* აღწერა:
* ეს კლასი არის ატვირთვადი ჯგუფი Firebase-ს ბაზაზე. ლაშქრობის ჯგუფის დატა კლასს ავრცობს და
* ამატებს .toMap მეთოდს, რომლის საშუალებითაც ჰეშმეპი იქმნება, რომელიც შეიძლება აიტვირთოს Firebase-ზე
*
* Description:
* This class is an uploadable version of the Group data class. This extends it and adds a .toMap
* method which maps the data to a HashMap, which can then be uploaded to Firebase
* 
*/

//Class that gets mapped to a group in Firebase database and is uploaded
class UploadableGroup extends Group {

    UploadableGroup(String group_id, boolean experienced, long start_date, long end_date,
                String destination_id, String extra_info, String group_preferences,
                ArrayList<String> member_ids) {
        this.setGroup_id(group_id);
        this.setExperienced(experienced);
        this.setStart_date(start_date);
        this.setEnd_date(end_date);
        this.setDestination_id(destination_id);
        this.setExtra_info(extra_info);
        this.setGroup_preferences(group_preferences);
        this.setMember_ids(member_ids);
    }

    //Maps data to a HashMap (NOTE: all key names are directly from Firebase, do not change them)
    @Exclude
    Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("destination_id", this.getDestination_id());
        result.put("start_date", this.getStart_date());
        result.put("end_date", this.getEnd_date());
        result.put("experienced", this.isExperienced());
        result.put("group_preferences", this.getGroup_preferences());
        result.put("extra_info", this.getExtra_info());
        result.put("member_ids", this.getMember_ids());
        return result;
    }
}