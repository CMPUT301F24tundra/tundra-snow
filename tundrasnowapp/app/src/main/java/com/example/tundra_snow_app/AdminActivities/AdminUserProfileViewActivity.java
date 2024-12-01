package com.example.tundra_snow_app.AdminActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.Models.Users;
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
public class AdminUserProfileViewActivity extends AppCompatActivity {


    private EditText profileName, profileEmail, profilePhone;
    private Button editButton;
    private FirebaseFirestore db;
    ImageView backButton, profileImageView, menuButton;
    TextView profilePicOptions;
    LinearLayout profilePicButtons;
    BottomNavigationView bottomNavigationView;
    private String userID;

    /**
     * Initializes the activity and sets up the UI elements.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);

        // Initialize UI elements
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        editButton = findViewById(R.id.editButton);
        backButton = findViewById(R.id.backButton); // Fixed initialization
        menuButton = findViewById(R.id.menuButton); // Fixed initialization
        profilePicOptions = findViewById(R.id.profilePicOptions);
        profilePicButtons = findViewById(R.id.profilePicButtons);
        profileImageView = findViewById(R.id.profileImageView);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Adjust visibility
        if (backButton != null) backButton.setVisibility(View.VISIBLE);
        if (menuButton != null) menuButton.setVisibility(View.GONE);
        if (editButton != null) editButton.setVisibility(View.GONE);
        if (profilePicOptions != null) profilePicOptions.setVisibility(View.GONE);
        if (profilePicButtons != null) profilePicButtons.setVisibility(View.GONE);
        if (bottomNavigationView != null) bottomNavigationView.setVisibility(View.GONE);

        // Back button listener
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        userID = getIntent().getStringExtra("userID");

        // Fetch user data
        fetchUserProfile();
        loadProfilePicture();
    }

    /**
     * Fetches the user's profile information from Firestore.
     */
    private void fetchUserProfile() {
        db.collection("users").document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Users user = documentSnapshot.toObject(Users.class);
                        if (user != null) {
                            String firstName = user.getFirstName();
                            String lastName = user.getLastName();
                            profileName.setText(getString(R.string.profile_name_format, firstName, lastName));
                            profileEmail.setText(user.getEmail());
                            profilePhone.setText(user.getPhoneNumber());
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    private void loadProfilePicture() {
        if (userID == null || userID.isEmpty()) {
            Toast.makeText(this, "User ID is not set", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Get the user's document
        db.collection("users").document(userID)
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
}
