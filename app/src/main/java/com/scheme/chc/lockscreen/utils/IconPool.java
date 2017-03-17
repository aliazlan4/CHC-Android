package com.scheme.chc.lockscreen.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by Paroxis' Matrices on 05-Mar-17 for LockScreen
 */
@SuppressLint("StaticFieldLeak")
public class IconPool {

    private static IconPool instance;
    private Context context;
    private Utilities utilities;
    private ArrayList<Icon> iconPool;

    private IconPool(Context context) {
        this.context = context;
        this.utilities = Utilities.getInstance();
        this.iconPool = new ArrayList<>();
        this.initializeIconPool();
    }

    public static void initialize(Context context) {
        instance = new IconPool(context);
    }

    public static IconPool getInstance() {
        if (instance == null) {
            throw new ExceptionInInitializerError("Context literal not initialized. Use " +
                    "initialize(Context) or getNewInstance(Context) first.");
        }
        return instance;
    }

    public static IconPool getInstance(Context context) {
        instance = new IconPool(context);
        return instance;
    }

    private void initializeIconPool() {
        try {
            AssetManager assetManager = context.getAssets();
            String[] fileList = assetManager.list("icons");
            for (int i = 1; i <= fileList.length; i++) {
                iconPool.add(new Icon(i + ".png", "icons/" + i + ".png", false,
                        streamToBitmap(assetManager.open("icons/" + i + ".png"))
                    )
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap streamToBitmap(InputStream inputStream) {
        Bitmap bitmap = BitmapFactory.decodeStream(new BufferedInputStream(inputStream));
        bitmap = Bitmap.createScaledBitmap(bitmap, utilities.getIconWidth(), utilities.getIconHeight(), false);
        return bitmap;
    }

    public ArrayList<Icon> getIconPool() {
        return iconPool;
    }

    private ArrayList<Icon> getRandomIconPool(ArrayList<String> passIconNames) {
        /*
            Randomize based on pass icons here...
        */
        return new ArrayList<>();
    }
}
