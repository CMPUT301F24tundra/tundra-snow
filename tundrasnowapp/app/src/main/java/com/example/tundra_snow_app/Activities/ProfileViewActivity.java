package com.example.tundra_snow_app.Activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.AdminActivities.AdminEventViewActivity;
import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.example.tundra_snow_app.Helpers.IdenticonGenerator;
import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.ListAdapters.FacilityListAdapter;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for viewing and editing the user's profile information.
 */
public class ProfileViewActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ModePrefs";
    private static final String MODE_KEY = "currentMode";

    private EditText profileName, profileEmail, profilePhone;
    private Button editButton, saveButton, addFacilityButton;
    private ListView facilitiesListView;
    private FacilityListAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String userId;
    private List<String> facilities = new ArrayList<>();
    private List<String> userRoles = new ArrayList<>();

    private ImageView profileImageView;
    private Button changePictureButton;
    private Button removePictureButton;
    private Button generatePictureButton;

    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;

    private ImageView menuButton;
    private View profileSection;
    private View facilitiesSection;
    private boolean isOrganizerMode;
    private TextView profileTitle;

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
        profileSection = findViewById(R.id.profileSection);
        facilitiesSection = findViewById(R.id.facilitiesSection);
        menuButton = findViewById(R.id.menuButton);
        profileImageView = findViewById(R.id.profileImageView);
        changePictureButton = findViewById(R.id.changePictureButton);
        removePictureButton = findViewById(R.id.removePictureButton);
        generatePictureButton = findViewById(R.id.generatePictureButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();// Initialize Firebase instances
        storage = FirebaseStorage.getInstance();

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Fetch user ID from the latest session
        fetchUserIdFromSession();

        // Register the Photo Picker Launcher
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        uploadProfilePictureToFirebase(uri);
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        changePictureButton.setOnClickListener(v -> openPhotoPicker());
        removePictureButton.setOnClickListener(v -> {
            if (userId != null && !userId.isEmpty()) {
                confirmAndRemoveProfilePicture();
            } else {
                Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            }
        });
        generatePictureButton.setOnClickListener(v -> generateIdenticonProfilePicture());
        editButton.setOnClickListener(v -> enableEditing(true));
        saveButton.setOnClickListener(v -> saveProfileUpdates());
        addFacilityButton.setOnClickListener(v -> showAddFacilityDialog());
        setupMenuButton();
    }

    private void generateIdenticonProfilePicture() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate an Identicon using the user ID's hashCode
        int hash = userId.hashCode();
        Bitmap identicon = IdenticonGenerator.generateIdenticon(hash, 256);

        // Save the Bitmap to Firebase
        saveBitmapToFirebase(identicon);
    }

    private void saveBitmapToFirebase(Bitmap bitmap) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = storage.getReference()
                .child("profile_pictures")
                .child(userId + ".jpg");

        // Convert the Bitmap to a ByteArray
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Upload the ByteArray to Firebase Storage
        storageReference.putBytes(data)
                .addOnSuccessListener(taskSnapshot ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            saveProfilePictureUrlToDatabase(downloadUrl);
                            Glide.with(this).load(downloadUrl).into(profileImageView);
                            Toast.makeText(this, "Generated profile picture set successfully.", Toast.LENGTH_SHORT).show();
                        })
                )
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload generated image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }



    /**
     * Confirms and removes the user's profile picture.
     */
    private void confirmAndRemoveProfilePicture() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Profile Picture")
                .setMessage("Are you sure you want to remove your profile picture?")
                .setPositiveButton("Yes", (dialog, which) -> removeProfilePicture())
                .setNegativeButton("No", null)
                .show();
    }

    /**
     * Removes the profile picture from Firebase Storage and Firestore.
     */
    private void removeProfilePicture() {
        // Reference to the profile picture in Firebase Storage
        StorageReference profilePicRef = storage.getReference()
                .child("profile_pictures")
                .child(userId + ".jpg");

        // Delete the picture from Storage
        profilePicRef.delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove the profile picture URL from Firestore
                    db.collection("users").document(userId)
                            .update("profilePictureUrl", null)
                            .addOnSuccessListener(aVoid1 -> {
                                // Reset the profile picture to a default image
                                profileImageView.setImageResource(R.drawable.default_profile_picture);
                                Toast.makeText(this, "Profile picture removed successfully.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update profile picture URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void loadProfilePicture() {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the user's document
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get the profile picture URL
                        String imageUrl = documentSnapshot.getString("profilePictureUrl");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Load the image using Glide
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_profile_picture) // Default placeholder
                                    .into(profileImageView);
                        } else {
                            // Set a default image if no URL is available
                            profileImageView.setImageResource(R.drawable.default_profile_picture);
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }


    private void openPhotoPicker() {
        photoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void uploadProfilePictureToFirebase(Uri imageUri) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageReference = storage.getReference()
                .child("profile_pictures")
                .child(userId + ".jpg");

        storageReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String downloadUrl = uri.toString();
                            saveProfilePictureUrlToDatabase(downloadUrl);
                            profileImageView.setTag(imageUri.toString());
                            Glide.with(this).load(downloadUrl).into(profileImageView);
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfilePictureUrlToDatabase(String imageUrl) {
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to the Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Update the user's document with the profile picture URL
        db.collection("users").document(userId)
                .update("profilePictureUrl", imageUrl)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Profile picture updated successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save profile picture URL.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving profile picture: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }



    /**
     * Sets up the menu button to display role-based options.
     */
    private void setupMenuButton() {
        if (userRoles == null || userRoles.size() <= 1 && userRoles.contains("user")) {
            menuButton.setVisibility(View.GONE);
        } else {
            menuButton.setVisibility(View.VISIBLE);
            menuButton.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(ProfileViewActivity.this, menuButton);
                popupMenu.getMenuInflater().inflate(R.menu.mode_menu, popupMenu.getMenu());

                // Only show the "Organizer" option if the role exists
                if (!userRoles.contains("organizer")) {
                    popupMenu.getMenu().findItem(R.id.menu_organizer).setVisible(false);
                }
                // Only show the "Admin" option if the role exists
                if (!userRoles.contains("admin")) {
                    popupMenu.getMenu().findItem(R.id.menu_admin).setVisible(false);
                }

                popupMenu.setOnMenuItemClickListener(item -> handleMenuSelection(item));
                popupMenu.show();
            });
        }
    }

    /**
     * Handles menu selection and updates the view accordingly.
     * @param item The selected menu item.
     * @return true if handled successfully.
     */
    private boolean handleMenuSelection(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_user) {
            setMode("user");
        } else if (itemId == R.id.menu_organizer) {
            setMode("organizer");
        } else if (itemId == R.id.menu_admin) {
            setMode("admin");
        }
        return true;
    }

    /**
     * Sets the mode based on the selected role.
     * @param mode The selected mode ("user", "organizer", or "admin").
     */
    private void setMode(String mode) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(MODE_KEY, mode).apply();

        switch (mode) {
            case "organizer":
                showOrganizerView();
                break;
            case "admin":
                Intent intent = new Intent(ProfileViewActivity.this, AdminEventViewActivity.class);
                startActivity(intent);
            default:
                showUserView();
                break;
        }
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

        loadProfilePicture();

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

                        // Retrieve roles and enable/disable modeToggle based on roles
                        userRoles = (List<String>) documentSnapshot.get("roles");

                        // Check roles and adjust menu button visibility
                        if (userRoles == null || userRoles.isEmpty() || userRoles.size() == 1 && userRoles.contains("user")) {
                            // Only "user" role is present, hide the menu button
                            menuButton.setVisibility(View.GONE);
                        } else {
                            // Show the menu button if additional roles are available
                            menuButton.setVisibility(View.VISIBLE);
                        }

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

    public void simulateProfileUpload(Uri imageUri) throws InterruptedException {
        fetchUserIdFromSession();
        Thread.sleep(3000);
        uploadProfilePictureToFirebase(imageUri);
    }
}
