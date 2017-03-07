package com.scheme.chc.lockscreen.utils;

import android.graphics.Bitmap;

/**
 * Created by Paroxis' Matrices on 05-Mar-17 for LockScreen
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Icon {

    private int x, y;
    private String hashID;
    private String path;
    private Bitmap imageData;
    private boolean isPassIcon;

    Icon(String hashID, String path, boolean isPassIcon, Bitmap imageData) {
        this(hashID, path, isPassIcon, imageData, 0, 0);
    }

    Icon(String hashID, String path, boolean isPassIcon, Bitmap imageData, int x, int y) {
        this.hashID = hashID;
        this.path = path;
        this.isPassIcon = isPassIcon;
        this.imageData = imageData;
        this.x = x;
        this.y = y;
    }

    public String getHashID() {
        return hashID;
    }

    public void setHashID(String hashID) {
        this.hashID = hashID;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isPassIcon() {
        return isPassIcon;
    }

    public void setPassIcon(boolean passIcon) {
        isPassIcon = passIcon;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Bitmap getImageData() {
        return imageData;
    }

    public void setImageData(Bitmap imageData) {
        this.imageData = imageData;
    }

    public String toString() {
        return "Hash ID: " + hashID + ", Path: " + path + ", X:" + x + ", Y: " + y + ", Pass: " + isPassIcon;
    }
}
