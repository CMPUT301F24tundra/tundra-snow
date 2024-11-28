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
import androidx.cardview.widget.CardView;

import com.example.tundra_snow_app.Helpers.DeviceUtils;
import com.example.tundra_snow_app.Models.Events;

import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Activity class for the event detail view. This class is responsible for displaying
 * the details of an event and allowing the user to sign up for the event.
 */
public class EventDetailActivity extends AppCompatActivity {

    private TextView titleTextView,
            locationTextView,
            descriptionTextView,
            geoLocationTextView,
            startDateTextView,
            endDateTextView,
            regStartDateTextView,
            regEndDateTextView;

    private CardView eventStatus;

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
        locationTextView = findViewById(R.id.detailEventLocation);
        descriptionTextView = findViewById(R.id.detailEventDescription);
        geoLocationTextView = findViewById(R.id.geoLocationNotification);
        backButton = findViewById(R.id.backButton);
        signUpButton = findViewById(R.id.buttonSignUpForEvent);
        eventStatus = findViewById(R.id.detailEventStatus);

        // Date fields initialization
        startDateTextView = findViewById(R.id.detailStartDate);
        endDateTextView = findViewById(R.id.detailEndDate);
        regStartDateTextView = findViewById(R.id.detailRegStartDate);
        regEndDateTextView = findViewById(R.id.detailRegEndDate);

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
                            locationTextView.setText(event.getLocation());
                            descriptionTextView.setText(event.getDescription());

                            // Check if currentUserID is in entrantList
                            List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");
                            List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                            List<String> declinedList = (List<String>) documentSnapshot.get("declinedList");

                            Long capacityLong = documentSnapshot.getLong("capacity");
                            int capacity = (capacityLong != null) ? capacityLong.intValue() : 0;

                            if (entrantList != null && entrantList.contains(currentUserID)) {
                                // Disable the sign-up button if the user is already signed up
                                signUpButton.setEnabled(false);
                                signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#646566")));
                                Toast.makeText(this, "You are already signed up for this event.", Toast.LENGTH_LONG).show();

                            } else if (chosenList != null && chosenList.contains(currentUserID)) {
                                signUpButton.setEnabled(false);
                                signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#646566")));
                                Toast.makeText(this, "You have already been chosen for this event.", Toast.LENGTH_LONG).show();
                            } else if (declinedList != null && declinedList.contains(currentUserID)) {
                                signUpButton.setEnabled(false);
                                signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#646566")));
                                Toast.makeText(this, "You are already signed up for this event.", Toast.LENGTH_LONG).show();
                            } else if (chosenList.size() >= capacity) {
                                signUpButton.setVisibility(View.GONE);
                                eventStatus.setVisibility(View.VISIBLE);
                                Toast.makeText(this, "Event is currently full!",  Toast.LENGTH_LONG).show();
                            } else {
                                // Check geolocation requirements if not already signed up
                                String geolocation = documentSnapshot.getString("geolocationRequirement");
                                if (geolocation != null && geolocation.equals("Enabled")) {
                                    geoLocationTextView.setVisibility(View.VISIBLE);

                                    if (!geolocationEnabled) {
                                        signUpButton.setEnabled(false);
                                        signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#646566")));
                                        Toast.makeText(this, "Geolocation is required. Enable it to sign up.", Toast.LENGTH_LONG).show();
                                    } else {
                                        signUpButton.setEnabled(true);
                                        signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB86FC")));
                                    }
                                } else {
                                    geoLocationTextView.setVisibility(View.GONE);
                                    signUpButton.setEnabled(true);
                                    signUpButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#BB86FC")));
                                }
                            }

                            startDateTextView.setText(event.getFormattedDate(event.getStartDate()));
                            endDateTextView.setText(event.getFormattedDate(event.getEndDate()));
                            regStartDateTextView.setText(event.getFormattedDate(event.getRegistrationStartDate()));
                            regEndDateTextView.setText(event.getFormattedDate(event.getRegistrationEndDate()));
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
