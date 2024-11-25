package com.example.tundra_snow_app.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


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

    /**
     * Check if geolocation is enabled on this device.
     * Disable sign-up button and prompt user if not enabled
     * @param activity To go to the settings activity
     */

    public static boolean ensureGeolocationEnabled(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGpsEnabled && !isNetworkEnabled) {
            Toast.makeText(activity, "Location services are disabled. Please enable them.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
            return false;
        }

        return true;
    }

    /**
     * Gets current address from Device location settings
     *
     * @param context  The context used for accessing resources.
     * @param location Variable to store the location within the database document
     * @return Address of user
     */
    public static String getAddressFromLocation(Context context, Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return address.getAddressLine(0);
            }
        } catch (IOException e) {
            Log.e("EntrantSignupActivity", "Error converting location to address", e);
        }

        return "Unknown Address";
    }
}
