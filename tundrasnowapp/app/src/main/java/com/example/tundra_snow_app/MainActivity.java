package com.example.tundra_snow_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.example.tundra_snow_app.Helpers.DeviceUtils;
import com.example.tundra_snow_app.Activities.EntrantSignupActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main activity for the app. This is the first screen that the user sees when they open the app.
 */
public class MainActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, signInWithDeviceButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String currentUserID;

    /**
     * This method is called when the activity is first created.
     * @param savedInstanceState The saved instance state of the activity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if a user is already logged in
        if (auth.getCurrentUser() != null) {
            startMainContent();
            finish();
            return;
        }

        // Set up input fields and buttons
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signInWithDeviceButton = findViewById(R.id.signInWithDeviceButton);

        // Set up "sign up" link
        setUpSignUpLink();

        // Set up login button
        loginButton.setOnClickListener(v -> loginUser());

        // Set up login with device button
        signInWithDeviceButton.setOnClickListener(v -> loginWithDeviceID());
    }

    /**
     * Sets up the "sign up" link in the main activity. This link allows users to navigate to the
     * EntrantSignUpActivity to create a new account.
     */
    private void setUpSignUpLink() {
        // Setting up the "sign up" button
        TextView signUpText = findViewById(R.id.signUpText);
        String fullText = "Don't have an account? Sign up";
        SpannableString spannableString = new SpannableString(fullText);

        // Making the "Sign up" words clickable
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                // Starting EntrantSignUp Activity
                Intent intent = new Intent(MainActivity.this, EntrantSignupActivity.class);
                startActivity(intent);
            }
        };

        // Applying clickable span to "Sign up"
        int start = fullText.indexOf("Sign up");
        int end = start + "Sign up".length();
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Setting spannable text to TextView
        signUpText.setText(spannableString);
        signUpText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    /**
     * Logs in the user with the provided email and password. If the login is successful, the user
     * is navigated to the EventViewActivity or AdminEventViewActivity depending on if they are an
     * admin or not.
     */
    private void loginUser() {
        String email = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Error checking to see if fields are empty
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticating login
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");

                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Password matches
                                String userID = document.getString("userID");
                                Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_LONG).show();

                                // Log current user in a session
                                logUserSession(document.getId());
                                currentUserID = userID;
                                startMainContent();
                                return;
                            } else {
                                // Password does not match
                                Toast.makeText(MainActivity.this, "Incorrect Password", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No account for this email exists", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Logging error for testing
                    Log.e("Login", "Error logging in", e);
                });
    }

    /**
     * Logs in the user with the device ID. If the login is successful, the user is navigated to the
     * EventViewActivity or AdminEventViewActivity depending on if they are an admin or not.
     */
    private void loginWithDeviceID() {
        String deviceID = DeviceUtils.getDeviceID(this);

        // Querying Firebase for a user with this deviceID
        db.collection("users")
                .whereEqualTo("deviceID", deviceID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Toast.makeText(MainActivity.this, "Device login successful!", Toast.LENGTH_LONG).show();

                            // Log current user in a session
                            logUserSession(document.getId());
                            startMainContent();
                            return;
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "No account for this device exists", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // Logging error for testing
                    Log.e("DeviceLogin", "Error fetching deviceID", e);
                });
    }

    /**
     * Logs the user's session in Firestore. This method is called after a successful login.
     * @param userId The ID of the user that is logging in.
     */
    private void logUserSession(String userId) {
        String sessionId = db.collection("sessions").document().getId();

        // Fetch user information from the "users" collection
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Prepare session data with all user details
                        Map<String, Object> sessionData = new HashMap<>();
                        sessionData.put("userId", userId);
                        sessionData.put("loginTimestamp", System.currentTimeMillis());
                        sessionData.put("sessionId", sessionId);

                        // Add user-specific fields to the session
                        sessionData.put("email", documentSnapshot.getString("email"));
                        sessionData.put("firstName", documentSnapshot.getString("firstName"));
                        sessionData.put("lastName", documentSnapshot.getString("lastName"));
                        sessionData.put("deviceID", documentSnapshot.getString("deviceID"));
                        sessionData.put("roles", documentSnapshot.get("roles")); // Assuming roles is stored as an array
                        sessionData.put("notificationsEnabled", documentSnapshot.getBoolean("notificationsEnabled"));
                        sessionData.put("geolocationEnabled", documentSnapshot.getBoolean("geolocationEnabled"));
                        sessionData.put("location", documentSnapshot.get("location"));
                        sessionData.put("dateOfBirth", documentSnapshot.getString("dateOfBirth"));
                        sessionData.put("organizerEventList", documentSnapshot.get("organizerEventList"));
                        sessionData.put("permissions", documentSnapshot.get("permissions"));
                        sessionData.put("userEventList", documentSnapshot.get("userEventList"));
                        sessionData.put("phoneNumber", documentSnapshot.getString("phoneNumber"));

                        // Add all facilities from the user's facility list
                        List<String> facilityList = (List<String>) documentSnapshot.get("facilityList");
                        if (facilityList != null) {
                            sessionData.put("facilityList", facilityList);
                        } else {
                            sessionData.put("facilityList", new ArrayList<>()); // Default to empty list if null
                        }

                        // Save session data to Firestore
                        db.collection("sessions").document(sessionId)
                                .set(sessionData)
                                .addOnSuccessListener(aVoid -> Log.d("Session", "User session logged with all user data"))
                                .addOnFailureListener(e -> Log.e("Session", "Error logging session data", e));
                    } else {
                        Log.e("Session", "User document not found for session logging.");
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching user data for session", e));
    }
    
    /**
     * Navigates to the EventViewActivity or AdminEventViewActivity depending on if the user is an
     * admin or not.
     */
    private void startMainContent() {
        Intent intent = new Intent(MainActivity.this, EventViewActivity.class); // Regular user view
        intent.putExtra("source", "MainActivity");
        intent.putExtra("userID", currentUserID);
        startActivity(intent);
    }
}
