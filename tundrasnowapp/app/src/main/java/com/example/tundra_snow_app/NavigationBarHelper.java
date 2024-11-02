package com.example.tundra_snow_app;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationBarHelper {

    // Setting up bottom navigation and defining navigation actions
    public static void setupBottomNavigation(final Activity activity, BottomNavigationView bottomNavigationView) {

        // Set the selected item based on the current activity
        setSelectedItem(activity, bottomNavigationView);

        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> handleNavigationSelection(activity, item));
    }

    private static boolean handleNavigationSelection(Activity activity, MenuItem item) {
        Intent intent;

        if (item.getItemId() == R.id.nav_calendar) {
            if (!(activity instanceof EventViewActivity)) {
                intent = new Intent(activity, EventViewActivity.class);
                startActivityWithTransition(activity, intent);
            }
            return true;

        } else if (item.getItemId() == R.id.nav_events) {
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

    private static void startActivityWithTransition(Activity activity, Intent intent) {
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    // Helper method to set the selected item in the bottom navigation view
    private static void setSelectedItem(Activity activity, BottomNavigationView bottomNavigationView) {
        if (activity instanceof EventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        } else if (activity instanceof MyEventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_events);
        } else if (activity instanceof QrScanActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_qr);
        } else if (activity instanceof ProfileViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        } else if (activity instanceof SettingsViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        }
    }
}
