package com.sage.sage_android.data;

import android.app.Activity;
import android.content.Context;

/**
 * Created by cyberpirate on 3/16/2016.
 */
public class AppInit {

    public static void appInit(Context act) {
        Storage.context = act.getApplicationContext();
        Storage.getInstance();
    }
}
