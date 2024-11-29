package com.example.tundra_snow_app.AdminActivities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Helpers.NavigationBarHelper;
import com.example.tundra_snow_app.ListAdapters.FacilityListAdapter;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.Models.Organizers;
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
    private Button editButton, saveButton, addFacilityButton;
    private FirebaseFirestore db;
    private String userID;
    private List<String> facilities = new ArrayList<>();
    private List<String> userRoles = new ArrayList<>();

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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        userID = getIntent().getStringExtra("userID");
        fetchUserProfile();
        editButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        addFacilityButton.setVisibility(View.GONE);
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
                            userRoles = user.getRoles();
                            if (!(userRoles == null || userRoles.size() <= 1 && userRoles.contains("user"))) {
                                facilities = user.getFacilityList();
                                if (facilities == null) facilities = new ArrayList<>();
                            }
                        }
                    } else {
                        Toast.makeText(this, "User profile not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

}
