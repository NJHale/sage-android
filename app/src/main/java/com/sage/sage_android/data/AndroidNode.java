package com.sage.sage_android.data;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.google.gson.GsonBuilder;
import android.os.Build;
import android.util.Log;


/**
 * Created by cyberpirate on 3/16/2016.
 */
public class AndroidNode {

    public String androidId;
    public String ownerId;
    public String nodeId;
    public String info;

    public AndroidNode() {

        androidId = getUUID();

        ownerId = "";
        info = new GsonBuilder().create().toJson(new BuildInfo());
    }

    public static String getUUID() {
        TelephonyManager tManager = (TelephonyManager) Storage.context.getSystemService(Context.TELEPHONY_SERVICE);
        return tManager.getDeviceId();
    }

    private class BuildInfo {
        public String BOARD = Build.BOARD;
        public String BOOTLOADER = Build.BOOTLOADER;
        public String BRAND = Build.BRAND;
        public String DEVICE = Build.DEVICE;
        public String DISPLAY = Build.DISPLAY;
        public String FINGERPRINT = Build.FINGERPRINT;
        public String HARDWARE = Build.HARDWARE;
        public String HOST = Build.HOST;
        public String ID = Build.ID;
        public String MANUFACTURER = Build.MANUFACTURER;
        public String MODEL = Build.MODEL;
        public String PRODUCT = Build.PRODUCT;
        public String SERIAL = Build.SERIAL;
        public String[] SUPPORTED_32_BIT_ABIS = Build.SUPPORTED_32_BIT_ABIS;
        public String[] SUPPORTED_64_BIT_ABIS = Build.SUPPORTED_64_BIT_ABIS;
        public String[] SUPPORTED_ABIS = Build.SUPPORTED_ABIS;
        public String TAGS = Build.TAGS;
        public long TIME = Build.TIME;
        public String TYPE = Build.TYPE;
        public String USER = Build.USER;
    }
}
