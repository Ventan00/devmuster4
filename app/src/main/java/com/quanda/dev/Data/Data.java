package com.quanda.dev.Data;

import android.provider.Settings;

import com.quanda.dev.HomeActivity;

public class Data {
    public static String uuid = null;

    public static String getAndroidID() {
        return Settings.Secure.getString(HomeActivity.homeActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static String isUUIDStored() {
        return null;
    }
}
