package com.explorify.xplore.xplore;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nikao on 1/15/2017.
 */

public class Reserve {
    private int id, imageId;
    private int difficulty; //scale of 1 to 10 lower means easier
    private String name, description, flora, fauna, equipment, extratags;
    private LatLng location;

    //Primary Constructor
    Reserve(int id, int difficulty, String name, String description, String flora, String fauna,
            String equipment, String extratags, LatLng location, int imageId) {
        this.id = id;
        this.difficulty = difficulty;
        this.name = name;
        this.description = description;
        this.flora = flora;
        this.fauna = fauna;
        this.equipment = equipment;
        this.extratags = extratags;
        this.location = location;
        this.imageId = imageId;
    }

    //Secondary Constructor
    Reserve(){ }

    int getId()
    {
        return id;
    }

    int getDifficulty()
    {
        return difficulty;
    }

    String getName()
    {
        return name;
    }

    String getDescription()
    {
        return description;
    }

    String getEquipment()
    {
        return equipment;
    }

    String getFlora()
    {
        return flora;
    }

    String getFauna()
    {
        return fauna;
    }

    String getExtratags()
    {
        return extratags;
    }

    LatLng getLocation() {
        return location;
    }

    void setId(int _id)
    {
        this.id = _id;
    }

    void setDifficulty(int _difficulty)
    {
        this.difficulty = _difficulty;
    }

    void setName(String _name)
    {
        this.name = _name;
    }

    void setDescription(String _description)
    {
        this.description = _description;
    }

    void setEquipment(String _equipment)
    {
        this.equipment = _equipment;
    }

    void setFlora(String _flora)
    {
        this.flora = _flora;
    }

    void setFauna(String _fauna)
    {
        this.fauna = _fauna;
    }

    void setExtratags(String _extratags)
    {
        this.extratags = _extratags;
    }

    void setLocation(LatLng location) {
        this.location = location;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
