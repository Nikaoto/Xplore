package com.xplore;


/**
 * Created by nikao on 2/11/2017.
 */

//DO NOT CHANGE THE VARIABLE NAMES, THEY CORRESPOND TO THE FIREBASE DATABASE KEY NAMES
public class User {
    String fname, lname, tel_num, id, profile_picture_url, email;
    int reputation, birth_date, age;
    //TODO change age to birth date and calculate age every time user info is loaded

    public User() {
        //LEAVE EMPTY
    }

    public User(String fname, String lname, String tel_num, String id, String profile_picture_url, String email, int reputation, int birth_date) {
        this.fname = fname;
        this.lname = lname;
        this.tel_num = tel_num;
        this.id = id;
        this.profile_picture_url = profile_picture_url;
        this.email = email;
        this.reputation = reputation;
        this.birth_date = birth_date;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(int birth_date) {
        this.birth_date = birth_date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getProfile_picture_url() {
        return profile_picture_url;
    }

    public void setProfile_picture_url(String profile_picture_url) {
        this.profile_picture_url = profile_picture_url;
    }
}
