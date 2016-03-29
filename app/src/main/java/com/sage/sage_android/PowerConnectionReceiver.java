package com.sage.sage_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

import com.sage.sage_android.data.AppInit;

/**
 * Created by cyberpirate on 3/23/2016.
 */
public class PowerConnectionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AppInit.appInit(context);

        if(TaskService.serviceStarted.get()) {
            TaskService.taskRunner.checkPause();
        }
    }
}