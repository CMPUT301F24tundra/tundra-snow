package com.example.tundra_snow_app.Helpers;

import android.content.Context;
import android.provider.Settings;

public class DeviceUtils {

    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
