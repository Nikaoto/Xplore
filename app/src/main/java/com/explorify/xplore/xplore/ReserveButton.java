package com.explorify.xplore.xplore;

/**
 * Created by nikao on 1/20/2017.
 */

public class ReserveButton {
    private int id, imageId;
    private String name;

    public ReserveButton(int id, int imageId, String name) {
        this.id = id;
        this.imageId = imageId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
