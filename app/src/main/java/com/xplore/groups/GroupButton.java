package com.xplore.groups;

import android.graphics.drawable.Drawable;

/**
 * Created by nikao on 1/20/2017.
 */

public class GroupButton {
    int reserve_id, imageId;
    String name, group_id, leader_image_url;

    public GroupButton(String group_id, int imageId, String leader_image_url,
                       int reserve_id, String name) {
        this.group_id = group_id;
        this.imageId = imageId;
        this.leader_image_url = leader_image_url;
        this.reserve_id = reserve_id;
        this.name = name;
    }


    public String getLeader_image_url() {
        return leader_image_url;
    }

    public void setLeader_image_url(String leader_image_url) {
        this.leader_image_url = leader_image_url;
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

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
