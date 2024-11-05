package com.example.tundra_snow_app.Activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.ListAdapters.FacilityListAdapter;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing and editing the user's profile information.
 */
public class ProfileViewActivity extends AppCompatActivity {

    private EditText profileName, profileEmail, profilePhone;
    private Button editButton, saveButton, addFacilityButton;
    private ListView facilitiesListView;
    private FacilityListAdapter adapter;
    private FirebaseFirestore db;
    private String userId;
    private List<String> facilities = new ArrayList<>();

    private ToggleButton modeToggle; // Organizer/User toggle
    private View profileSection;
    private View facilitiesSection;

    /**
     * Initializes the activity and sets up the UI elements.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);

        // Set up bottom navigation
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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch user ID from the latest session
        fetchUserIdFromSession();

        // Set up button listeners
        editButton.setOnClickListener(v -> enableEditing(true));
        saveButton.setOnClickListener(v -> saveProfileUpdates());
        addFacilityButton.setOnClickListener(v -> showAddFacilityDialog());

        // Set up ToggleButton listener to switch views
        modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showOrganizerView();
            } else {
                showUserView();
            }
        });
    }

    /**
     * Shows the user view and hides the organizer view.
     */
    private void showUserView() {
        profileSection.setVisibility(View.VISIBLE);
        facilitiesSection.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);  // Show Edit button in User mode
        saveButton.setVisibility(View.GONE);     // Hide Save button initially in User mode
    }

    /**
     * Shows the organizer view and hides the user view.
     */
    private void showOrganizerView() {
        profileSection.setVisibility(View.GONE);
        facilitiesSection.setVisibility(View.VISIBLE);
        editButton.setVisibility(View.GONE);     // Hide Edit button in Organizer mode
        saveButton.setVisibility(View.GONE);     // Hide Save button in Organizer mode
        setupFacilitiesListView();               // Ensure the facilities are displayed in Organizer mode
    }

    /**
     * Fetches the user ID from the latest session in Firestore.
     */
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

    /**
     * Fetches the user's profile information from Firestore.
     */
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

                        facilities = (List<String>) documentSnapshot.get("facilityList");
                        if (facilities == null) facilities = new ArrayList<>();
                        setupFacilitiesListView();
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

    /**
     * Sets up the facilities ListView with the user's facilities.
     */
    private void setupFacilitiesListView() {
        adapter = new FacilityListAdapter(this, facilities, this::showFacilityOptionsDialog);
        facilitiesListView.setAdapter(adapter);
    }

    /**
     * Enables or disables editing of the profile information.
     * @param isEditable True to enable editing, false to disable.
     */
    private void enableEditing(boolean isEditable) {
        profileName.setEnabled(isEditable);
        profileEmail.setEnabled(isEditable);
        profilePhone.setEnabled(isEditable);

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    /**
     * Saves the updated profile information to Firestore.
     */
    private void saveProfileUpdates() {
        String[] nameParts = profileName.getText().toString().split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String email = profileEmail.getText().toString();
        String phone = profilePhone.getText().toString();

        db.collection("users").document(userId)
                .update("firstName", firstName,
                        "lastName", lastName,
                        "email", email,
                        "phoneNumber", phone,
                        "facilityList", facilities)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    /**
     * Shows a dialog to add a new facility to the user's profile.
     */
    private void showAddFacilityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Facility");

        final EditText input = new EditText(this);
        input.setHint("Enter facility name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newFacility = input.getText().toString().trim();
            if (!newFacility.isEmpty()) {
                facilities.add(newFacility);
                adapter.notifyDataSetChanged();
                saveProfileUpdates();
            } else {
                Toast.makeText(this, "Facility name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Shows a dialog with options to edit or delete a facility.
     * @param position The position of the facility in the list.
     * @param facility The name of the facility.
     */
    private void showFacilityOptionsDialog(int position, String facility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Facility Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) showEditFacilityDialog(position, facility);
                    else if (which == 1) showDeleteFacilityDialog(position);
                })
                .show();
    }

    /**
     * Shows a dialog to edit the name of a facility.
     * @param position The position of the facility in the list.
     * @param facility The name of the facility.
     */
    private void showEditFacilityDialog(int position, String facility) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Facility");

        final EditText input = new EditText(this);
        input.setText(facility);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedFacility = input.getText().toString().trim();
            if (!updatedFacility.isEmpty()) {
                facilities.set(position, updatedFacility);
                adapter.notifyDataSetChanged();
                saveProfileUpdates();
            } else {
                Toast.makeText(this, "Facility name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Shows a dialog to confirm deletion of a facility.
     * @param position The position of the facility in the list.
     */
    private void showDeleteFacilityDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Facility")
                .setMessage("Are you sure you want to delete this facility?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    facilities.remove(position);
                    adapter.notifyDataSetChanged();
                    saveProfileUpdates();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
