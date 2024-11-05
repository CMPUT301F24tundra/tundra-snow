package com.example.tundra_snow_app.ListActivities;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// 
import com.example.tundra_snow_app.ListAdapters.ConfirmedListAdapter;

import com.example.tundra_snow_app.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewConfirmedParticipantListActivity extends AppCompatActivity {

    private RecyclerView participantRecyclerView;
    private FirebaseFirestore db;
    private ConfirmedListAdapter adapter;
    private String eventID;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirmed_participant_list_view);

        participantRecyclerView = findViewById(R.id.waitListBox);
        participantRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        backButton = findViewById(R.id.backButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");

        loadParticipantList();
        backButton.setOnClickListener(view -> finish());

    }

    private void loadParticipantList() {
        db.collection("events").document(eventID).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> entrantList = (List<String>) documentSnapshot.get("confirmedList");

                if (entrantList != null && !entrantList.isEmpty()) {
                    adapter = new ConfirmedListAdapter(this, entrantList, eventID);
                    participantRecyclerView.setAdapter(adapter);
                }

            }
        });
    }


}
