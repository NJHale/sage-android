package com.sage.sage_android.data;

/**
 * Created by cyberpirate on 4/19/16.
 */
public class GoogleToken {
    String googleIdStr;

    public GoogleToken() {
        googleIdStr = Storage.getInstance().googleToken;
    }
}
