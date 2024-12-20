package com.example.tundra_snow_app.EventActivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tundra_snow_app.Models.Events;

import com.example.tundra_snow_app.Models.Notifications;
import com.example.tundra_snow_app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Button leftButton, rightButton, middleButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID, userStatus;
    private ImageView eventImageView;


    /**
     * Called when the activity is created. Initializes the UI components, sets up
     * Firebase Firestore, retrieves the event ID passed via intent, and fetches the
     * current user's session details before loading the event details.
     *
     * @param savedInstanceState The saved instance state containing any previous
     *                           data for this activity, or null if no data is available.
     */
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
        eventImageView = findViewById(R.id.eventImageView);

        middleButton = findViewById(R.id.middleButton);

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

                            // Load image using Glide (or your preferred library)
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrl)
                                        .into(eventImageView);
                            }

                            List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");
                            List<String> cancelledList = (List<String>) documentSnapshot.get("cancelledList");
                            List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                            List<String> confirmedList = (List<String>) documentSnapshot.get("confirmedList");
                            List<String> declinedList = (List<String>) documentSnapshot.get("declinedList");

                            if (entrantList != null && entrantList.contains(currentUserID) && !declinedList.contains(currentUserID)) {
                                userStatus = "entrant";
                                statusMessage.setText("You have successfully signed up! Organizer has not yet sent you an invite.");
                                configureChosenButtons();
                            } else if (chosenList != null && chosenList.contains(currentUserID)) {
                                userStatus = "chosen";
                                statusMessage.setText("You have been invited!");
                                configureChosenButtons();
                            } else if (cancelledList != null && cancelledList.contains(currentUserID)) {
                                userStatus = "cancelled";
                                statusMessage.setText("Event has been cancelled!");
                                configureChosenButtons();
                            } else if (confirmedList != null && confirmedList.contains(currentUserID)) {
                                userStatus = "confirmed";
                                statusMessage.setText("You have confirmed your attendance!");
                                configureChosenButtons();
                            } else if (declinedList != null && entrantList != null && entrantList.contains(currentUserID) && declinedList.contains(currentUserID)) {
                                userStatus = "declined";
                                statusMessage.setText("You were not chosen for this event! If you are rechosen, you will be notified");
                                configureChosenButtons();
                            } else if (declinedList != null && declinedList.contains(currentUserID)) {
                                userStatus = "declined";
                                statusMessage.setText("You have not been chosen from this event! Good luck next time.");
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

            middleButton.setVisibility(View.GONE);

            rightButton.setVisibility(View.VISIBLE);
            rightButton.setText("Cancel");
            rightButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .update("entrantList", FieldValue.arrayRemove(currentUserID),
                                "cancelledList", FieldValue.arrayUnion(currentUserID))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You have cancelled your participation.", Toast.LENGTH_SHORT).show();
                            sendCancelNotification();
                            finishWithResult();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to cancel.", Toast.LENGTH_SHORT).show());
            });
        } else if (userStatus.equals("cancelled") || userStatus.equals("confirmed") || userStatus.equals("declined")) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("Back");
            leftButton.setOnClickListener(v -> finish());

            middleButton.setVisibility(View.GONE);

            rightButton.setVisibility(View.GONE);
        } else if (userStatus.equals("chosen")) {
            leftButton.setVisibility(View.VISIBLE);
            leftButton.setText("Back");
            leftButton.setOnClickListener(v -> finish());

            middleButton.setVisibility(View.VISIBLE);
            middleButton.setText("Decline");
            middleButton.setOnClickListener(v -> {
                db.collection("events").document(eventID)
                        .update("chosenList", FieldValue.arrayRemove(currentUserID),
                                "cancelledList", FieldValue.arrayUnion(currentUserID))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "You have declined your participation.", Toast.LENGTH_SHORT).show();
                            sendCancelNotification();
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
                            Toast.makeText(this, "You have successfully confirmed your participation!", Toast.LENGTH_SHORT).show();
                            sendAcceptNotification();
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
     * Sends a cancellation notification to the user.
     */
    private void sendCancelNotification() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("title");
                if (eventName != null) {
                    createNotification(
                            "cancel",
                            List.of(currentUserID), // Single user being notified
                            eventID,
                            eventName,
                            "You have successfully cancelled your participation in the event."
                    );
                } else {
                    Log.e("Notifications", "Event name is null, cancel notification not sent.");
                }
            } else {
                Log.e("Notifications", "Event document does not exist, cancel notification not sent.");
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Error fetching event details for cancel notification: ", e));
    }

    /**
     * Sends a accept notification to the user.
     */
    private void sendAcceptNotification() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("title");
                if (eventName != null) {
                    createNotification(
                            "accept",
                            List.of(currentUserID), // Single user being notified
                            eventID,
                            eventName,
                            "You have successfully confirmed your participation in the event!"
                    );
                } else {
                    Log.e("Notifications", "Event name is null, accept notification not sent.");
                }
            } else {
                Log.e("Notifications", "Event document does not exist, accept notification not sent.");
            }
        }).addOnFailureListener(e -> Log.e("Notifications", "Error fetching event details for accept notification: ", e));
    }

    /**
     * Finishes the activity with a result.
     */
    private void finishWithResult() {
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    /**
     * Creates a notification document in Firestore.
     *
     * @param type        The type of the notification (e.g., "winner", "loser").
     * @param userIDs     List of user IDs to notify.
     * @param eventID     ID of the associated event.
     * @param eventName   Name of the associated event.
     * @param notificationText The message content for the notification.
     */
    private void createNotification(String type, List<String> userIDs, String eventID, String eventName, String notificationText) {
        String notificationID = db.collection("notifications").document().getId(); // Auto-generate ID

        Log.d("Notifications", "Creating notification with the following details:");
        Log.d("Notifications", "Type: " + type);
        Log.d("Notifications", "Notification ID: " + notificationID);
        Log.d("Notifications", "Event ID: " + eventID);
        Log.d("Notifications", "Event Name: " + eventName);
        Log.d("Notifications", "User IDs: " + userIDs.toString());
        Log.d("Notifications", "Notification Text: " + notificationText);

        // Create a map to track individual user statuses
        Map<String, Boolean> userStatus = new HashMap<>();
        for (String userID : userIDs) {
            userStatus.put(userID, true);
        }

        Notifications notification = new Notifications(
                notificationID,
                userIDs,
                eventID,
                eventName,
                notificationText,
                type,
                userStatus
        );

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("notificationID", notification.getNotificationID());
        notificationData.put("userIDs", notification.getUserIDs());
        notificationData.put("eventID", notification.getEventID());
        notificationData.put("eventName", notification.getEventName());
        notificationData.put("text", notification.getText());
        notificationData.put("notificationType", notification.getNotificationType());
        notificationData.put("timestamp", System.currentTimeMillis());
        notificationData.put("userStatus", notification.getUserStatus());

        db.collection("notifications").document(notificationID)
                .set(notificationData)
                .addOnSuccessListener(aVoid -> Log.d("Notifications", "Notification (" + type + ") created successfully."))
                .addOnFailureListener(e -> Log.e("Notifications", "Failed to create notification (" + type + "): ", e));
    }
}
