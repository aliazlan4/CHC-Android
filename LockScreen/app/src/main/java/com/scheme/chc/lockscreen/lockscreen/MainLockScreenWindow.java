package com.scheme.chc.lockscreen.lockscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.scheme.chc.lockscreen.R;
import com.scheme.chc.lockscreen.service.LockScreenService;
import com.scheme.chc.lockscreen.service.LockScreenUtils;
import com.scheme.chc.lockscreen.settings.SettingsActivity;

import java.util.List;

public class MainLockScreenWindow extends Activity implements
        LockScreenUtils.OnLockStatusChangedListener, SurfaceHolder.Callback{

    // User-interface
    public static Button btnUnlock;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    // Member variables
    private LockScreenUtils mLockscreenUtils;
    public static boolean opensettings = true;

    // Set appropriate flags to make the screen appear over the keyguard
    @Override
    public void onAttachedToWindow() {
        this.getWindow().setType(
                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        this.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        );

        super.onAttachedToWindow();
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlockscreenwindow);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref_enablechc = (preferences.getBoolean("enablechc", false));

        System.out.println("pref is " + pref_enablechc + " " + LockScreenService.notification);
        if((!pref_enablechc && opensettings) && LockScreenService.notification == null){
            System.out.println("here");
            startActivity(new Intent(MainLockScreenWindow.this,SettingsActivity.class));
        }

        mLockscreenUtils = new LockScreenUtils();
        initializeVariables();

        if(pref_enablechc) {

            // unlock screen in case of app get killed by system
            if (getIntent() != null && getIntent().hasExtra("kill")
                    && getIntent().getExtras().getInt("kill") == 1) {
                enableKeyguard();
                unlockHomeButton();
            } else {
                try {
                    disableKeyguard();
                    lockHomeButton();
                    // start service for observing intents
                    startService(new Intent(this, LockScreenService.class));
                    System.out.println("Locked: " +isScreenLocked() + "" +opensettings);
                    if(isScreenLocked() && opensettings){        //should be not
                        System.out.println("here");
                        startActivity(new Intent(MainLockScreenWindow.this,SettingsActivity.class));
                    }
                    else {
                        opensettings = (getIntent().getBooleanExtra("settings", false));
                        System.out.println("settings " + opensettings + "Locked1: " + isScreenLocked() + " " + LockScreenService.notification + " " + isServiceRunning(getString(R.string.ServiceClass)));
                        if (LockScreenService.notification != null && !opensettings && isScreenLocked() && isServiceRunning(getString(R.string.ServiceClass))) {
                            opensettings = true;
                            startSurfaceViewThread();
                            System.out.println("here1");
                        }
                    }

                    // listen the events get fired during the call
                    StateListener phoneStateListener = new StateListener();
                    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

                } catch (Exception ignored) {
                }
            }
        }
    }


    @SuppressLint("NewApi")
    public boolean isScreenLocked(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return (Build.VERSION.SDK_INT < 20 ? powerManager.isScreenOn() : powerManager.isInteractive());
    }

    public boolean isServiceRunning(String serviceClassName){
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClassName)){
                return true;
            }
        }
        return false;
    }

    private void initializeVariables() {
        btnUnlock = (Button) findViewById(R.id.btnUnlock);
        surfaceView = (SurfaceView) findViewById(R.id.mysurface);
        surfaceHolder = surfaceView.getHolder();
        btnUnlock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // unlock home button and then screen on button press
                unlockHomeButton();
            }
        });

    }

    private void startSurfaceViewThread() {
        MainCanvasEngine mainCanvasEngine = new MainCanvasEngine(getApplicationContext(), true, surfaceHolder, surfaceView);
        mainCanvasEngine.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {}

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {}

    // Handle events of calls and unlock screen if necessary
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    unlockHomeButton();
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    break;
            }
        }
    }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {}

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        return (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)
                || (keyCode == KeyEvent.KEYCODE_HOME);

    }

    // handle the key press events here itself
    public boolean dispatchKeyEvent(KeyEvent event) {
        return !(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER))
                && (event.getKeyCode() == KeyEvent.KEYCODE_HOME);
    }

    // Lock home button
    public void lockHomeButton() {
        mLockscreenUtils.lock(MainLockScreenWindow.this);
    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        mLockscreenUtils.unlock();
    }

    // Simply unlock device when home button is successfully unlocked
    @Override
    public void onLockStatusChanged(boolean isLocked) {
        if (!isLocked) {
            unlockDevice();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unlockHomeButton();
    }

    @SuppressWarnings("deprecation")
    private void disableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.disableKeyguard();
    }

    @SuppressWarnings("deprecation")
    private void enableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.reenableKeyguard();
    }

    //Simply unlock device by finishing the activity
    private void unlockDevice()
    {
        finish();
    }

}