package com.scheme.chc.lockscreen.lockscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;

import com.scheme.chc.lockscreen.R;
import com.scheme.chc.lockscreen.service.LockScreenService;
import com.scheme.chc.lockscreen.settings.SettingsActivity;
import com.scheme.chc.lockscreen.utils.AppSharedPrefs;

import java.util.List;

public class MainLockScreenWindow extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean prefEnableCHC = AppSharedPrefs.getInstance().getEnabledCHC();
        if (!prefEnableCHC) {    //&& opensettings || LockScreenService.notification == null
            System.out.println("here");
            startActivity(new Intent(MainLockScreenWindow.this, SettingsActivity.class));
        }
        if (isServiceRunning(getString(R.string.ServiceClass)) && !prefEnableCHC) {
            stopService(new Intent(this, LockScreenService.class));
        }
        if (prefEnableCHC) {
            startService(new Intent(this, LockScreenService.class));
            startActivity(new Intent(MainLockScreenWindow.this, SettingsActivity.class));
        }
    }

    public boolean isServiceRunning(String serviceClassName) {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)) {
                return true;
            }
        }
        return false;
    }

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

}
