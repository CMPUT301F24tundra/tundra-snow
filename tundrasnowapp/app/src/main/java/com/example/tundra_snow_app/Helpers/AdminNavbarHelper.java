package com.example.tundra_snow_app.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.example.tundra_snow_app.AdminActivities.AdminEventViewActivity;
import com.example.tundra_snow_app.AdminActivities.AdminFacilityViewActivity;
import com.example.tundra_snow_app.AdminActivities.AdminImagesViewActivity;
import com.example.tundra_snow_app.AdminActivities.AdminQRViewActivity;
import com.example.tundra_snow_app.AdminActivities.AdminUsersViewActivity;
import com.example.tundra_snow_app.Activities.QrScanActivity;

import com.example.tundra_snow_app.Activities.SettingsViewActivity;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class for setting up the bottom navigation bar in the admin views.
 */
public class AdminNavbarHelper {

    /**
     * Sets up the bottom navigation bar for the admin views.
     * @param activity The activity
     * @param bottomNavigationView The bottom navigation view
     */
    public static void setupBottomNavigation(final Activity activity, BottomNavigationView bottomNavigationView) {

        // Set the selected item based on the current activity
        setSelectedItem(activity, bottomNavigationView);

        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> handleNavigationSelection(activity, item));
    }

    /**
     * Handles the selection of a navigation item in the bottom navigation bar.
     * @param activity The activity
     * @param item The selected item
     * @return True if the selection was handled, false otherwise
     */
    private static boolean handleNavigationSelection(Activity activity, MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.admin_nav_events) {
            if (!(activity instanceof AdminEventViewActivity)) {
                intent = new Intent(activity, AdminEventViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.admin_nav_facilities) {
            if (!(activity instanceof AdminFacilityViewActivity)) {
                intent = new Intent(activity, AdminFacilityViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.admin_nav_qr) {
            if (!(activity instanceof AdminQRViewActivity)) {
                intent = new Intent(activity, AdminQRViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.admin_nav_profiles) {
            if (!(activity instanceof AdminUsersViewActivity)) {
                intent = new Intent(activity, AdminUsersViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.admin_nav_images) {
            if (!(activity instanceof SettingsViewActivity)) {
                intent = new Intent(activity, AdminImagesViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;
        }
        return false;
    }

    /**
     * Starts an activity with a transition animation.
     * @param activity The activity
     * @param intent The intent
     */
    private static void startActivityWithTransition(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    /**
     * Sets the selected item in the bottom navigation bar based on the current activity.
     * @param activity The activity
     * @param bottomNavigationView The bottom navigation view
     */
    private static void setSelectedItem(Activity activity, BottomNavigationView bottomNavigationView) {
        if (activity instanceof AdminEventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_events);
        } else if (activity instanceof AdminFacilityViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_facilities);
        } else if (activity instanceof AdminQRViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_qr);
        } else if (activity instanceof AdminUsersViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_profiles);
        } else if (activity instanceof AdminImagesViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_images);
        }
    }
}
