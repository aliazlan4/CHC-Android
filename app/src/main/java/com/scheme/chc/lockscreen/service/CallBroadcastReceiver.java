package com.scheme.chc.lockscreen.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class CallBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PhoneStatesListener phoneListener = new PhoneStatesListener();
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, android.telephony.PhoneStateListener.LISTEN_CALL_STATE);
    }
}
