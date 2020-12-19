package com.quanda.dev;

import android.provider.Settings;

public class Data {
    public static String uuid = null;

    public static String getAndroidID() {
        return Settings.Secure.getString(HomeActivity.homeActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String isUUIDStored() {
        return null;
    }
}
