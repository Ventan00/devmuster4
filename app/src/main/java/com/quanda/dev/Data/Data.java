package com.quanda.dev.Data;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.quanda.dev.HomeActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class Data {
    public static String uuid = null;

    public static String getAndroidID() {
        return Settings.Secure.getString(HomeActivity.homeActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void saveCredentials(Activity activity, String username, String password) {
        String encodedUsername = username;
        String encodedPassword = password;

        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", encodedUsername);
        editor.putString("password", encodedPassword);
        editor.apply();
    }

    public static JSONObject getSavedCredentials(Activity activity) throws JSONException {
        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        String encodedUsername = sharedPreferences.getString("username", null);
        String encodedPassword = sharedPreferences.getString("password", null);

        if (encodedPassword == null || encodedUsername == null) {
            return null;
        } else {
            String decodedUsername = encodedUsername;
            String decodedPassword = encodedPassword;
            JSONObject credentials = new JSONObject();
            credentials.put("username", decodedUsername);
            credentials.put("password", decodedPassword);
            return credentials;
        }
    }

    public static String isUUIDStored() {
        return null;
    }
}
