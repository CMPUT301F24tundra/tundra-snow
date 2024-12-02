package com.example.tundra_snow_app.Activities;


import android.Manifest;
import android.content.pm.PackageManager;
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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tundra_snow_app.Helpers.DeviceUtils;

import com.example.tundra_snow_app.Models.Users;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * EntrantSignupActivity handles the user registration process for entrants and organizers.
 * It includes features such as input validation, geolocation fetching, role-based registration,
 * and integration with Firebase Firestore for storing user data and profile pictures.
 *
 * Features:
 * - User registration with role-specific fields (entrant or organizer).
 * - Location fetching with permission handling.
 * - Profile picture upload to Firebase Storage.
 * - Facility management for organizers.
 *
 * This class extends {@link AppCompatActivity}.
 */
public class EntrantSignupActivity extends AppCompatActivity{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private String deviceID, userLocation;
    private Button createAccountButton, backButton;
    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, dateOfBirthEditText, phoneNumberEditText;
//    private ToggleButton notificationToggleButton, geolocationToggleButton;

    // Organizer Fields
    private CheckBox organizerCheckbox;

    private CheckBox notificationCheckBox, geolocationCheckBox;
    private LinearLayout facilityLayout;
    private EditText facilityEditText;

    // Database Instances
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FusedLocationProviderClient fusedLocationClient;

    // Profile Picture
    private Uri profilePictureUri;

    /**
     * Called when the activity is created. Initializes the UI components, Firebase instances,
     * and sets up event listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the most recent data supplied. Otherwise, null.
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

        // Location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initializing UI elements
        backButton = findViewById(R.id.backButton);
        firstNameEditText = findViewById(R.id.editTextFirstName);
        lastNameEditText = findViewById(R.id.editTextLastName);
        emailEditText = findViewById(R.id.editTextEmail);
        dateOfBirthEditText = findViewById(R.id.editTextDateOfBirth);
        phoneNumberEditText = findViewById(R.id.editTextPhoneNumber);
        passwordEditText = findViewById(R.id.editTextPassword);
//        notificationToggleButton = findViewById(R.id.toggleButtonNotification);
//        geolocationToggleButton = findViewById(R.id.toggleButtonGeolocation);
        notificationCheckBox = findViewById(R.id.checkBoxNotifications);
        geolocationCheckBox = findViewById(R.id.checkBoxGeolocation);
        organizerCheckbox = findViewById(R.id.checkBoxOrganizer);
        facilityLayout = findViewById(R.id.facilityLayout);
        facilityEditText = findViewById(R.id.editTextFacility);


        // Check geolocation settings if toggle is checked
        geolocationCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                boolean isGeolocationEnabled = DeviceUtils.ensureGeolocationEnabled(this);
                if (isGeolocationEnabled) {
                    fetchUserLocation();
                } else {
                    geolocationCheckBox.setChecked(false);
                    userLocation = null;
                }
            }
        });


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
     * Handles the result of permission requests. Specifically checks for location permissions
     * and fetches the user's location if granted.
     *
     * @param requestCode The request code passed in {@link ActivityCompat#requestPermissions}.
     * @param permissions The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, fetch location again
                fetchUserLocation();
            } else {
                // Permissions denied
                Toast.makeText(this, "Location permission denied. Cannot fetch location.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Fetches the user's current location using {@link FusedLocationProviderClient}.
     * If location permissions are not granted, requests the necessary permissions.
     */
    private void fetchUserLocation() {
        // Check if location permissions are granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Permissions are granted; fetch the location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            userLocation = DeviceUtils.getAddressFromLocation(this, location);
                            Log.d("EntrantSignupActivity", "User location fetched: " + userLocation);
                        } else {
                            Log.d("EntrantSignupActivity", "Unable to fetch location.");
                        }
                    })
                    .addOnFailureListener(e -> Log.e("EntrantSignupActivity", "Error fetching location", e));
        } else {
            // Request location permissions
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Registers a new user by collecting input details, validating them, and saving
     * the user data to Firestore. Also handles profile picture uploads if provided.
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
//        boolean notificationsEnabled = notificationToggleButton.isChecked();
//        boolean geolocationEnabled = geolocationToggleButton.isChecked();
        boolean notificationsEnabled = notificationCheckBox.isChecked();
        boolean geolocationEnabled = geolocationCheckBox.isChecked();

        // Roles and permissions setup
        List<String> roles = new ArrayList<>();
        roles.add("user");

        List<String> facilityList = new ArrayList<>();
        // Additional setup if user selects "organizer"
        if (organizerCheckbox.isChecked()) {
            roles.add("organizer");
            String facilityName = facilityEditText.getText().toString();
            String facilityID = UUID.randomUUID().toString();

            if (facilityName.isEmpty()) {
                Toast.makeText(this, "Please enter facility name.", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Debug", "Organizer role selected, facility details provided");

            // Check for existing facility with the same name
            checkAndAddFacility(facilityID, facilityName);
        }

        // Validating input fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (geolocationEnabled && userLocation == null) {
            Toast.makeText(this, "Fetching location... Please wait and try again.", Toast.LENGTH_SHORT).show();
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
                geolocationEnabled,
                deviceID,
                userLocation,
                roles,
                facilityList);

        Log.d("Debug", "User object created");

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
     * Saves the user object to Firestore.
     *
     * @param newUser The {@link Users} object containing user details.
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
        userMap.put("geolocationEnabled", newUser.isGeolocationEnabled());
        userMap.put("deviceID", newUser.getDeviceID());
        userMap.put("location", newUser.getLocation());
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
     * Checks whether a facility with the given name already exists in Firestore.
     * If no such facility exists, it adds the facility.
     *
     * @param facilityID The unique ID of the facility.
     * @param facilityName The name of the facility.
     */
    private void checkAndAddFacility(String facilityID, String facilityName) {
        db.collection("facilities")
                .whereEqualTo("facilityName", facilityName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().isEmpty()) {
                        // No facility with the same name exists, proceed to add it
                        saveFacilityToFirestore(facilityID, facilityName);
                    } else {
                        Log.d("FacilityCheck", "Facility with this name already exists, skipping addition.");
                    }
                })
                .addOnFailureListener(e -> Log.e("FacilityCheck", "Error checking facility existence", e));
    }

    /**
     * Saves a facility to Firestore.
     *
     * @param facilityID The unique ID of the facility.
     * @param facilityName The name of the facility.
     */
    private void saveFacilityToFirestore(String facilityID, String facilityName) {
        // Create a map for facility data
        Map<String, Object> facilityData = new HashMap<>();
        facilityData.put("facilityID", facilityID);
        facilityData.put("facilityName", facilityName);

        // Add the facility document to Firestore
        db.collection("facilities").document(facilityID)
                .set(facilityData)
                .addOnSuccessListener(aVoid -> Log.d("FacilityRegistration", "Facility saved with ID: " + facilityID))
                .addOnFailureListener(e -> Log.e("FacilityRegistration", "Error saving facility", e));
    }
}
