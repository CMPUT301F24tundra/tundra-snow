package com.example.tundra_snow_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class ProfileViewActivity extends AppCompatActivity {

    private EditText profileName, profileEmail, profilePhone, profileFacilities;
    private Button editButton, saveButton;
    private ToggleButton modeToggle;
    private LinearLayout profileSection, facilitiesSection;
    private FirebaseFirestore db;
    private String userId;
    private ArrayList<String> facilitiesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationBarHelper.setupBottomNavigation(this, bottomNavigationView);

        // Initialize UI elements
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        profileFacilities = findViewById(R.id.profileFacilities);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        modeToggle = findViewById(R.id.modeToggle);

        profileSection = findViewById(R.id.profileSection);
        facilitiesSection = findViewById(R.id.facilitiesSection);

        db = FirebaseFirestore.getInstance();

        fetchUserIdFromSession();

        editButton.setOnClickListener(v -> enableEditing(true));
        saveButton.setOnClickListener(v -> saveProfileUpdates());

        // Toggle visibility between profile and facilities section
        modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                profileSection.setVisibility(View.GONE);
                facilitiesSection.setVisibility(View.VISIBLE);
            } else {
                profileSection.setVisibility(View.VISIBLE);
                facilitiesSection.setVisibility(View.GONE);
            }
        });
    }

    private void fetchUserIdFromSession() {
        CollectionReference sessionsRef = db.collection("sessions");

        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        userId = latestSession.getString("userId");

                        fetchUserProfile();
                    } else {
                        Toast.makeText(this, "Session information not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load session information.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void fetchUserProfile() {
        if (userId == null) return;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("firstName") != null ? documentSnapshot.getString("firstName") : "";
                        String lastName = documentSnapshot.getString("lastName") != null ? documentSnapshot.getString("lastName") : "";
                        profileName.setText(getString(R.string.profile_name_format, firstName, lastName));
                        profileEmail.setText(documentSnapshot.getString("email") != null ? documentSnapshot.getString("email") : getString(R.string.default_email));
                        profilePhone.setText(documentSnapshot.getString("phoneNumber") != null ? documentSnapshot.getString("phoneNumber") : getString(R.string.default_phone));

                        facilitiesList = (ArrayList<String>) documentSnapshot.get("facilityList");
                        profileFacilities.setText(facilitiesList != null ? String.join(", ", facilitiesList) : "");
                        enableEditing(false);
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void enableEditing(boolean isEditable) {
        profileName.setEnabled(isEditable);
        profileEmail.setEnabled(isEditable);
        profilePhone.setEnabled(isEditable);
        profileFacilities.setEnabled(isEditable);

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    private void saveProfileUpdates() {
        String[] nameParts = profileName.getText().toString().split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String email = profileEmail.getText().toString();
        String phone = profilePhone.getText().toString();

        String facilitiesInput = profileFacilities.getText().toString();
        facilitiesList = new ArrayList<>(Arrays.asList(facilitiesInput.split("\\s*,\\s*")));

        db.collection("users").document(userId)
                .update("firstName", firstName,
                        "lastName", lastName,
                        "email", email,
                        "phoneNumber", phone,
                        "facilityList", facilitiesList)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }
}
