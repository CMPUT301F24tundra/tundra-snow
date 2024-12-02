package com.example.tundra_snow_app.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.tundra_snow_app.Helpers.DeviceUtils;
import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.MainActivity;
import com.example.tundra_snow_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

/**
 * SettingsViewActivity allows users to view and update their settings, including
 * notification preferences and geolocation settings. It also provides a logout option
 * that deletes the user's current session and redirects them to the login screen.
 *
 * Features:
 * - Manage notification and geolocation preferences.
 * - Update settings in Firestore for both the user and their current session.
 * - Fetch and update the user's location in Firestore.
 * - Logout functionality with session deletion.
 *
 * This class extends {@link AppCompatActivity}.
 */
public class SettingsViewActivity extends AppCompatActivity {

    private CheckBox notificationsCheckbox, geolocationCheckbox;
    private FirebaseFirestore db;
    private String currentUserID;
    private FusedLocationProviderClient fusedLocationClient;
    private Button logoutButton;

    /**
     * This method is called when the activity is first created.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationBarHelper.setupBottomNavigation(this, bottomNavigationView);

        notificationsCheckbox = findViewById(R.id.notificationsCheckbox);
        geolocationCheckbox = findViewById(R.id.geolocationCheckbox);
        logoutButton = findViewById(R.id.logoutButton);

        db = FirebaseFirestore.getInstance();

        // Load user ID from the latest session first
        fetchUserIdFromSession(() -> {
            Log.d("LogoutProcess", "Fetched current user ID: " + currentUserID);

            loadUserSettings();
            loadSessionSettings();
            logoutButton.setOnClickListener(v -> showLogoutConfirmationDialog());
        });
    }


    /**
     * Shows a confirmation dialog for logout.
     */
    private void showLogoutConfirmationDialog() {
        Log.d("LogoutProcess", "Displaying logout confirmation dialog.");
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout and exit the application?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Log.d("LogoutProcess", "User confirmed logout.");
                    logoutAndExit();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    Log.d("LogoutProcess", "User canceled logout.");
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Logs out the user by deleting the current session and exits the application.
     */
    private void logoutAndExit() {
        if (currentUserID == null) {
            Log.w("LogoutProcess", "No user session found. Unable to log out.");
            Toast.makeText(this, "No user session found. Unable to log out.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("LogoutProcess", "Fetching the latest session to delete.");
        // Find and delete the current session
        db.collection("sessions")
                .whereEqualTo("userId", currentUserID)
                .orderBy("loginTimestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Delete the latest session document
                        String sessionId = querySnapshot.getDocuments().get(0).getId();
                        Log.d("LogoutProcess", "Found session to delete: " + sessionId);
                        db.collection("sessions").document(sessionId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("LogoutProcess", "Successfully deleted session: " + sessionId);
                                    Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                                    redirectToMainActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("LogoutProcess", "Error deleting session: ", e);
                                    Toast.makeText(this, "Error logging out. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.w("LogoutProcess", "No active session found to delete.");
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LogoutProcess", "Error fetching session to delete: ", e);
                    Toast.makeText(this, "Error logging out. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Redirects the user to the MainActivity (login screen) and clears the activity stack.
     */
    private void redirectToMainActivity() {
        Log.d("LogoutProcess", "Redirecting to MainActivity.");
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);
        finish();
    }

    /**
     * Fetches the user ID from the latest session in Firestore.
     */
    private void fetchUserIdFromSession(@NonNull Runnable onComplete) {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        Log.d("LogoutProcess", "Successfully fetched current user ID: " + currentUserID);

                        onComplete.run();
                    } else {
                        Log.w("LogoutProcess", "No active session found.");
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("LogoutProcess", "Error fetching session data: ", e);
                    Toast.makeText(this, "Error fetching session data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads the user's session settings from Firestore.
     */
    private void loadSessionSettings() {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);

                                boolean notificationsEnabled = latestSession.getBoolean("notificationsEnabled") != null
                                        && latestSession.getBoolean("notificationsEnabled");
                                notificationsCheckbox.setChecked(notificationsEnabled);

                                boolean geolocationEnabled = latestSession.getBoolean("geolocationEnabled") != null
                                        && latestSession.getBoolean(("geolocationEnabled"));
                                geolocationCheckbox.setChecked(geolocationEnabled);

                                // Set up listener for checkbox changes
                                notificationsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationToggle(isChecked));
                                geolocationCheckbox.setOnCheckedChangeListener(((buttonView, isChecked) -> onGeolocationToggle(isChecked, latestSession)));
                            } else {
                                Toast.makeText(this, "User session not found.", Toast.LENGTH_SHORT).show();
                            }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user session settings.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads the user's settings from Firestore.
     */
    private void loadUserSettings() {
        if (currentUserID == null) return;

        db.collection("users").document(currentUserID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled") != null
                                && documentSnapshot.getBoolean("notificationsEnabled");
                        notificationsCheckbox.setChecked(notificationsEnabled);

                        boolean geolocationEnabled = documentSnapshot.getBoolean("geolocationEnabled") != null
                                && documentSnapshot.getBoolean(("geolocationEnabled"));
                        geolocationCheckbox.setChecked(geolocationEnabled);

                        // Set up listener for checkbox changes
                        notificationsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationToggle(isChecked));
                        geolocationCheckbox.setOnCheckedChangeListener(((buttonView, isChecked) -> onGeolocationToggle(isChecked, documentSnapshot)));
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user settings.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the user's notification setting in Firestore.
     * @param isChecked True if notifications are enabled, false otherwise.
     */
    private void onNotificationToggle(boolean isChecked) {
        // Update the user's notification setting in Firestore
        db.collection("users").document(currentUserID)
                .update("notificationsEnabled", isChecked)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notification settings updated.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update settings.", Toast.LENGTH_SHORT).show());

        // Update session notification settings
        db.collection("sessions").orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot sessionDoc = querySnapshot.getDocuments().get(0);
                        db.collection("sessions").document(sessionDoc.getId())
                                .update("notificationsEnabled", isChecked)
                                .addOnSuccessListener(aVoid -> Log.d("SettingsViewActivity", "Session notifications updated."))
                                .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to update session notifications.", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to fetch session document.", e));
    }

    /**
     * Updates users geolocation setting in Firestore and device settings
     * @param isChecked True if geolocation is enabled, false otherwise
    */
    private void onGeolocationToggle(boolean isChecked, DocumentSnapshot userSnapshot) {
        Log.d("SettingsViewActivity", "onGeolocationToggle called. isChecked: " + isChecked);

        if (isChecked) {
            boolean isGeolocationEnabled = DeviceUtils.ensureGeolocationEnabled(this);
            Log.d("SettingsViewActivity", "Geolocation enabled status: " + isGeolocationEnabled);

            if (!isGeolocationEnabled) {
                geolocationCheckbox.setChecked(false);
                return;
            }

            String currentLocation = userSnapshot.getString("location");
            Log.d("SettingsViewActivity", "Current location from Firestore: " + currentLocation);

            if (currentLocation == null) {
                Log.d("SettingsViewActivity", "No location found. Fetching user location...");
                fetchUserLocationAndUpdate();
            }
        } else {
            Log.d("SettingsViewActivity", "Geolocation disabled. Setting location to null in Firestore.");

            // Update users geolocation setting
            db.collection("users").document(currentUserID)
                    .update("location", null, "geolocationEnabled", false)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("SettingsViewActivity", "Geolocation disabled successfully in Firestore.");
                        Toast.makeText(this, "Geolocation settings updated.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SettingsViewActivity", "Failed to disable geolocation in Firestore.", e);
                        Toast.makeText(this, "Failed to update settings.", Toast.LENGTH_SHORT).show();
                    });

            // Update the session's geolocation setting
            db.collection("sessions").orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot sessionDoc = querySnapshot.getDocuments().get(0);
                            db.collection("sessions").document(sessionDoc.getId())
                                    .update("geolocationEnabled", false, "location", null)
                                    .addOnSuccessListener(aVoid -> Log.d("SettingsViewActivity", "Session geolocation disabled."))
                                    .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to disable session geolocation.", e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to fetch session document.", e));
        }
    }

    /**
     * Fetches the user's current location using the location provider and updates
     * it in Firestore for both the user and their current session.
     */
    private void fetchUserLocationAndUpdate() {
        Log.d("SettingsViewActivity", "Fetching user location...");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d("SettingsViewActivity", "Location permission granted.");

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            String address = DeviceUtils.getAddressFromLocation(this, location);
                            Log.d("SettingsViewActivity", "Address fetched from location: " + address);

                            // Update users Firestore document
                            db.collection("users").document(currentUserID)
                                    .update("location", address, "geolocationEnabled", true)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("SettingsViewActivity", "Location updated successfully in Firestore.");
                                        Toast.makeText(this, "Location updated in Firestore.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("SettingsViewActivity", "Failed to update location in Firestore.", e);
                                        Toast.makeText(this, "Failed to update location.", Toast.LENGTH_SHORT).show();
                                    });

                            // Update session's Firestore document
                            db.collection("sessions").orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                                    .get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (!querySnapshot.isEmpty()) {
                                            DocumentSnapshot sessionDoc = querySnapshot.getDocuments().get(0);
                                            db.collection("sessions").document(sessionDoc.getId())
                                                    .update("location", address, "geolocationEnabled", true)
                                                    .addOnSuccessListener(aVoid -> Log.d("SettingsViewActivity", "Session location updated in Firestore."))
                                                    .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to update session location.", e));
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("SettingsViewActivity", "Failed to fetch session document.", e));
                        } else {
                            Log.e("SettingsViewActivity", "Unable to fetch location. Location object is null.");
                            Toast.makeText(this, "Unable to fetch location.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("SettingsViewActivity", "Error fetching last known location.", e);
                        Toast.makeText(this, "Failed to fetch location.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.w("SettingsViewActivity", "Location permission not granted.");
            Toast.makeText(this, "Location permission not granted.", Toast.LENGTH_SHORT).show();
        }
    }
}
