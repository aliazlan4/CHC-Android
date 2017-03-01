package com.scheme.chc.lockscreen.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;


public class Utilities {

    private static Utilities instance;
    private final int totalNumberOfPassIcons;
    private final int totalIconsToDisplay;
    private final int TotalNumberOfRegularIcons;
    private final int numberOfIconsVertically;
    private final int numberOfIconsHorizontally;
    private final int TotalRounds;
    private String[] ViewingPassicons = new String[getTotalNumberOfPassIcons()];
    private String[] ChoosenPassIcons = new String[getTotalNumberOfPassIcons()];
    private String[] UploadedPassIcons = new String[getTotalNumberOfPassIcons()];
    public ArrayList<Integer> index  = new ArrayList<>();

    public static Utilities getInstance() {
        if (instance == null) {
            throw new ExceptionInInitializerError("Context not initialized. User getInstance(Context) first.");
        }
        return instance;
    }

    public static Utilities getInstance( Context context) {
        instance = new Utilities(context);
        return instance;
    }

    private Utilities(Context context){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        totalNumberOfPassIcons = Integer.parseInt(preferences.getString("no_of_pass_icons", "5"));
        totalIconsToDisplay = (Integer.parseInt(preferences.getString("total_icons", "40")));
        TotalNumberOfRegularIcons = (Integer.parseInt(preferences.getString("total_icons", "40"))) - totalNumberOfPassIcons;
        TotalRounds = Integer.parseInt(preferences.getString("rounds", "5"));
        numberOfIconsVertically = Math.abs(totalIconsToDisplay /5);
        numberOfIconsHorizontally =  Math.abs((totalIconsToDisplay /numberOfIconsVertically));
//        System.out.println("H: " + numberOfIconsHorizontally + " V: " + numberOfIconsVertically);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            Set<String> viewingIcons = preferences.getStringSet("view_pass_icons", null);
            Set<String> selectionscustomuploaded = preferences.getStringSet("custom_pass_icon", null);
            Set<String> selectionschoosengallery = preferences.getStringSet("choose_pass_icon", null);
            UploadedPassIcons = selectionscustomuploaded != null ? selectionscustomuploaded.toArray(new String[]{}) : new String[0];
            ChoosenPassIcons = selectionschoosengallery != null ? selectionschoosengallery.toArray(new String[]{}) : new String[0];
            ViewingPassicons = viewingIcons != null ? viewingIcons.toArray(new String[]{}) : new String[0];
        }

//        System.out.println("Uploaded " + Arrays.toString(UploadedPassIcons));
//        System.out.println("Choosen " + Arrays.toString(ChoosenPassIcons));
//        System.out.println("viewing " + Arrays.toString(ViewingPassicons));
    }

    public int getRandomInt(int limit) {
        int random = new Random().nextInt(limit - 5) + 5;

        if (index.contains(random)) {
            random = getRandomInt(limit);
        }
        else {
            index.add(random);
        }
        return random;
    }

    public int getTotalNumberOfPassIcons() {
        return totalNumberOfPassIcons;
    }

    public int getTotalIconsToDisplay() {
        return totalIconsToDisplay;
    }

    public int getTotalNumberOfRegularIcons() {
        return TotalNumberOfRegularIcons;
    }

    public int getNumberOfIconsVertically() {
        return numberOfIconsVertically;
    }

    public int getNumberOfIconsHorizontally() {
        return numberOfIconsHorizontally;
    }

    public int getTotalRounds() {
        return TotalRounds;
    }

    public String[] getUploadedPassIcons() {
        return UploadedPassIcons;
    }

    public String[] getChoosenPassIcons() {
        return ChoosenPassIcons;
    }

}
