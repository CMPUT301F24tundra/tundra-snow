package com.example.tundra_snow_app.ListActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 

import com.example.tundra_snow_app.ListAdapters.UserListAdapter;
import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewParticipantListActivity extends AppCompatActivity {

    private RecyclerView participantRecyclerView;
    private FirebaseFirestore db;
    private UserListAdapter adapter;
    private String eventID;
    private Button backButton, editButton, saveButton;
    private EditText maxParticipantEdit;

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

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadParticipantList();
        backButton.setOnClickListener(view -> finish());
        editButton.setOnClickListener(view -> enableEditing(true));
        saveButton.setOnClickListener(view -> saveEventUpdates());

    }

    private void loadParticipantList() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("entrantList");

                if (entrantList != null && !entrantList.isEmpty()) {
                    adapter = new UserListAdapter(this, entrantList, eventID);
                    participantRecyclerView.setAdapter(adapter);
                }

                Integer capacity = documentSnapshot.getLong("capacity").intValue();
                maxParticipantEdit.setText(String.valueOf(capacity));
            }
        });
    }

    private void saveEventUpdates() {
        // Convert the text from EditText to an integer for maxParticipants
        int maxParticipantsEdited;
        try {
            maxParticipantsEdited = Integer.parseInt(maxParticipantEdit.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number for max participants.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the user profile in the "users" collection
        db.collection("events").document(eventID)
                .update("capacity", maxParticipantsEdited)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void enableEditing(boolean isEditable) {
        maxParticipantEdit.setEnabled(isEditable);

        // Toggle visibility of buttons
        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
