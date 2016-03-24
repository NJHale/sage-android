package com.sage.sage_android.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.sage.sage_android.TaskService;

/**
 * Created by cyberpirate on 3/16/2016.
 */
public class AppInit {

    public static void appInit(Context act) {
        Storage.context = act.getApplicationContext();
        Storage.getInstance();

        if(!TaskService.serviceStarted.get()) {
            act.startService(new Intent(act, TaskService.class));
        }
    }
}
