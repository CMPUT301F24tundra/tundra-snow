package com.example.tundra_snow_app.EventActivities;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Helpers.DeviceUtils;
import com.example.tundra_snow_app.Models.Events;

import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

/**
 * Activity class for the event detail view. This class is responsible for displaying
 * the details of an event and allowing the user to sign up for the event.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, locationTextView, descriptionTextView, geoLocationTextView;
    private Button signUpButton, backButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID, location;
    private boolean geolocationEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_event_view);

        // Initialize views
        titleTextView = findViewById(R.id.detailEventTitle);
        dateTextView = findViewById(R.id.detailEventDate);
        locationTextView = findViewById(R.id.detailEventLocation);
        descriptionTextView = findViewById(R.id.detailEventDescription);
        geoLocationTextView = findViewById(R.id.geoLocationNotification);
        backButton = findViewById(R.id.backButton);
        signUpButton = findViewById(R.id.buttonSignUpForEvent);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");  // Get event ID from intent

        fetchSessionUser(() -> {
            loadEventDetails();
            backButton.setOnClickListener(view -> finish());
            signUpButton.setOnClickListener(v -> signUpForEvent());
        });
    }

    /**
     * Load event details from Firestore and populate the view with the event data.
     */
    private void loadEventDetails() {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null) {
                            titleTextView.setText(event.getTitle());
                            dateTextView.setText(event.getFormattedDate(event.getStartDate()));  // Use formatted date
                            locationTextView.setText(event.getLocation());
                            descriptionTextView.setText(event.getDescription());

                            String geolocation = documentSnapshot.getString("geolocationRequirement");
                            if (geolocation != null && geolocation.equals("Enabled")) {
                                geoLocationTextView.setVisibility(View.VISIBLE);

                                if (!geolocationEnabled) {
                                    signUpButton.setEnabled(false);
                                    signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#646566")));
                                    Toast.makeText(this, "Geolocation is required. Please enable it in your user settings to sign up", Toast.LENGTH_LONG).show();
                                } else {
                                    signUpButton.setEnabled(true);
                                }
                            } else {
                                geoLocationTextView.setVisibility(View.GONE);
                                signUpButton.setEnabled(true);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Sign up the current user for the event by adding their userId to the event's entrantList.
     */
    private void signUpForEvent() {
        db.collection("events")
                .document(eventID)
                .update("entrantList", FieldValue.arrayUnion(currentUserID))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sign-up failed", Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetch the userId of the current user from the latest session in the "sessions" collection.
     * @param onComplete Runnable to execute after fetching the userId
     */
    private void fetchSessionUser(@NonNull Runnable onComplete) {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        geolocationEnabled = Boolean.TRUE.equals(latestSession.getBoolean("geolocationEnabled"));
                        location = latestSession.getString("location");
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }
}
