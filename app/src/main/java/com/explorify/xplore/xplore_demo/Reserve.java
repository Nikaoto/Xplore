package com.explorify.xplore.xplore_demo;

import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by nikao on 1/15/2017.
 */

public class Reserve {
    private int id;
    private int difficulty; //scale of 1 to 10 lower means easier
    private String name, description, flora, fauna, equipment, image, extratags;
    private LatLng location;
    private Drawable drawable;

    public Reserve()
    {
        //TODO make this an actual focken constructor :^)
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public int getId()
    {
        return id;
    }

    public int getDifficulty()
    {
        return difficulty;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public String getEquipment()
    {
        return equipment;
    }

    public String getFlora()
    {
        return flora;
    }

    public String getFauna()
    {
        return fauna;
    }

    public String getExtratags()
    {
        return extratags;
    }

    public String getImage() { return image; }

    public LatLng getLocation() {
        return location;
    }

    public void setId(int _id)
    {
        this.id = _id;
    }

    public void setDifficulty(int _difficulty)
    {
        this.difficulty = _difficulty;
    }

    public void setName(String _name)
    {
        this.name = _name;
    }

    public void setDescription(String _description)
    {
        this.description = _description;
    }

    public void setEquipment(String _equipment)
    {
        this.equipment = _equipment;
    }

    public void setFlora(String _flora)
    {
        this.flora = _flora;
    }

    public void setFauna(String _fauna)
    {
        this.fauna = _fauna;
    }

    public void setExtratags(String _extratags)
    {
        this.extratags = _extratags;
    }

    public void setImage(String _image) { this.image  = _image; }

    public void setLocation(LatLng location) {
        this.location = location;
    }
}
