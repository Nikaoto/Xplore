package com.explorify.xplore.xplore_demo;

import android.media.Image;

/**
 * Created by nikao on 2/11/2017.
 */

public class User {
    String fname, lname, tel_num, id, profile_picture_ref;
    int reputation, age;

    public User() {

        //LEAVE EMPTY
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReputation() {
        return reputation;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getTel_num() {
        return tel_num;
    }

    public void setTel_num(String tel_num) {
        this.tel_num = tel_num;
    }

    public String getProfile_picture_ref() {
        return profile_picture_ref;
    }

    public void setProfile_picture_ref(String profile_picture_ref) {
        this.profile_picture_ref = profile_picture_ref;
    }
}
