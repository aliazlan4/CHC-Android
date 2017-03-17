package com.scheme.chc.lockscreen.settings;

import android.graphics.Bitmap;

class GridViewIcon {
    private Bitmap image;
    private String title;

    GridViewIcon(Bitmap image, String title) {
        super();
        this.image = image;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}