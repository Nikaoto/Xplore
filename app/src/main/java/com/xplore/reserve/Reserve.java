package com.xplore.reserve;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

/**
 * Created by Nikaoto on 1/15/2017.
 */

public class Reserve {
    private int id, imageId, iconId;
    private int difficulty; //scale of 1 to 10 lower means easier
    private String name, description, flora, fauna, equipment, extratags;
    private LatLng location;

    //Primary Constructor
    public Reserve(int id, int difficulty, String name, String description, String flora, String fauna,
            String equipment, String extratags, LatLng location, int imageId, int iconId) {
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
        this.iconId = iconId;
    }

    public int getId() {
        return id;
    }

    //Secondary Constructor
    public Reserve() {}

    public void setId(int id) {
        this.id = id;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFlora() {
        return flora;
    }

    public void setFlora(String flora) {
        this.flora = flora;
    }

    public String getFauna() {
        return fauna;
    }

    public void setFauna(String fauna) {
        this.fauna = fauna;
    }

    public String getEquipment() {
        return equipment;
    }

    public void setEquipment(String equipment) {
        this.equipment = equipment;
    }

    public String getExtratags() {
        return extratags;
    }

    public void setExtratags(String extratags) {
        this.extratags = extratags;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Exclude
    public boolean hasNoLocation() {
        return location.latitude < -90f || location.latitude > 90f
                || location.longitude < -90f || location.longitude > 90f;
    }
}
