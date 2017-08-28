package com.xplore.groups;

import java.util.HashMap;

/**
 * Created by Nikaoto on 2/8/2017.
 */

public class Group {
    public static final int DESTINATION_DEFAULT = -1;

    public String group_id, name;
    public long start_date, end_date;
    public String start_time, end_time;
    public boolean experienced;
    public int destination_id;
    public double destination_latitude, destination_longitude;
    public String group_image_url, extra_info, group_preferences;
    public HashMap<String, Boolean> member_ids;
    public HashMap<String, Boolean> invited_member_ids;

    public Group() {
        //LEAVE EMPTY
    }

    public String getLeaderId() {
        for (String userId : member_ids.keySet()) {
            if (member_ids.get(userId)) {
                return userId;
            }
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtra_info() {
        return extra_info;
    }

    public void setExtra_info(String extra_info) {
        this.extra_info = extra_info;
    }

    public String getGroup_preferences() {
        return group_preferences;
    }

    public void setGroup_preferences(String group_preferences) {
        this.group_preferences = group_preferences;
    }

    public long getStart_date() {
        return start_date;
    }

    public void setStart_date(long start_date) {
        this.start_date = start_date;
    }

    public long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(long end_date) {
        this.end_date = end_date;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public boolean isExperienced() {
        return experienced;
    }

    public void setExperienced(boolean experienced) {
        this.experienced = experienced;
    }

    public HashMap<String, Boolean> getMember_ids() {
        return member_ids;
    }

    public void setMember_ids(HashMap<String, Boolean> member_ids) {
        this.member_ids = member_ids;
    }

    public String getGroup_image_url() {
        return group_image_url;
    }

    public void setGroup_image_url(String group_image_url) {
        this.group_image_url = group_image_url;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public HashMap<String, Boolean> getInvited_member_ids() {
        return invited_member_ids;
    }

    public void setInvited_member_ids(HashMap<String, Boolean> invited_member_ids) {
        this.invited_member_ids = invited_member_ids;
    }

    public int getDestination_id() {
        return destination_id;
    }

    public void setDestination_id(int destination_id) {
        this.destination_id = destination_id;
    }

    public Double getDestination_latitude() {
        return destination_latitude;
    }

    public void setDestination_latitude(Double destination_latitude) {
        this.destination_latitude = destination_latitude;
    }

    public double getDestination_longitude() {
        return destination_longitude;
    }

    public void setDestination_longitude(double destination_longitude) {
        this.destination_longitude = destination_longitude;
    }
}
