package com.example.biketracker.DB;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    static final String PREF_EMAIL = "email";
    static final String PREF_GROUP_NAME = "group";
    static final String PREF_DEVICE_NAME = "device";

    static final String PREF_YGGIO_TOKEN = "token";

    static SharedPreferences getSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void setEmail(Context context, String email) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_EMAIL, email);
        editor.apply();
    }

    public static String getEmail(Context context) {
        return getSharedPreferences(context).getString(PREF_EMAIL, "");
    }

    public static void setGroupName(Context context, String name) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_GROUP_NAME, name);
        editor.apply();
    }

    public static String getGroupName(Context context) {
        return getSharedPreferences(context).getString(PREF_GROUP_NAME, "");
    }

    public static void setDeviceName(Context context, String name) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_DEVICE_NAME, name);
        editor.apply();
    }

    public static String getDeviceName(Context context) {
        return getSharedPreferences(context).getString(PREF_DEVICE_NAME, "");
    }

    public static void setYggioToken(Context context, String token) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_YGGIO_TOKEN, token);
        editor.apply();
    }

    public static String getYggioToken(Context context) {
        return getSharedPreferences(context).getString(PREF_YGGIO_TOKEN, "");
    }
}
