package com.explorify.xplore.xplore_demo;

import android.graphics.drawable.Drawable;

/**
 * Created by nikao on 1/20/2017.
 */

public class GroupButton {
    Drawable reserveImage;
    int reserve_id;
    String name, group_id, leader_image_ref, leader_id;

    public GroupButton(String group_id, Drawable reserveImage, String leader_image_ref,
                       int reserve_id, String name, String leader_id) {
        this.group_id = group_id;
        this.reserveImage = reserveImage;
        this.leader_image_ref = leader_image_ref;
        this.reserve_id = reserve_id;
        this.name = name;
        this.leader_id = leader_id;
    }

    public String getLeader_image_ref() {
        return leader_image_ref;
    }

    public void setLeader_image_ref(String leader_image_ref) {
        this.leader_image_ref = leader_image_ref;
    }

    public String getLeader_id() {
        return leader_id;
    }

    public void setLeader_id(String leader_id) {
        this.leader_id = leader_id;
    }

    public int getReserve_id() {
        return reserve_id;
    }

    public void setReserve_id(int reserve_id) {
        this.reserve_id = reserve_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String id) {
        this.group_id = id;
    }

    public Drawable getImage() {
        return reserveImage;
    }

    public void setImage(Drawable image) {
        this.reserveImage = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
