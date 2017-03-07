package com.explorify.xplore.xplore_demo;

import android.graphics.drawable.Drawable;

/**
 * Created by nikao on 1/20/2017.
 */

public class ReserveButton {
    private int id;
    private Drawable image;
    private String name;

    public ReserveButton(int id, Drawable image, String name) {
        this.id = id;
        this.image = image;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
