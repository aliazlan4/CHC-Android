package com.scheme.chc.lockscreen.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;

import java.util.Set;

/**
 * Created by Paroxis' Matrices on 04-Mar-17 for LockScreen
 */
@SuppressLint("StaticFieldLeak")
@SuppressWarnings({"WeakerAccess", "unused"})
public class AppSharedPrefs {

    private static AppSharedPrefs instance;
    private SharedPreferences sharedPreferences;

    private AppSharedPrefs(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void initialize(Context context) {
        instance = new AppSharedPrefs(context);
    }

    public static AppSharedPrefs getInstance() {
        if (instance == null) {
            throw new ExceptionInInitializerError("Context literal not initialized. Use " +
                    "initialize(Context) or getNewInstance(Context) first ");
        }
        return instance;
    }

    public static AppSharedPrefs getNewInstance(Context context) {
        instance = new AppSharedPrefs(context);
        return instance;
    }

    /* *********************************************************************************************
                                            PREFERENCE VARIABLES
    ********************************************************************************************* */

    public boolean getEnabledCHC() {
        return sharedPreferences.getBoolean(PrefProtocols.ENABLE_CHC.getProtocolVal(), false);
    }

    public void setEnabledCHC(boolean val) {
        sharedPreferences.edit().putBoolean(PrefProtocols.ENABLE_CHC.getProtocolVal(), val).apply();
    }

    public String getNumPassIcons() {
        return sharedPreferences.getString(PrefProtocols.NUM_PASS_ICONS.getProtocolVal(), "5");
    }

    public void setNumPassIcons(String val) {
        sharedPreferences.edit().putString(PrefProtocols.NUM_PASS_ICONS.getProtocolVal(), val).apply();
    }

    public String getTotalIcons() {
        return sharedPreferences.getString(PrefProtocols.TOTAL_ICONS.getProtocolVal(), "40");
    }

    public void setTotalIcons(String val) {
        sharedPreferences.edit().putString(PrefProtocols.TOTAL_ICONS.getProtocolVal(), val).apply();
    }

    public String getRounds() {
        return sharedPreferences.getString(PrefProtocols.ROUNDS.getProtocolVal(), "5");
    }

    public void setRounds(String val) {
        sharedPreferences.edit().putString(PrefProtocols.ROUNDS.getProtocolVal(), val).apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getViewPassIcons() {
        return sharedPreferences.getStringSet(PrefProtocols.VIEW_PASS_ICONS.getProtocolVal(), null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setViewPassIcons(Set<String> val) {
        sharedPreferences.edit().putStringSet(PrefProtocols.VIEW_PASS_ICONS.getProtocolVal(), val).apply();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public Set<String> getCustomPassIcons() {
        return sharedPreferences.getStringSet(PrefProtocols.CUSTOM_PASS_ICON.getProtocolVal(), null);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setCustomPassIcons(Set<String> val) {
        sharedPreferences.edit().putStringSet(PrefProtocols.CUSTOM_PASS_ICON.getProtocolVal(), val).apply();
    }

    public int getWeatherId() {
        return sharedPreferences.getInt(PrefProtocols.WEATHER_ID.getProtocolVal(), -1);
    }

    public void setWeatherId(int val) {
        sharedPreferences.edit().putInt(PrefProtocols.WEATHER_ID.getProtocolVal(), val).apply();
    }

    public String getTemperature() {
        return sharedPreferences.getString(PrefProtocols.TEMPERATURE.getProtocolVal(), "");
    }

    public void setTemperature(String val) {
        sharedPreferences.edit().putString(PrefProtocols.TEMPERATURE.getProtocolVal(), val).apply();
    }

    public String getCondition() {
        return sharedPreferences.getString(PrefProtocols.CONDITION.getProtocolVal(), "");
    }

    public void setCondition(String val) {
        sharedPreferences.edit().putString(PrefProtocols.CONDITION.getProtocolVal(), val).apply();
    }

    public long getSunrise() {
        return sharedPreferences.getLong(PrefProtocols.SUNRISE.getProtocolVal(), -1);
    }

    public void setSunrise(long val) {
        sharedPreferences.edit().putLong(PrefProtocols.SUNRISE.getProtocolVal(), val).apply();
    }

    public long getSunset() {
        return sharedPreferences.getLong(PrefProtocols.SUNSET.getProtocolVal(), -1);
    }

    public void setSunset(long val) {
        sharedPreferences.edit().putLong(PrefProtocols.SUNSET.getProtocolVal(), val).apply();
    }

    /* *********************************************************************************************
                                            GENERALIZED METHODS
    ********************************************************************************************* */

    public void setBoolean(PrefProtocols prefProtocol, boolean val) {
        sharedPreferences.edit().putBoolean(prefProtocol.getProtocolVal(), val).apply();
    }

    public boolean getBoolean(PrefProtocols prefProtocol) {
        return sharedPreferences.getBoolean(prefProtocol.getProtocolVal(), false);
    }

    public void setInteger(PrefProtocols prefProtocol, int val) {
        sharedPreferences.edit().putInt(prefProtocol.getProtocolVal(), val).apply();
    }

    public int getInteger(PrefProtocols prefProtocol) {
        return sharedPreferences.getInt(prefProtocol.getProtocolVal(), -1);
    }

    public void setString(PrefProtocols prefProtocol, String val) {
        sharedPreferences.edit().putString(prefProtocol.getProtocolVal(), val).apply();
    }

    public String getString(PrefProtocols prefProtocol) {
        return sharedPreferences.getString(prefProtocol.getProtocolVal(), "");
    }

    public void setStringSet(PrefProtocols prefProtocol, Set<String> val) {
        sharedPreferences.edit().putStringSet(prefProtocol.getProtocolVal(), val).apply();
    }

    public Set<String> getStringSet(PrefProtocols prefProtocol) {
        return sharedPreferences.getStringSet(prefProtocol.getProtocolVal(), null);
    }

    public void setFloat(PrefProtocols prefProtocol, float val) {
        sharedPreferences.edit().putFloat(prefProtocol.getProtocolVal(), val).apply();
    }

    public float getFloat(PrefProtocols prefProtocol) {
        return sharedPreferences.getFloat(prefProtocol.getProtocolVal(), -1.0f);
    }

    public void setLong(PrefProtocols prefProtocol, long val) {
        sharedPreferences.edit().putLong(prefProtocol.getProtocolVal(), val).apply();
    }

    public long getLong(PrefProtocols prefProtocol) {
        return sharedPreferences.getLong(prefProtocol.getProtocolVal(), -1);
    }

    /* *********************************************************************************************
                                            PREFERENCE PROTOCOLS
    ********************************************************************************************* */

    /*private static final class PrefProtocols {
        private static final String ENABLE_CHC = "enable_chc";
        private static final String NUM_PASS_ICONS = "no_of_pass_icons";
        private static final String TOTAL_ICONS = "total_icons";
        private static final String ROUNDS = "rounds";
        private static final String VIEW_PASS_ICONS = "view_pass_icons";
        private static final String CUSTOM_PASS_ICON = "custom_pass_icon";
    }*/

    private enum PrefProtocols {
        // For CHC
        ROUNDS              ("rounds"),
        ENABLE_CHC          ("enable_chc"),
        TOTAL_ICONS         ("total_icons"),
        VIEW_PASS_ICONS     ("view_pass_icons"),
        NUM_PASS_ICONS      ("no_of_pass_icons"),
        CUSTOM_PASS_ICON("custom_pass_icon"),

        // For weather
        WEATHER_ID("weather_id"),
        TEMPERATURE("temp"),
        CONDITION("cond"),
        SUNRISE("sunrise"),
        SUNSET("sunset");

        private String protocolVal;

        PrefProtocols(String protocolVal) {
            this.protocolVal = protocolVal;
        }

        public String getProtocolVal() {
            return protocolVal;
        }
    }
}
