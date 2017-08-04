package com.xplore.groups;

import java.util.ArrayList;

/**
 * Created by Nikaoto on 2/8/2017.
 */

public class Group {
    public boolean experienced;
    public long start_date, end_date;
    public String start_time, end_time;
    protected String destination_id, group_id, extra_info, group_preferences;
    public ArrayList<String> member_ids;
    public ArrayList<String> invited_member_ids;

    public Group() {
        //LEAVE EMPTY
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

    public ArrayList<String> getMember_ids() {
        return member_ids;
    }

    public void setMember_ids(ArrayList<String> member_ids) {
        this.member_ids = member_ids;
    }

    public String getDestination_id() {
        return destination_id;
    }

    public void setDestination_id(String destination_id) {
        this.destination_id = destination_id;
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

    public ArrayList<String> getInvited_member_ids() {
        return invited_member_ids;
    }

    public void setInvited_member_ids(ArrayList<String> invited_member_ids) {
        this.invited_member_ids = invited_member_ids;
    }
}
