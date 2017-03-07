package com.scheme.chc.lockscreen;

import android.app.Application;

import com.scheme.chc.lockscreen.utils.AppSharedPrefs;
import com.scheme.chc.lockscreen.utils.IconPool;
import com.scheme.chc.lockscreen.utils.Utilities;

/**
 * Created by Paroxis' Matrices on 04-Mar-17 for LockScreen
 * <p>
 * This class is executed once automatically when the app starts. The purpose is to initialize all
 * the singletons that will be used throughout the app.
 * </p>
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppSharedPrefs.initialize(getApplicationContext());
        Utilities.initialize(getApplicationContext());
        IconPool.initialize(getApplicationContext());
    }
}
