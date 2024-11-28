package com.example.tundra_snow_app.ListActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.tundra_snow_app.ListAdapters.UserListAdapter;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private FirebaseFirestore db;
    private UserListAdapter adapter;
    private String eventID;
    private Button backButton, editButton, saveButton, regSample;
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

        maxParticipantEdit = findViewById(R.id.maxParticipantEdit);
        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);
        regSample = findViewById(R.id.selectRegSampleButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadParticipantList();
        backButton.setOnClickListener(view -> finish());
        editButton.setOnClickListener(view -> enableEditing(true));
        saveButton.setOnClickListener(view -> saveEventUpdates());
        regSample.setOnClickListener(view -> selectRandomSample());
    }

    /**
     * Loads the list of participants for the event from Firestore.
     */
    private void loadParticipantList() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");

                if (entrantList != null && !entrantList.isEmpty()) {
                    adapter = new UserListAdapter(this, entrantList, eventID);
                    participantRecyclerView.setAdapter(adapter);
                } else {
                    participantRecyclerView.setAdapter(null);
                }

                // Retrieve and display capacity, or "N/A" if not available
                Long capacityLong = documentSnapshot.getLong("capacity");
                if (capacityLong != null) {
                    maxParticipantEdit.setText(String.valueOf(capacityLong.intValue()));
                } else {
                    maxParticipantEdit.setText("N/A");
                }
            }
        });
    }

    /**
     * Saves the updated event details to Firestore.
     */
    private void saveEventUpdates() {
        // Get the text from maxParticipantEdit
        String maxParticipantsText = maxParticipantEdit.getText().toString().trim();
        Integer maxParticipantsEdited = null;

        // Convert the text to an integer
        try {
            // Check if the field is empty
            if (!maxParticipantsText.isEmpty()) {
                maxParticipantsEdited = Integer.parseInt(maxParticipantsText);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number for max participants.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the event capacity in Firestore
        db.collection("events").document(eventID)
                .update("capacity", maxParticipantsEdited)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update event details.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
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
                } else {
                    updates.put("declinedList", FieldValue.arrayUnion(remainingEntrants.toArray()));
                    Toast.makeText(this, "Capacity reached! Remaining entrants moved to declined list.", Toast.LENGTH_SHORT).show();
                }

                db.collection("events").document(eventID)
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            List<String> updatedEntrantList = (List<String>) documentSnapshot.get("entrantList");
                            if (updatedEntrantList == null || updatedEntrantList.isEmpty()) {
                                db.collection("events").document(eventID)
                                        .update("entrantList", new ArrayList<>())
                                        .addOnSuccessListener(aVoid2 -> {
                                            loadParticipantList();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to maintain entrant list.", Toast.LENGTH_SHORT).show();
                                            e.printStackTrace();
                                        });
                            } else {
                                // Reload the participant list directly
                                loadParticipantList();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to select a sample.", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        });
            } else {
                Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching event details.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        });
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
