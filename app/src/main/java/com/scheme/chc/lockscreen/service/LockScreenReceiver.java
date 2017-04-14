package com.scheme.chc.lockscreen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.scheme.chc.lockscreen.LockScreenActivity;

public class LockScreenReceiver extends BroadcastReceiver {

    // Handle actions and display Lockscreen
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_USER_UNLOCKED)
                || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            startLockScreen(context);
        }
    }

    // Display lock screen
    private void startLockScreen(Context context) {
        System.out.println("start");
        Intent mIntent = new Intent(context, LockScreenActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }
}