package com.scheme.chc.lockscreen.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by Ender on 08-Apr-17 for CHC-Android-master
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class BroadcastHelper {

    /**
     * Keeps track of all the broadcast receivers and their contexts.
     */
    private static ArrayList<Pair<Context, BroadcastReceiver>> receivers = new ArrayList<>();

    /**
     * Registers the receiver via the {@link Context} object passed. The necessary data is returned
     * in the {@link BroadcastListener#onBroadcastReceived(Context, Intent, int)}.<br />
     * <p>
     * Note: In cases where your activity is no longer needed and closed, the receivers need to be
     * unregistered. Use {@link #unregister(int)} or {@link #unregisterAll()} for that.
     *
     * @param context      Object via which the broadcast is to be registered.
     * @param intentFilter Filter for the type of broadcast.
     * @param listener     The data of the broadcast receiver is returned in this listener.
     * @return The {@code id} of the receiver that can be used to unregister it using the
     * {@link #unregister(int)} method.
     */
    public static int broadcast(Context context, IntentFilter intentFilter, final BroadcastListener listener) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (listener != null) {
                    listener.onBroadcastReceived(context, intent, receivers.size());
                }
            }
        };
        receivers.add(new Pair<>(context, broadcastReceiver));
        context.registerReceiver(broadcastReceiver, intentFilter);
        return receivers.size() - 1;
    }

    /**
     * Unregisters the broadcast receiver associated with the {@code id} passed to it. If you aim to
     * unregister all receivers, use {@link #unregisterAll()} for that.
     *
     * @param id Identifier used to locate the receiver. This is obtained when
     *           {@link #broadcast(Context, IntentFilter, BroadcastListener)} is called.
     */
    public static void unregister(int id) {
        Pair<Context, BroadcastReceiver> pair = receivers.remove(id);
        pair.first.unregisterReceiver(pair.second);
    }

    /**
     * Unregisters all the broadcast receivers. If you aim to unregister a certain receiver using an
     * {@code id}, use the {@link #unregister(int)} method.
     */
    public static void unregisterAll() {
        for (Pair<Context, BroadcastReceiver> pair : receivers) {
            pair.first.unregisterReceiver(pair.second);
        }
        receivers.clear();
    }

    /**
     * The listener that returns the data when the broadcast receiver is invoked.
     */
    public interface BroadcastListener {
        /**
         * The listener that returns the data when the broadcast receiver is invoked.
         *
         * @param context The context with which the respective receiver was broadcast-ed.
         * @param intent  The intent object containing the data of the broadcast call.
         * @param id      The id of the receiver.
         */
        void onBroadcastReceived(Context context, Intent intent, int id);
    }
}
