package com.example.tundra_snow_app.Helpers;

import android.content.Context;
import android.provider.Settings;

/**
 * Helper class for device related operations.
 */
public class DeviceUtils {

    /**
     * Gets the device ID.
     * @param context The context
     * @return The device ID
     */
    public static String getDeviceID(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
