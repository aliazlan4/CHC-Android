package com.scheme.chc.lockscreen.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.scheme.chc.lockscreen.LockScreenActivity;
import com.scheme.chc.lockscreen.utils.Cryptographer;

import java.io.File;

public class LockScreenReceiver extends BroadcastReceiver {

    private Cryptographer cryptographer;

    // Handle actions and display Lockscreen
    @Override
    public void onReceive(Context context, Intent intent) {
        cryptographer = Cryptographer.getInstance(context);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)
                || intent.getAction().equals(Intent.ACTION_USER_UNLOCKED)
                || intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            startLockScreen(context);
        }

        if ((intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                || (intent.getAction().equals(Intent.ACTION_LOCKED_BOOT_COMPLETED))) {
            System.out.println("OFF screen");
            // traverseAndroidDirectoriesAndEncrypt(new File(System.getenv("EXTERNAL_STORAGE") + "/"), true);
        }

    }

    // Display lock screen
    private void startLockScreen(Context context) {
        System.out.println("start");
        Intent mIntent = new Intent(context, LockScreenActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mIntent);
    }

    private void traverseAndroidDirectoriesAndEncrypt(File dir, Boolean encrytFiles) {

        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!(file.getName().equals(".android_secure"))) {
                    System.out.println(file.getName());
                    if (file.isDirectory()) {
                        traverseAndroidDirectoriesAndEncrypt(file, encrytFiles);
                    } else {
                        if (encrytFiles) {
                            System.out.println("encrypting File");
                            cryptographer.encryptFile(file);
                        } else {
                            System.out.println("decrypting File");
                            cryptographer.decryptFile(file);
                        }
                    }
                }
            }
        }
    }
}