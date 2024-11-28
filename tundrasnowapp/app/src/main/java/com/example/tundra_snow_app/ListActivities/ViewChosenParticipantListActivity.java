package com.example.tundra_snow_app.ListActivities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.ListAdapters.ChosenListAdapter;

import com.example.tundra_snow_app.Models.Notifications;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * Activity to view the list of chosen participants for an event.
 */
public class ViewChosenParticipantListActivity extends AppCompatActivity {

    private RecyclerView participantRecyclerView;
    private FirebaseFirestore db;
    private ChosenListAdapter adapter;
    private String eventID;
    private Button backButton, sendNotificationButton;

    /**
     * Called when the activity is starting. Initializes the activity view and loads the chosen participant list.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chosen_participant_list_view);

        participantRecyclerView = findViewById(R.id.waitListBox);
        participantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.backButton);
        sendNotificationButton = findViewById(R.id.sendNotificationButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadParticipantList();
        backButton.setOnClickListener(view -> finish());

        // Add functionality to send notifications
        sendNotificationButton.setOnClickListener(view -> {
            db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                    String eventName = documentSnapshot.getString("title");

                    if (chosenList != null && !chosenList.isEmpty() && eventName != null) {
                        createNotification(
                                "chosen",
                                chosenList,
                                eventID,
                                eventName,
                                "You have been chosen! Please sign-up for the event!"
                        );
                        Toast.makeText(this, "Notifications sent to chosen participants.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "No chosen participants or event name unavailable.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch event details.", Toast.LENGTH_SHORT).show();
                Log.e("SendNotification", "Error fetching event details: ", e);
            });
        });
    }

    /**
     * Loads the list of chosen participants for the event from Firestore.
     */
    private void loadParticipantList() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("chosenList");

                if (entrantList != null && !entrantList.isEmpty()) {
                    adapter = new ChosenListAdapter(this, entrantList, eventID);
                    participantRecyclerView.setAdapter(adapter);
                }

            }
        });
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

        Notifications notification = new Notifications(
                notificationID,
                userIDs,
                eventID,
                eventName,
                notificationText,
                type
        );

        db.collection("notifications").document(notificationID)
                .set(notification)
                .addOnSuccessListener(aVoid -> Log.d("Notifications", "Notification (" + type + ") created successfully."))
                .addOnFailureListener(e -> Log.e("Notifications", "Failed to create notification (" + type + "): ", e));
    }
}
