package com.example.tundra_snow_app.ListActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tundra_snow_app.ListAdapters.UserListAdapter;
import com.example.tundra_snow_app.Models.Notifications;
import com.example.tundra_snow_app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity to view the list of participants for an event.
 */
public class ViewParticipantListActivity extends AppCompatActivity {

    private RecyclerView participantRecyclerView;
    private LinearLayout regSampleLayout, regReplaceLayout;
    private FirebaseFirestore db;
    private UserListAdapter adapter;
    private String eventID;
    private Button backButton, editButton, saveButton, regSample, repSample;
    private EditText maxParticipantEdit;

    /**
     * Called when the activity is starting. Initializes the activity view and loads the participant list.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_list_view);

        participantRecyclerView = findViewById(R.id.waitListBox);
        participantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        regSampleLayout = findViewById(R.id.regSampleLayout);
        regReplaceLayout = findViewById(R.id.regReplaceLayout);

        maxParticipantEdit = findViewById(R.id.maxParticipantEdit);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        regSample = findViewById(R.id.selectRegSampleButton);
        repSample = findViewById(R.id.selectReplaceButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadParticipantList();
        backButton.setOnClickListener(view -> finish());
        editButton.setOnClickListener(view -> enableEditing(true));
        saveButton.setOnClickListener(view -> {
            saveEventUpdates();
            loadParticipantList();
        });
        regSample.setOnClickListener(view -> selectRandomSample());
        repSample.setOnClickListener(view -> selectReplacementSample());
    }

    private void loadParticipantList() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Retrieve lists from Firestore
                List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");
                List<String> cancelledList = (List<String>) documentSnapshot.get("cancelledList");
                List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                List<String> confirmedList = (List<String>) documentSnapshot.get("confirmedList");
                List<String> declinedList = (List<String>) documentSnapshot.get("declinedList");

                // Initialize lists to avoid null pointer issues
                entrantList = (entrantList == null) ? new ArrayList<>() : entrantList;
                cancelledList = (cancelledList == null) ? new ArrayList<>() : cancelledList;
                chosenList = (chosenList == null) ? new ArrayList<>() : chosenList;
                confirmedList = (confirmedList == null) ? new ArrayList<>() : confirmedList;
                declinedList = (declinedList == null) ? new ArrayList<>() : declinedList;

                Map<String, Object> updates = new HashMap<>();

                // Retrieve and display capacity, or "N/A" if not available
                Long capacityLong = documentSnapshot.getLong("capacity");
                if (capacityLong != null) {
                    maxParticipantEdit.setText(String.valueOf(capacityLong.intValue()));
                    int capacity = capacityLong.intValue();

                    // Handle full capacity: Move all entrants to declinedList
                    if (confirmedList.size() >= capacity) {
                        if (!entrantList.isEmpty()) {
                            updates.put("declinedList", FieldValue.arrayUnion(entrantList.toArray()));
                            updates.put("entrantList", FieldValue.arrayRemove(entrantList.toArray()));
                        }

                        if (chosenList != null && !chosenList.isEmpty()) {
                            // Move chosen members to declinedList
                            updates.put("declinedList", FieldValue.arrayUnion(chosenList.toArray()));
                            updates.put("chosenList", FieldValue.arrayRemove(chosenList.toArray()));
                        }

                        Toast.makeText(this, "Event is at full capacity!", Toast.LENGTH_LONG).show();
                        regSampleLayout.setVisibility(View.GONE);
                        regReplaceLayout.setVisibility(View.GONE);
                    } else if (chosenList.size() >= capacity) {
                        if (!entrantList.isEmpty()) {
                            updates.put("declinedList", FieldValue.arrayUnion(entrantList.toArray()));
                        }

                        Toast.makeText(this, "Max participants have been selected. Sampling will enable if cancellations occur.", Toast.LENGTH_LONG).show();
                        regSampleLayout.setVisibility(View.GONE);
                        regReplaceLayout.setVisibility(View.GONE);
                    } else if (!cancelledList.isEmpty() && chosenList.size() < capacity) {
                        if (!entrantList.isEmpty()) {
                            updates.put("declinedList", FieldValue.arrayRemove(entrantList.toArray()));
                        } else if (!declinedList.isEmpty()) {
                            updates.put("entrantList", FieldValue.arrayUnion(declinedList.toArray()));
                            updates.put("declinedList", FieldValue.arrayRemove(declinedList.toArray()));
                        }

                        Toast.makeText(this, "There have been cancellations. Please select a replacement sample.", Toast.LENGTH_LONG).show();
                        regSampleLayout.setVisibility(View.GONE);
                        regReplaceLayout.setVisibility(View.VISIBLE);
                    } else if (chosenList.size() < capacity) {
                        if (!entrantList.isEmpty()) {
                            updates.put("declinedList", FieldValue.arrayRemove(entrantList.toArray()));
                        } else if (!declinedList.isEmpty()) {
                            updates.put("entrantList", FieldValue.arrayUnion(declinedList.toArray()));
                            updates.put("declinedList", FieldValue.arrayRemove(declinedList.toArray()));
                        }

                        regSampleLayout.setVisibility(View.VISIBLE);
                        regReplaceLayout.setVisibility(View.GONE);
                    }
                } else {
                    if (!entrantList.isEmpty()) {
                        updates.put("declinedList", FieldValue.arrayRemove(entrantList.toArray()));
                    } else {
                        updates.put("declinedList", FieldValue.arrayRemove(declinedList.toArray()));
                        updates.put("entrantList", FieldValue.arrayUnion(declinedList.toArray()));
                    }
                    maxParticipantEdit.setText("N/A");
                    regSampleLayout.setVisibility(View.VISIBLE);
                    regReplaceLayout.setVisibility(View.GONE);
                }

                // Apply updates to Firestore
                handleUpdates(updates);
            } else {
                Log.e("LoadParticipants", "Event document does not exist.");
            }
        }).addOnFailureListener(e -> {
            Log.e("LoadParticipants", "Failed to load event details: ", e);
            Toast.makeText(this, "Error loading participants.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateRecyclerView(List<String> entrantList) {
        if (entrantList != null && !entrantList.isEmpty()) {
            if (adapter == null) {
                // Initialize the adapter for the first time
                adapter = new UserListAdapter(this, entrantList, eventID);
                participantRecyclerView.setAdapter(adapter);
            } else {
                // Update the adapter's data and refresh
                adapter.updateData(entrantList);
                adapter.notifyDataSetChanged();
            }
            participantRecyclerView.setVisibility(View.VISIBLE);
        } else {
            // Clear the RecyclerView if the list is empty
            if (adapter != null) {
                adapter.updateData(new ArrayList<>());
                adapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Helper function for handling database updates and refreshing the UI.
     */
    private void handleUpdates(Map<String, Object> updates) {
        if (updates.isEmpty()) {
            Log.d("LoadParticipants", "No updates to apply.");
            return;
        }

        // Apply updates to Firestore
        db.collection("events").document(eventID)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoadParticipants", "Event details updated successfully.");

                    // Fetch the updated document to refresh the RecyclerView
                    db.collection("events").document(eventID).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");

                                    if (entrantList != null) {
                                        updateRecyclerView(entrantList);
                                    } else {
                                        Log.d("LoadParticipants", "Entrant list is empty after update.");
                                        updateRecyclerView(new ArrayList<>());
                                    }
                                } else {
                                    Log.e("LoadParticipants", "Event document no longer exists.");
                                    Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("LoadParticipants", "Error fetching updated document: ", e);
                                Toast.makeText(this, "Error fetching updated event details.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("LoadParticipants", "Error updating Firestore: ", e);
                    Toast.makeText(this, "Failed to update event details.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Saves the updated event details to Firestore.
     */
    private void saveEventUpdates() {
        // Get the text from maxParticipantEdit
        String maxParticipantsText = maxParticipantEdit.getText().toString().trim();

        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                        List<String> confirmedList = (List<String>) documentSnapshot.get("confirmedList");
                        int chosenListSize = chosenList != null ? chosenList.size() : 0;
                        int confirmedListSize = confirmedList != null ? confirmedList.size() : 0;

                        Integer maxParticipantsEdited = null;
                        try {
                            // Allow empty capacity (no limit)
                            if (!maxParticipantsText.isEmpty()) {
                                maxParticipantsEdited = Integer.parseInt(maxParticipantsText);

                                // Validate capacity with chosenList size
                                while (maxParticipantsEdited < chosenListSize || maxParticipantsEdited < confirmedListSize) {
                                    Toast.makeText(this,
                                            "Invalid capacity. " + chosenListSize + " participants have already been chosen. Please enter a capacity larger than or equal to " + chosenListSize,
                                            Toast.LENGTH_LONG
                                    ).show();
                                    return; // Prevent update until the condition is satisfied
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid number for max participants.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Update Firestore with the new capacity
                        db.collection("events").document(eventID)
                                .update("capacity", maxParticipantsEdited) // Can be null for unlimited capacity
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                                    enableEditing(false);
                                    loadParticipantList();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to update event details.", Toast.LENGTH_LONG).show();
                                    Log.e("SaveEventUpdates", "Error updating Firestore: ", e);
                                });
                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching event details.", Toast.LENGTH_SHORT).show();
                    Log.e("SaveEventUpdates", "Error fetching Firestore document: ", e);
                });
    }

    /**
     * Selects a random sample of up to 5 participants from the entrantList and moves them to the chosenList.
     */
    private void selectRandomSample() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");

                if (entrantList == null || entrantList.isEmpty()) {
                    Toast.makeText(this, "No participants in the entrant list.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Long capacityLong = documentSnapshot.getLong("capacity");
                int capacity;
                if (capacityLong != null) {
                    capacity = capacityLong.intValue();
                    Toast.makeText(this, "Sample size is set to max event capacity..", Toast.LENGTH_SHORT).show();
                } else {
                    capacity = 5;
                    Toast.makeText(this, "Event capacity is not set, therefore sampling is set to 5 people by default..", Toast.LENGTH_SHORT).show();
                }


                Collections.shuffle(entrantList);
                int sampleSize = Math.min(entrantList.size(), capacity); // Up to 5 for random sample
                List<String> chosenSample = new ArrayList<>(entrantList.subList(0, sampleSize));
                List<String> remainingEntrants = new ArrayList<>(entrantList.subList(sampleSize, entrantList.size()));

                Map<String, Object> updates = new HashMap<>();
                updates.put("entrantList", FieldValue.arrayRemove(chosenSample.toArray()));
                updates.put("chosenList", FieldValue.arrayUnion(chosenSample.toArray()));

                if (entrantList.size() <= capacity) {
                    Toast.makeText(this, "All participants have been selected.", Toast.LENGTH_SHORT).show();
                }

                db.collection("events").document(eventID)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            loadParticipantList(); // Refresh the UI

                            String eventName = documentSnapshot.getString("title");
                            if (eventName != null) {
                                // Send notifications for winners and losers
                                createNotification("winner", chosenSample, eventID, eventName,
                                        "Congratulations! You have been selected for the event. Please sign-up to confirm attendance.");

                                if (!remainingEntrants.isEmpty()) {
                                    createNotification("loser", remainingEntrants, eventID, eventName,
                                            "Unfortunately, you were not selected for the event. Thank you for participating.");
                                }
                            } else {
                                Log.e("selectRandomSample", "Event name is null, notifications not created.");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update the event details.", Toast.LENGTH_SHORT).show();
                            Log.e("selectRandomSample", "Error updating Firestore: ", e);
                        });
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                Log.w("selectRandomSample", "No document found for eventID: " + eventID);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching event details.", Toast.LENGTH_SHORT).show();
            Log.e("selectRandomSample", "Error fetching Firestore document: ", e);
        });
    }

