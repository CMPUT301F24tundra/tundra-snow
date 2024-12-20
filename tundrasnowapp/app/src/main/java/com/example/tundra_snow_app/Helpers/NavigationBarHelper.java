package com.example.tundra_snow_app.Helpers;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.example.tundra_snow_app.EventActivities.MyEventViewActivity;
import com.example.tundra_snow_app.Activities.ProfileViewActivity;
import com.example.tundra_snow_app.Activities.QrScanActivity;

import com.example.tundra_snow_app.Activities.SettingsViewActivity;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Helper class for setting up the bottom navigation bar in the user views.
 */
public class NavigationBarHelper {

    /**
     * Sets up the bottom navigation bar for the user views, and defines the navigation actions.
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

        if (item.getItemId() == R.id.nav_events) {
            if (!(activity instanceof EventViewActivity)) {
                intent = new Intent(activity, EventViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.nav_my_events) {
            if (!(activity instanceof MyEventViewActivity)) {
                intent = new Intent(activity, MyEventViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.nav_qr) {
            if (!(activity instanceof QrScanActivity)) {
                intent = new Intent(activity, QrScanActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.nav_profile) {
            if (!(activity instanceof ProfileViewActivity)) {
                intent = new Intent(activity, ProfileViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.nav_settings) {
            if (!(activity instanceof SettingsViewActivity)) {
                intent = new Intent(activity, SettingsViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;
        }
        return false;
    }

    /**
     * Starts an activity with a transition animation.
     */
    private static void startActivityWithTransition(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    // Helper method to set the selected item in the bottom navigation view

    /**
     * Sets the selected item in the bottom navigation view based on the current activity.
     * @param activity The activity
     * @param bottomNavigationView The bottom navigation view
     */
    private static void setSelectedItem(Activity activity, BottomNavigationView bottomNavigationView) {
        if (activity instanceof EventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_events);
        } else if (activity instanceof MyEventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_my_events);
        } else if (activity instanceof QrScanActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_qr);
        } else if (activity instanceof ProfileViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        } else if (activity instanceof SettingsViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        }
    }
}
