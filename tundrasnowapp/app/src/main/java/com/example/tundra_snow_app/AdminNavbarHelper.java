package com.example.tundra_snow_app;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminNavbarHelper {

    // Setting up bottom navigation and defining navigation actions
    public static void setupBottomNavigation(final Activity activity, BottomNavigationView bottomNavigationView) {

        // Set the selected item based on the current activity
        setSelectedItem(activity, bottomNavigationView);

        // Set up navigation item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> handleNavigationSelection(activity, item));
    }

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
            if (!(activity instanceof QrScanActivity)) {
                intent = new Intent(activity, QrScanActivity.class);
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
        if (activity instanceof AdminEventViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_events);
        } else if (activity instanceof AdminFacilityViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_facilities);
        } else if (activity instanceof QrScanActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_qr);
        } else if (activity instanceof AdminUsersViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_profiles);
        } else if (activity instanceof SettingsViewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.admin_nav_images);
        }
    }
}
