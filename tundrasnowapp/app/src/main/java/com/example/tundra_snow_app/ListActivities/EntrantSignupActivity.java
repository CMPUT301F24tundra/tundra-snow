package com.example.tundra_snow_app.ListActivities;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Helpers.DeviceUtils;

import com.example.tundra_snow_app.Models.Users;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Activity for user registration (Entrant and Organizer).
 */
public class EntrantSignupActivity extends AppCompatActivity{
    private String deviceID;
    private Button createAccountButton, backButton;
    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, dateOfBirthEditText, phoneNumberEditText, editFacilityLocation;
    private ToggleButton notificationToggleButton;

    // Organizer Fields
    private CheckBox organizerCheckbox;
    private LinearLayout facilityLayout;
    private EditText facilityEditText;

    // Database Instances
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    // Profile Picture
    private Uri profilePictureUri;

    /**
     * OnCreate method for EntrantSignupActivity. Initializes UI elements and sets up event listeners.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_signup_activity);
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize device ID
        deviceID = DeviceUtils.getDeviceID(this);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initializing UI elements
        backButton = findViewById(R.id.backButton);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        dateOfBirthEditText = findViewById(R.id.editTextDateOfBirth);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        passwordEditText = findViewById(R.id.editTextPassword);
        notificationToggleButton = findViewById(R.id.toggleButtonNotification);

        organizerCheckbox = findViewById(R.id.checkBoxOrganizer);
        facilityLayout = findViewById(R.id.facilityLayout);
        facilityEditText = findViewById(R.id.editTextFacility);
        editFacilityLocation = findViewById(R.id.editTextFacilityLocation);

        // Toggling visibility of facility input based on organizer selection
        organizerCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                facilityLayout.setVisibility(View.VISIBLE);
            } else {
                facilityLayout.setVisibility(View.GONE);
            }
        });

        // Set up account creation
        createAccountButton = findViewById(R.id.signupButton);
        createAccountButton.setOnClickListener(v -> registerUser());

        // Back button
        backButton.setOnClickListener(v -> finish());
    }

    /**
     * Method to register a new user. Collects user details, validates input fields, and uploads user data to Firestore.
     */
    public void registerUser() {
        Log.d("Debug", "registerUser called");

        // Collecting user details
        String firstName = firstNameEditText.getText().toString();
        String lastName = lastNameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String dateOfBirth = dateOfBirthEditText.getText().toString();
        String phoneNumber = phoneNumberEditText.getText().toString();
        boolean notificationsEnabled = notificationToggleButton.isChecked();

        // Roles and permissions setup
        List<String> roles = new ArrayList<>();
        roles.add("user");

        List<String> facilityList = new ArrayList<>();
        // Additional setup if user selects "organizer"
        if (organizerCheckbox.isChecked()) {
            roles.add("organizer");
            String facilityName = facilityEditText.getText().toString();
            String facilityLocation = editFacilityLocation.getText().toString();
            String facilityID = UUID.randomUUID().toString();

            if (facilityName.isEmpty() || facilityLocation.isEmpty()) {
                Toast.makeText(this, "Please enter facility name and location.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check for existing facility with the same name
            checkAndAddFacility(facilityID, facilityName, facilityLocation);

            // Save facility information to Firestore
            saveFacilityToFirestore(facilityID, facilityName, facilityLocation);
        }

        // Validating input fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generating unique userID (using UUID)
        String userID = UUID.randomUUID().toString();

        // Instantiating Users Class
        Users newUser = new Users(
                userID,
                firstName,
                lastName,
                email,
                password,
                null,
                dateOfBirth,
                phoneNumber,
                notificationsEnabled,
                deviceID,
                null,
                roles,
                facilityList);

        // Upload profile picture if provided
        if (profilePictureUri != null) {
            StorageReference profilePictureRef = storage.getReference("profilePictures/" + userID);
            profilePictureRef.putFile(profilePictureUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                newUser.setProfilePicUrl(uri.toString());
                                saveUserToFirestore(newUser);
                            }))
                    .addOnFailureListener(e -> {
                        Log.e("UserRegistration", "Error uploading profile picture: " + e.getMessage());
                        Toast.makeText(this, "Failed to upload profile picture.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No profile picture; continue
            saveUserToFirestore(newUser);
        }
    }

    /**
     * Method to save user data to Firestore.
     * @param newUser The user object to save
     */
    private void saveUserToFirestore(Users newUser) {
        // Prepare user data for Fire-store
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userID", newUser.getUserID());
        userMap.put("firstName", newUser.getFirstName());
        userMap.put("lastName", newUser.getLastName());
        userMap.put("email", newUser.getEmail());
        userMap.put("password", newUser.getPassword());
        userMap.put("dateOfBirth", newUser.getDateOfBirth());
        userMap.put("phoneNumber", newUser.getPhoneNumber());
        userMap.put("notificationsEnabled", newUser.isNotificationsEnabled());
        userMap.put("deviceID", newUser.getDeviceID());
        userMap.put("roles", newUser.getRoles());
        userMap.put("permissions", newUser.getPermissions());
        userMap.put("userEventList", newUser.getUserEventList());

        if (newUser.getProfilePicUrl() != null) {
            userMap.put("profilePictureUrl", newUser.getProfilePicUrl());
        }

        // Add organizer-specific data
        if (newUser.getRoles().contains("organizer")) {
            userMap.put("facilityList", newUser.getFacilityList());
            userMap.put("organizerEventList", newUser.getOrganizerEventList());
        }

        // Adding user document to Fire-store
        CollectionReference usersRef = db.collection("users");
        usersRef.document(newUser.getUserID())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserRegistration", "User registered successfully with ID: " + newUser.getUserID());
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("UserRegistration", "Error adding user to Fire-store", e);
                    Toast.makeText(this, "Error creating account. Please try again later..", Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Method to check if a facility with the same name already exists in Firestore. If not, adds the facility.
     * @param facilityID The facility ID
     * @param facilityName The facility name
     * @param facilityLocation The facility location
     */
    private void checkAndAddFacility(String facilityID, String facilityName, String facilityLocation) {
        db.collection("facilities")
                .whereEqualTo("facilityName", facilityName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // No facility with the same name exists, proceed to add it
                        saveFacilityToFirestore(facilityID, facilityName, facilityLocation);
                    } else {
                        Log.d("FacilityCheck", "Facility with this name already exists, skipping addition.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FacilityCheck", "Error checking facility existence", e));
    }

    /**
     * Method to save facility data to Firestore.
     * @param facilityID The facility ID
     * @param facilityName The facility name
     * @param facilityLocation The facility location
     */
    private void saveFacilityToFirestore(String facilityID, String facilityName, String facilityLocation) {
        // Create a map for facility data
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("facilityID", facilityID);
        facilityData.put("facilityName", facilityName);
        facilityData.put("facilityLocation", facilityLocation);

        // Add the facility document to Firestore
        db.collection("facilities").document(facilityID)
                .set(facilityData)
                .addOnSuccessListener(aVoid -> Log.d("FacilityRegistration", "Facility saved with ID: " + facilityID))
                .addOnFailureListener(e -> Log.e("FacilityRegistration", "Error saving facility", e));
    }
}
