package com.scheme.chc.lockscreen.lockscreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.RequiresApi;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
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
import com.scheme.chc.lockscreen.utils.AppSharedPrefs;

import java.util.List;

public class MainLockScreenWindow extends Activity implements LockScreenUtils.OnLockStatusChangedListener, SurfaceHolder.Callback {

    // User-interface
    public static Button btnUnlock;
    public static boolean opensettings = true;
    public static boolean removeview = false;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    // Member variables
    private LockScreenUtils mLockscreenUtils;
    private boolean prefEnableCHC;
    private long userLeaveTime;
    private NotificationBlockView view;

    @Override
    public void onAttachedToWindow() {
        // Set appropriate flags to make the screen appear over the keyguard
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
        this.getWindow().addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                // Self added
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_SECURE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        );
        WindowManager.LayoutParams window = new WindowManager.LayoutParams();
        window.gravity = Gravity.TOP;
        super.onAttachedToWindow();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainlockscreenwindow);
        initializeVariables();
        prefEnableCHC = AppSharedPrefs.getInstance().getEnabledCHC();
        System.out.println("pref is " + prefEnableCHC + "opening  " + opensettings + " " + LockScreenService.notification);

        if (!prefEnableCHC && opensettings || LockScreenService.notification == null) {
            System.out.println("here");
            startActivity(new Intent(MainLockScreenWindow.this, SettingsActivity.class));
        }
        if (isServiceRunning(getString(R.string.ServiceClass)) && !prefEnableCHC) {
            stopService(new Intent(this, LockScreenService.class));
        }
        if (prefEnableCHC) {
            if (getIntent() != null && getIntent().hasExtra("kill")
                    && getIntent().getExtras().getInt("kill") == 1) {
                // Unlock screen in case of app get killed by system
                enableKeyguard();
                unlockHomeButton();
            } else {
                try {
                    disableKeyguard();
                    lockHomeButton();

                    // Start service for observing intents
                    startService(new Intent(this, LockScreenService.class));

                    myPhoneStateListener();
                    if (opensettings) {
                        startActivity(new Intent(MainLockScreenWindow.this, SettingsActivity.class));
                    } else {
                        opensettings = (getIntent().getBooleanExtra("settings", false));
                        System.out.println("settings " + opensettings + "Locked1: " + isScreenLocked() + " " + isServiceRunning(getString(R.string.ServiceClass)));
                        if (!opensettings) {
                            System.out.println("here in");
                            blockNotificationBar(true);
                            startSurfaceViewThread();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("finishing");
            finish();
        }
    }

    private void initializeVariables() {
        mLockscreenUtils = new LockScreenUtils();
        btnUnlock = (Button) findViewById(R.id.btnUnlock);
        surfaceView = (SurfaceView) findViewById(R.id.mysurface);
        surfaceHolder = surfaceView.getHolder();
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Unlock home button and then screen on button press
                unlockHomeButton();
            }
        });
    }

    private void myPhoneStateListener() {
        // listen the events get fired during the call
        StateListener phoneStateListener = new StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void blockNotificationBar(boolean add) {
        WindowManager manager = ((WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        if (add) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            layoutParams.gravity = Gravity.TOP;
            // FLAG_NOT_TOUCH_MODAL : This is to enable the notification to receive touch events
            // FLAG_LAYOUT_IN_SCREEN : Draws over status bar
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = (int) (50 * getResources().getDisplayMetrics().scaledDensity);
            layoutParams.format = PixelFormat.TRANSPARENT;
            view = new NotificationBlockView(this);
            manager.addView(view, layoutParams);
        } else if (prefEnableCHC && removeview
                && LockScreenService.notification != null
                && isServiceRunning(getString(R.string.ServiceClass))) {
            manager.removeView(view);
            removeview = false;
        }
    }

    public boolean isScreenLocked() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        // return (Build.VERSION.SDK_INT < 20 ? powerManager.isInteractive() : powerManager.isInteractive());
        return (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive());
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

    private void startSurfaceViewThread() {
        MainCanvasEngine mainCanvasEngine = new MainCanvasEngine(getApplicationContext(), true, surfaceHolder, surfaceView);
        mainCanvasEngine.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    // Don't finish Activity on Back press
    @Override
    public void onBackPressed() {
    }

    // Handle button clicks
    @SuppressLint("InlinedApi")
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        return (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)
                || (keyCode == KeyEvent.KEYCODE_HOME)
                || (keyCode == KeyEvent.KEYCODE_APP_SWITCH);

    }

    // Handle the key press events here itself
    @SuppressLint("InlinedApi")
    public boolean dispatchKeyEvent(KeyEvent event) {
        return !(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER))
                && (event.getKeyCode() == KeyEvent.KEYCODE_HOME)
                && (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH);
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

    @SuppressLint("InlinedApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onStop() {
        super.onStop();

        long defStop = System.currentTimeMillis() - userLeaveTime;
        if (defStop < 200) {
            // Means recent apps button is pressed
            if (prefEnableCHC && LockScreenService.notification != null && !opensettings && isScreenLocked() && isServiceRunning(getString(R.string.ServiceClass))) {
                unlockDevice();
                startActivity(new Intent(this, MainLockScreenWindow.class));
                System.out.println("in stop");
            }
        } else {
            // Back button or home
            blockNotificationBar(false);
            unlockHomeButton();
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        userLeaveTime = System.currentTimeMillis();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        super.onPause();
        if (prefEnableCHC && LockScreenService.notification != null && !opensettings && isScreenLocked() && isServiceRunning(getString(R.string.ServiceClass))) {
            unlockDevice();
            startActivity(new Intent(this, MainLockScreenWindow.class));
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            System.out.println("in window");
            unlockDevice();
            startActivity(new Intent(this, MainLockScreenWindow.class));
        }
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
    private void unlockDevice() {
        finish();
    }

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
}