    private void selectReplacementSample() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");
                List<String> chosenList = (List<String>) documentSnapshot.get("chosenList");
                List<String> confirmedList = (List<String>) documentSnapshot.get("confirmedList");

                if (entrantList == null || entrantList.isEmpty()) {
                    Toast.makeText(this, "No participants available in the entrant list for replacement.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (chosenList == null) {
                    chosenList = new ArrayList<>(); // Handle null case
                }

                Long capacityLong = documentSnapshot.getLong("capacity");
                if (capacityLong == null) {
                    Toast.makeText(this, "Event capacity is not set. Cannot proceed with replacements.", Toast.LENGTH_SHORT).show();
                    Log.e("selectReplacementSample", "Event capacity is null.");
                    return;
                }

                int capacity = capacityLong.intValue();
                int remainingSlots = capacity - chosenList.size();

                if (remainingSlots <= 0) {
                    Toast.makeText(this, "No replacements are needed as the event is already at capacity.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (entrantList.size() < remainingSlots) {
                    Toast.makeText(this, "Not enough participants in the entrant list to fill the remaining slots.", Toast.LENGTH_SHORT).show();
                    remainingSlots = entrantList.size(); // Adjust sample size to available entrants
                }

                Collections.shuffle(entrantList); // Randomize the entrant list
                List<String> replacementSample = new ArrayList<>(entrantList.subList(0, remainingSlots));
                List<String> remainingEntrants = new ArrayList<>(entrantList.subList(remainingSlots, entrantList.size()));

                Map<String, Object> updates = new HashMap<>();
                updates.put("entrantList", FieldValue.arrayRemove(replacementSample.toArray()));
                updates.put("chosenList", FieldValue.arrayUnion(replacementSample.toArray()));

                db.collection("events").document(eventID)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            loadParticipantList(); // Refresh the UI

                            String eventName = documentSnapshot.getString("title");
                            if (eventName != null) {
                                createNotification("replacement", replacementSample, eventID, eventName,
                                        "You have been chosen as a replacement participant for the event. Please confirm your attendance.");
                            } else {
                                Log.e("selectReplacementSample", "Event name is null, replacement notifications not created.");
                            }
                            Toast.makeText(this, "Replacement sample selected and updated successfully.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update event details for replacements.", Toast.LENGTH_SHORT).show();
                            Log.e("selectReplacementSample", "Error updating Firestore: ", e);
                        });
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                Log.w("selectReplacementSample", "No document found for eventID: " + eventID);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching event details.", Toast.LENGTH_SHORT).show();
            Log.e("selectReplacementSample", "Error fetching Firestore document: ", e);
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

    /**
     * Enables or disables editing of the event details.
     * @param isEditable True if editing should be enabled, false otherwise
     */
    private void enableEditing(boolean isEditable) {
        maxParticipantEdit.setEnabled(isEditable);

        // Toggle visibility of buttons
        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
