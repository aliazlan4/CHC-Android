package com.scheme.chc.lockscreen.separated;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.Telephony;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DigitalClock;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.scheme.chc.lockscreen.LockScreenActivity;
import com.scheme.chc.lockscreen.R;
import com.scheme.chc.lockscreen.utils.AppSharedPrefs;
import com.scheme.chc.lockscreen.utils.BroadcastHelper;
import com.scheme.chc.lockscreen.utils.Utilities;
import com.scheme.chc.lockscreen.weather.LocationModel;
import com.scheme.chc.lockscreen.weather.WeatherAPI;
import com.scheme.chc.lockscreen.weather.WeatherModel;

import java.util.Date;

/**
 * Created by Ender on 08-Apr-17 for CHC-Android-master
 */
@SuppressLint("SetTextI18n")
@SuppressWarnings({"deprecation", "FieldCanBeLocal", "unused"})
public class LockScreenLayout implements View.OnTouchListener, View.OnClickListener,
        /*BroadcastHelper.BroadcastListener,*/ WeatherAPI.WeatherListener, LocationListener {

    private TextView tvDate;
    private TextView tvWeather;
    private TextView tvWeatherIcon;
    private TextView tvTemperature;
    private TextView tvPhoneStarting;
    private TextView tvMessageStarting;
    private TextView tvPhoneNotification;
    private TextView tvMessageNotification;

    private DigitalClock dcTime;
    private ProgressBar pbBatteryStatus;

    private FloatingActionButton fabPhone;
    private FloatingActionButton fabSlide;
    private FloatingActionButton fabCamera;
    private FloatingActionButton fabMessage;

    private float dX, orX;
    private double latitude, longitude;
    private int rBattery, rMissedCall, rMessages;

    private LockScreenActivity parentActivity;

    public LockScreenLayout(LockScreenActivity parentActivity) {
        this.parentActivity = parentActivity;
        initialize();
        configure();
    }

    private void initialize() {
        tvDate = (TextView) parentActivity.findViewById(R.id.tvDate);
        tvWeather = (TextView) parentActivity.findViewById(R.id.tvWeather);
        tvWeatherIcon = (TextView) parentActivity.findViewById(R.id.tvWeatherIcon);
        tvTemperature = (TextView) parentActivity.findViewById(R.id.tvTemperature);
        tvPhoneStarting = (TextView) parentActivity.findViewById(R.id.tvPhoneStarting);
        tvMessageStarting = (TextView) parentActivity.findViewById(R.id.tvMessageStarting);
        tvPhoneNotification = (TextView) parentActivity.findViewById(R.id.tvPhoneNotification);
        tvMessageNotification = (TextView) parentActivity.findViewById(R.id.tvMessageNotification);

        dcTime = (DigitalClock) parentActivity.findViewById(R.id.dcTime);
        pbBatteryStatus = (ProgressBar) parentActivity.findViewById(R.id.pbBatteryStatus);

        fabPhone = (FloatingActionButton) parentActivity.findViewById(R.id.fabPhone);
        fabSlide = (FloatingActionButton) parentActivity.findViewById(R.id.fabSlide);
        fabCamera = (FloatingActionButton) parentActivity.findViewById(R.id.fabCamera);
        fabMessage = (FloatingActionButton) parentActivity.findViewById(R.id.fabMessage);

        latitude = 0.0;
        longitude = 0.0;
    }

    private void configure() {
        Typeface tfDengXian = Typeface.createFromAsset(parentActivity.getAssets(), "DengXian.ttf");
        Typeface tfWeatherIcons = Typeface.createFromAsset(parentActivity.getAssets(), "WeatherIcons.ttf");

        tvDate.setTypeface(tfDengXian);
        tvWeather.setTypeface(tfDengXian);
        // tvTemperature.setTypeface(tfDengXian);
        tvPhoneStarting.setTypeface(tfDengXian);
        tvMessageStarting.setTypeface(tfDengXian);
        tvPhoneNotification.setTypeface(tfDengXian);
        tvMessageNotification.setTypeface(tfDengXian);

        dcTime.setTypeface(tfDengXian);
        tvWeatherIcon.setTypeface(tfWeatherIcons);

        tvDate.setText(Utilities.getInstance().getDate());
        rBattery = BroadcastHelper.broadcast(parentActivity.getBaseContext(),
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED), new BroadcastHelper.BroadcastListener() {
                    @Override
                    public void onBroadcastReceived(Context context, Intent intent, int id) {
                        pbBatteryStatus.setProgress(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
                    }
                });
        rMissedCall = BroadcastHelper.broadcast(parentActivity.getBaseContext(),
                new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED), new BroadcastHelper.BroadcastListener() {
                    @Override
                    public void onBroadcastReceived(Context context, Intent intent, int id) {
                        updateMessageCount();
                    }
                });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            rMessages = BroadcastHelper.broadcast(parentActivity.getBaseContext(),
                    new IntentFilter(Telephony.Sms.Intents.DATA_SMS_RECEIVED_ACTION), new BroadcastHelper.BroadcastListener() {
                        @Override
                        public void onBroadcastReceived(Context context, Intent intent, int id) {
                            updateMissedCallsCount();
                        }
                    });
        }

        updateLocation();
        updateMessageCount();
        updateMissedCallsCount();
        new WeatherAPI(latitude, longitude, this).execute();

        fabSlide.setOnTouchListener(this);
        fabPhone.setOnClickListener(this);
        fabCamera.setOnClickListener(this);
        fabMessage.setOnClickListener(this);
    }

    private void updateMessageCount() {
        Cursor cursor = parentActivity.getContentResolver().query(
                Uri.parse("content://sms/inbox"), null, "read = 0", null, null);
        if (cursor != null) {
            tvMessageNotification.setText(cursor.getCount() + " new message(s)");
            cursor.close();
        } else {
            tvMessageNotification.setText("0 new message(s)");
        }
    }

    private void updateMissedCallsCount() {
        String[] projection = {CallLog.Calls.CACHED_NAME, CallLog.Calls.CACHED_NUMBER_LABEL, CallLog.Calls.TYPE};
        String where = CallLog.Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + "=1";

        if (ActivityCompat.checkSelfPermission(parentActivity.getBaseContext(),
                Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Cursor cursor = parentActivity.getContentResolver().query(
                CallLog.Calls.CONTENT_URI, projection, where, null, null);
        if (cursor != null) {
            tvPhoneNotification.setText(cursor.getCount() + " missed call(s)");
            cursor.close();
        } else {
            tvPhoneNotification.setText("0 missed call(s)");
        }
    }

    private void updateLocation() {
        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(parentActivity.getBaseContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationManager locationManager = (LocationManager) parentActivity.getSystemService(Context.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!enabled) {
            showDialogGPS();
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 10, this);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
    }

    private String setWeatherIcon(int actualId, long sunrise, long sunset) {
        int id = 0;
        String icon = "";
        if (actualId <= 200) id = 2;
        else if (actualId <= 300) id = 3;
        else if (actualId <= 500) id = 5;
        else if (actualId <= 600) id = 6;
        else if (actualId <= 700) id = 7;
        else if (actualId <= 800) id = 8;
        if (actualId >= 800) {
            long currentTime = new Date().getTime();
            if (currentTime >= sunrise && currentTime < sunset) {
                icon = "&#xf00d;";
            } else {
                icon = "&#xf02e;";
            }
        } else {
            System.out.println(id);
            switch (id) {
                case 2:
                    icon = "&#xf01e;";
                    break;
                case 3:
                    icon = "&#xf01c;";
                    break;
                case 7:
                    icon = "&#xf014;";
                    break;
                case 8:
                    icon = "&#xf013;";
                    break;
                case 6:
                    icon = "&#xf01b;";
                    break;
                case 5:
                    icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                orX = view.getX();
                dX = orX - event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                view.animate()
                        .x(event.getRawX() + dX)
                        .setDuration(0)
                        .start();
                System.out.println(event.getRawX() + dX);
                break;
            case MotionEvent.ACTION_UP:
                if (view.getX() <= 100.0) {
                    parentActivity.flipNext(LockScreenActivity.NO_INTENT);
                }
                view.animate()
                        .x(orX)
                        .setDuration(500)
                        .start();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == fabPhone) {
            parentActivity.flipNext(LockScreenActivity.INTENT_PHONE);
        } else if (v == fabMessage) {
            parentActivity.flipNext(LockScreenActivity.INTENT_MESSAGES);
        } else if (v == fabCamera) {
            parentActivity.bringOutCamera();
        }
    }

    /*@Override
    public void onBroadcastReceived(Context context, Intent intent, int id) {
        System.out.println("Here with ID: " + id);
        if (id == rBattery) {
            System.out.println("Here in battery changed");
            pbBatteryStatus.setProgress(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0));
        } else if (id == rMessages) {
            System.out.println("Here in message count changed");
            updateMessageCount();
        } else if (id == rMissedCall) {
            System.out.println("Here in missed call count changed");
            updateMissedCallsCount();
        }
    }*/

    @Override
    public void onWeatherReturned(LocationModel locationModel, WeatherModel weatherModel) {
        if ((int) (weatherModel.temperature.getTemp()) > 0) {
            tvWeather.setText(weatherModel.currentCondition.getCondition());
            tvTemperature.setText(Html.fromHtml(weatherModel.temperature.getTemp() + "°C"));
            tvWeatherIcon.setText(Html.fromHtml(setWeatherIcon(weatherModel.currentCondition.getWeatherId(),
                    locationModel.getSunrise(), locationModel.getSunset())));
        } else {
            AppSharedPrefs appSharedPrefs = AppSharedPrefs.getInstance();
            tvWeather.setText(appSharedPrefs.getCondition());
            tvTemperature.setText(Html.fromHtml(appSharedPrefs.getTemperature() + "°C"));
            tvWeatherIcon.setText(Html.fromHtml(setWeatherIcon(appSharedPrefs.getWeatherId(), 0, 0)));
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        new WeatherAPI(latitude, longitude, this).execute();
    }

    /**
     * Show a dialog to the user requesting that GPS be enabled
     */
    private void showDialogGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(parentActivity);
        builder.setCancelable(false);
        builder.setTitle("Enable GPS");
        builder.setMessage("Please enable GPS");
        builder.setInverseBackgroundForced(true);
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                parentActivity.startActivity(
                        new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        builder.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }
}
