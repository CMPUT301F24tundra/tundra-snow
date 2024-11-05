package com.example.tundra_snow_app.Activities;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SettingsViewActivity extends AppCompatActivity {

    private CheckBox notificationsCheckbox;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationBarHelper.setupBottomNavigation(this, bottomNavigationView);

        notificationsCheckbox = findViewById(R.id.notificationsCheckbox);  // Ensure this ID is in your layout XML

        db = FirebaseFirestore.getInstance();

        // Load user ID from the latest session first
        fetchUserIdFromSession();
    }

    private void fetchUserIdFromSession() {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        userId = latestSession.getString("userId");

                        // Fetch user-specific settings
                        loadUserSettings();
                    } else {
                        Toast.makeText(this, "Session information not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load session information.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserSettings() {
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        boolean notificationsEnabled = documentSnapshot.getBoolean("notificationsEnabled") != null
                                && documentSnapshot.getBoolean("notificationsEnabled");
                        notificationsCheckbox.setChecked(notificationsEnabled);

                        // Set up listener for checkbox changes
                        notificationsCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> onNotificationToggle(isChecked));
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user settings.", Toast.LENGTH_SHORT).show();
                });
    }

    private void onNotificationToggle(boolean isChecked) {
        // Update the user's notification setting in Firestore
        db.collection("users").document(userId)
                .update("notificationsEnabled", isChecked)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Notification settings updated.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update settings.", Toast.LENGTH_SHORT).show());
    }
}
