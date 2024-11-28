package com.example.tundra_snow_app.EventActivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
 * the details of an event.
 */
public class MyEventDetailActivity extends AppCompatActivity {

    private TextView titleTextView,
            locationTextView,
            descriptionTextView,
            geoLocationTextView,
            statusMessage,
            startDateTextView,
            endDateTextView,
            regStartDateTextView,
            regEndDateTextView;

    private Button leftButton, rightButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID, userStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_event_detail_view);

        // Initializations
        titleTextView = findViewById(R.id.detailEventTitle);
        locationTextView = findViewById(R.id.detailEventLocation);
        descriptionTextView = findViewById(R.id.detailEventDescription);
        geoLocationTextView = findViewById(R.id.geoLocationNotification);
        statusMessage = findViewById(R.id.statusMessage);
        rightButton = findViewById(R.id.rightButton);
        leftButton = findViewById(R.id.leftButton);

        // Date fields initialization
        startDateTextView = findViewById(R.id.detailStartDate);
        endDateTextView = findViewById(R.id.detailEndDate);
        regStartDateTextView = findViewById(R.id.detailRegStartDate);
        regEndDateTextView = findViewById(R.id.detailRegEndDate);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        fetchSessionUserId(this::loadEventDetails);
    }

    /**
     * Loads the event details from Firestore and populates the view with all event data.
     */
    private void loadEventDetails() {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null) {
                            // Populate title, location, and description
                            titleTextView.setText(event.getTitle());
                            locationTextView.setText(event.getLocation());
                            descriptionTextView.setText(event.getDescription());

                            // Populate geolocation
                            String geolocation = documentSnapshot.getString("geolocationRequirement");
                            if (geolocation != null && geolocation.equals("Enabled")) {
                                geoLocationTextView.setVisibility(View.VISIBLE);
                            } else {
                                geoLocationTextView.setVisibility(View.GONE);
                            }

                            startDateTextView.setText(event.getFormattedDate(event.getStartDate()));
                            endDateTextView.setText(event.getFormattedDate(event.getEndDate()));
                            regStartDateTextView.setText(event.getFormattedDate(event.getRegistrationStartDate()));
                            regEndDateTextView.setText(event.getFormattedDate(event.getRegistrationEndDate()));

                            if (((List<String>) documentSnapshot.get("entrantList")).contains(currentUserID)) {
                                userStatus = "entrant";
                                statusMessage.setText("You have successfully signed up! Organizer has not yet sent you an invite.");
                                configureChosenButtons();
                            } else if (((List<String>) documentSnapshot.get("chosenList")).contains(currentUserID)) {
                                userStatus = "chosen";
                                statusMessage.setText("You have been invited!");
                                configureChosenButtons();
                            } else if (((List<String>) documentSnapshot.get("cancelledList")).contains(currentUserID)) {
                                userStatus = "cancelled";
                                statusMessage.setText("Event has been cancelled!");
                                configureChosenButtons();
                            } else if (((List<String>) documentSnapshot.get("confirmedList")).contains(currentUserID)) {
                                userStatus = "confirmed";
                                statusMessage.setText("You have confirmed your attendance!");
                                configureChosenButtons();
                            } else if (((List<String>) documentSnapshot.get("declinedList")).contains(currentUserID)) {
                                userStatus = "declined";
                                statusMessage.setText("You were not chosen for this event!");
                                configureChosenButtons();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Configures the buttons based on the user's status for the event.
     */
    private void configureChosenButtons() {

        if (userStatus.equals("entrant")) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("Back");
            leftButton.setOnClickListener(v -> finish());

            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Cancel");
            rightButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .update("entrantList", FieldValue.arrayRemove(currentUserID),
                                "cancelledList", FieldValue.arrayUnion(currentUserID))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You have cancelled your participation.", Toast.LENGTH_SHORT).show();
                            finishWithResult();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel.", Toast.LENGTH_SHORT).show());
            });
        } else if (userStatus.equals("cancelled") || userStatus.equals("confirmed") || userStatus.equals("declined")) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("Back");
            leftButton.setOnClickListener(v -> finish());

            rightButton.setVisibility(View.GONE);
        } else if (userStatus.equals("chosen")) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("Decline");
            leftButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .update("chosenList", FieldValue.arrayRemove(currentUserID),
                                "declinedList", FieldValue.arrayUnion(currentUserID))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You have declined your participation.", Toast.LENGTH_SHORT).show();
                            finishWithResult();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel.", Toast.LENGTH_SHORT).show());
            });

            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Accept");
            rightButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .update("chosenList", FieldValue.arrayRemove(currentUserID),
                                "confirmedList", FieldValue.arrayUnion(currentUserID))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You have cancelled your participation.", Toast.LENGTH_SHORT).show();
                            finishWithResult();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel.", Toast.LENGTH_SHORT).show());
            });
        }
    }

    /**
     * Fetches the user ID from the latest session in the "sessions" collection.
     * @param onComplete The callback to execute after fetching the user ID.
     */
    private void fetchSessionUserId(@NonNull Runnable onComplete) {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }

    /**
     * Finishes the activity with a result.
     */
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
