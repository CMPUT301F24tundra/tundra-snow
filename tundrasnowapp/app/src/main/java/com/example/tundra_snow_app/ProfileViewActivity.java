package com.example.tundra_snow_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ProfileViewActivity extends AppCompatActivity {

    private EditText profileName, profileEmail, profilePhone;
    private Button editButton, saveButton;
    private FirebaseFirestore db;
    private String userId;  // Variable to store the user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_view);

        // Initialize UI elements
        profileName = findViewById(R.id.profileName);
        profileEmail = findViewById(R.id.profileEmail);
        profilePhone = findViewById(R.id.profilePhone);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Fetch user ID from the latest session
        fetchUserIdFromSession();

        // Set up button listeners
        editButton.setOnClickListener(v -> enableEditing(true));
        saveButton.setOnClickListener(v -> saveProfileUpdates());
    }

    private void fetchUserIdFromSession() {
        CollectionReference sessionsRef = db.collection("sessions");

        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        userId = latestSession.getString("userId");  // Retrieve the userId from the session

                        // Now fetch the actual user information from "users"
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
        if (userId == null) return;  // Ensure userId is set

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate UI with user data
                        String firstName = documentSnapshot.getString("firstName") != null ? documentSnapshot.getString("firstName") : "";
                        String lastName = documentSnapshot.getString("lastName") != null ? documentSnapshot.getString("lastName") : "";
                        profileName.setText(getString(R.string.profile_name_format, firstName, lastName));
                        profileEmail.setText(documentSnapshot.getString("email") != null ? documentSnapshot.getString("email") : getString(R.string.default_email));
                        profilePhone.setText(documentSnapshot.getString("phoneNumber") != null ? documentSnapshot.getString("phoneNumber") : getString(R.string.default_phone));
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

        // Toggle visibility of buttons
        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    private void saveProfileUpdates() {
        String[] nameParts = profileName.getText().toString().split(" ");
        String firstName = nameParts.length > 0 ? nameParts[0] : "";
        String lastName = nameParts.length > 1 ? nameParts[1] : "";
        String email = profileEmail.getText().toString();
        String phone = profilePhone.getText().toString();

        // Update the user profile in the "users" collection
        db.collection("users").document(userId)
                .update("firstName", firstName,
                        "lastName", lastName,
                        "email", email,
                        "phoneNumber", phone)
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
