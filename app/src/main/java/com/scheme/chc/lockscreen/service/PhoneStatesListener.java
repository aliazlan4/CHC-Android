package com.scheme.chc.lockscreen.service;

import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStatesListener extends android.telephony.PhoneStateListener {
    public void onCallStateChanged(int state, String incomingNumber) {

        switch (state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Log.d("DEBUG", "IDLE");
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Log.d("DEBUG", "OFFHOOK");
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Log.d("DEBUG", "RINGING");
                break;
        }
    }
}