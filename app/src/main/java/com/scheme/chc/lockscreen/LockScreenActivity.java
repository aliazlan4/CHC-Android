package com.scheme.chc.lockscreen;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ViewFlipper;

import com.scheme.chc.lockscreen.separated.CameraLayout;
import com.scheme.chc.lockscreen.separated.ConvexHullClickEngine;
import com.scheme.chc.lockscreen.separated.LockScreenLayout;
import com.scheme.chc.lockscreen.service.LockScreenUtils;
import com.scheme.chc.lockscreen.utils.BroadcastHelper;
import com.scheme.chc.lockscreen.utils.NotificationBlockView;
import com.scheme.chc.lockscreen.utils.OnSwipeTouchListener;

@SuppressWarnings({"deprecation", "StaticFieldLeak"})
public class LockScreenActivity extends AppCompatActivity implements LockScreenUtils.OnLockStatusChangedListener {
    public static final int NO_INTENT = 21;
    public static final int INTENT_PHONE = 22;
    public static final int INTENT_NULL = 100;
    public static final int INTENT_MESSAGES = 24;

    public static Button btnUnlock;
    public static boolean shouldRemoveView = false;

    private ViewFlipper vfFlipper;
    private CameraLayout cameraLayout;
    private LockScreenLayout lockScreenLayout;

    private long userLeaveTime;
    private NotificationBlockView view;
    private LockScreenUtils lockScreenUtils;
    private ConvexHullClickEngine convexHullClickEngine;

//    @Override
//    public void onAttachedToWindow() {
//        // Set appropriate flags to make the screen appear over the keyguard
//        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
//        this.getWindow().addFlags(
//                          WindowManager.LayoutParams.FLAG_FULLSCREEN
//                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
//                        // Self added
//                        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
//                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//                        | WindowManager.LayoutParams.FLAG_IGNORE_CHEEK_PRESSES
//                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                        | WindowManager.LayoutParams.FLAG_SECURE
//                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
//        );
//        WindowManager.LayoutParams window = new WindowManager.LayoutParams();
//        window.gravity = Gravity.TOP;
//        super.onAttachedToWindow();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        initialize();
        configure();
    }

    private void initialize() {
        btnUnlock = (Button) findViewById(R.id.btnUnlock);

        vfFlipper = (ViewFlipper) findViewById(R.id.vfFlipper);
        lockScreenLayout = new LockScreenLayout(this);
        lockScreenUtils = new LockScreenUtils();
        cameraLayout = new CameraLayout(this);

        lockHomeButton(); //not works
        disableKeyguard();  //back works
        myPhoneStateListener(); //back works
        blockNotificationBar(true); //back works
    }

    private void configure() {
        vfFlipper.setDisplayedChild(1);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlockHomeButton();
                blockNotificationBar(false);
            }
        });
        vfFlipper.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                System.out.println("Right");

                //camera
                if (convexHullClickEngine != null) {
                    startActivity(new Intent(LockScreenActivity.this, LockScreenActivity.class));
                    //LockScreenActivity.shouldRemoveView = true;
                }
                flipPrevious();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                //chc
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    System.out.println("Left");
                    cameraLayout.camView.setVisibility(View.GONE);
                }
                flipNext(INTENT_NULL);
            }
        });
    }

    // Lock home button
    public void lockHomeButton() {
        lockScreenUtils.lock(LockScreenActivity.this);
    }

    // Unlock home button and wait for its callback
    public void unlockHomeButton() {
        lockScreenUtils.unlock();
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
        } else {
            manager.removeView(view);
            unlockHomeButton();
            enableKeyguard();
            LockScreenActivity.shouldRemoveView = false;
        }
    }

    private void disableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.disableKeyguard();
    }

    private void enableKeyguard() {
        KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
        mKL.reenableKeyguard();
    }

    public void bringOutCamera() {
        flipPrevious();
        cameraLayout.cameraActivated = true;
    }

    public void flipNext(int intentChooser) {
        vfFlipper.setInAnimation(this, R.anim.slide_in_left);
        vfFlipper.setOutAnimation(this, R.anim.slide_out_left);
        vfFlipper.showNext();
        // Start the CHC Scheme with the right intent
        if (intentChooser != -1) startSurfaceViewThread(intentChooser);
    }

    public void flipPrevious() {
        vfFlipper.setInAnimation(this, R.anim.slide_in_right);
        vfFlipper.setOutAnimation(this, R.anim.slide_out_right);
        vfFlipper.showPrevious();
    }

    private void myPhoneStateListener() {
        // Listen the events get fired during the call
        LockScreenActivity.StateListener phoneStateListener = new LockScreenActivity.StateListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void startSurfaceViewThread(int intentChooser) {
        convexHullClickEngine = new ConvexHullClickEngine(this, true, intentChooser);
        convexHullClickEngine.start();
    }

    @SuppressLint("NewApi")
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        System.out.println("Back pressed.");
        if (vfFlipper.getDisplayedChild() == 0) {
            System.out.println("Flipping from camera to lock screen");
            flipNext(-1);
        } else if (vfFlipper.getDisplayedChild() == 1) {
            System.out.println("Back button doesn't work on the lock screen");
            // Do nothing
        } else if (vfFlipper.getDisplayedChild() == 2) {
            System.out.println("Coming back from CHC to lock screen");
            flipPrevious(); //this isnt working
            recreate();
        }
    }

    @Override
    public void onLockStatusChanged(boolean isLocked) {
        // Simply unlock device when home button is successfully unlocked
        if (!isLocked) {
            finish();
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onStop() {
        super.onStop();
        long defStop = System.currentTimeMillis() - userLeaveTime;
        if (defStop < 100) {
            finish();
            startActivity(new Intent(this, LockScreenActivity.class));
            System.out.println("in stop");
        } else {
            // Back button or home
            if (shouldRemoveView) {
                System.out.println("STOP");
                blockNotificationBar(false);
//                unlockHomeButton();
                //shouldRemoveView = false;
            } else {
                System.out.println("Coming here");
            }
        }
        BroadcastHelper.unregisterAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraLayout.cameraActivated) {
            cameraLayout.cameraActivated = false;
            System.out.println("Pause camera activity");
            flipNext(-1);
        }
        if (shouldRemoveView) {
            blockNotificationBar(false);
            System.out.println("Paused");
            //shouldRemoveView = false;
        } else {
            System.out.println("Pause finishing activity");
        }
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        userLeaveTime = System.currentTimeMillis();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Handle the key press events here itself
        return !(event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)
                || (event.getKeyCode() == KeyEvent.KEYCODE_CAMERA))
                && (event.getKeyCode() == KeyEvent.KEYCODE_HOME);
        // && (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH);
    }

//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (!hasFocus) {
//            System.out.println("in window");
//            finish();
//            startActivity(new Intent(this, LockScreenActivity.class));
//        }
//    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        // super.onKeyDown(keyCode, event);
        return (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)
                || (keyCode == KeyEvent.KEYCODE_HOME);
    }

    // Handle events of calls and unlock screen if necessary
    private class StateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    System.out.println("1");
                    startActivity(new Intent(LockScreenActivity.this, LockScreenActivity.class));
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    System.out.println("2");
                    startActivity(new Intent(LockScreenActivity.this, LockScreenActivity.class));
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    System.out.println("3");
                    break;
            }
        }
    }
}
