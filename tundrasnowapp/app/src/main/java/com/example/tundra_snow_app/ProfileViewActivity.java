package com.example.tundra_snow_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewActivity extends AppCompatActivity {

    private EditText profileName, profileEmail, profilePhone;
    private Button editButton, saveButton, addFacilityButton;
    private ListView facilitiesListView;
    private FirebaseFirestore db;
    private String userId;
    private ArrayList<String> facilitiesList;
    private ArrayAdapter<String> facilitiesAdapter;
    private ToggleButton modeToggle;
    private View profileSection, facilitiesSection;

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
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        addFacilityButton = findViewById(R.id.addFacilityButton);
        facilitiesListView = findViewById(R.id.facilitiesListView);
        modeToggle = findViewById(R.id.modeToggle);
        profileSection = findViewById(R.id.profileSection);
        facilitiesSection = findViewById(R.id.facilitiesSection);

        db = FirebaseFirestore.getInstance();
        facilitiesList = new ArrayList<>();

        facilitiesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, facilitiesList);
        facilitiesListView.setAdapter(facilitiesAdapter);

        fetchUserIdFromSession();

        editButton.setOnClickListener(v -> enableEditing(true));
        saveButton.setOnClickListener(v -> saveProfileUpdates());
        addFacilityButton.setOnClickListener(v -> showFacilityDialog(null));

        facilitiesListView.setOnItemClickListener((parent, view, position, id) -> showFacilityDialog(position));
        facilitiesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            removeFacility(position);
            return true;
        });

        // Toggle between User and Organizer mode
        modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Organizer Mode: Show facilities section, hide profile section
                profileSection.setVisibility(View.GONE);
                facilitiesSection.setVisibility(View.VISIBLE);
            } else {
                // User Mode: Show profile section, hide facilities section
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

                        List<String> facilitiesFromDB = (List<String>) documentSnapshot.get("facilityList");
                        facilitiesList.clear();
                        if (facilitiesFromDB != null) {
                            facilitiesList.addAll(facilitiesFromDB);
                        }
                        facilitiesAdapter.notifyDataSetChanged();
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

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        addFacilityButton.setEnabled(isEditable);
    }

    private void saveProfileUpdates() {
        String[] nameParts = profileName.getText().toString().split(" ");
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String email = profileEmail.getText().toString();
        String phone = profilePhone.getText().toString();

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

    private void showFacilityDialog(Integer position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(position == null ? "Add Facility" : "Edit Facility");

        final EditText input = new EditText(this);
        input.setHint("Enter facility name");
        if (position != null) input.setText(facilitiesList.get(position));
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String facility = input.getText().toString().trim();
            if (position == null) {
                facilitiesList.add(facility);
            } else {
                facilitiesList.set(position, facility);
            }
            facilitiesAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void removeFacility(int position) {
        facilitiesList.remove(position);
        facilitiesAdapter.notifyDataSetChanged();
    }
}
