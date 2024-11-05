package com.example.tundra_snow_app.EventActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.EventAdapters.MyEventsAdapter;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.Helpers.NavigationBarHelper;

import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventViewActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ModePrefs";
    private static final String MODE_KEY = "isOrganizerMode";
    private static final int REQUEST_CODE_UPDATE_STATUS = 1;

    private RecyclerView recyclerViewEvents;
    private LinearLayout noEventsLayout;
    private MyEventsAdapter myEventsAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;
    private ToggleButton modeToggle;
    private TextView eventTitle;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationBarHelper.setupBottomNavigation(this, bottomNavigationView);

        recyclerViewEvents = findViewById(R.id.myEventsRecyclerView);
        noEventsLayout = findViewById(R.id.noEventsLayout);

        // Organizer UI components
        modeToggle = findViewById(R.id.modeToggle);
        eventTitle = findViewById(R.id.myEventTitle);

        // Initialize Firebase and events list
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // Retrieve the toggle state from SharedPreferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isOrganizerMode = preferences.getBoolean(MODE_KEY, false);
        modeToggle.setChecked(isOrganizerMode);

        fetchSessionUserId(() -> {
            // Set up Recycler View
            myEventsAdapter = new MyEventsAdapter(this, eventList, currentUserID, isOrganizerMode, REQUEST_CODE_UPDATE_STATUS);
            recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewEvents.setAdapter(myEventsAdapter);

            setMode(isOrganizerMode);
            modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                setMode(isChecked);
                // Save the mode state in SharedPreferences for persistence across activities
                preferences.edit().putBoolean("isOrganizerMode", isChecked).apply();
            });
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPDATE_STATUS && resultCode == RESULT_OK) {
            // Refresh the adapter to re-bind data and update status
            if (myEventsAdapter != null) {
                myEventsAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setMode(boolean isOrganizerMode) {
        Log.d("MyEventViewActivity", "Setting mode to: " + (isOrganizerMode ? "Organizer" : "User"));

        // Update the adapter with the new mode
        myEventsAdapter.setMode(isOrganizerMode);

        // Load events based on the mode
        if (isOrganizerMode) {
            loadMyOrganizerEventsFromFirestore();
            eventTitle.setText("My Organized Events");
        } else {
            loadMyUserEventFromFirestore();
            eventTitle.setText("My Events");
        }
    }

    private void loadMyUserEventFromFirestore() {
        db.collection("events")
                .whereEqualTo("published", "yes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Events event = document.toObject(Events.class);

                            // Checking  if currentUserID is in the entrantList
                            List<String> entrantList = (List<String>) document.get("entrantList");
                            List<String> declinedList = (List<String>) document.get("declinedList");
                            List<String> cancelledList = (List<String>) document.get("cancelledList");
                            List<String> confirmedList = (List<String>) document.get("confirmedList");
                            List<String> chosenList = (List<String>) document.get("chosenList");

                            if ((entrantList != null && entrantList.contains(currentUserID))
                                    || (declinedList != null && declinedList.contains(currentUserID))
                                    || (cancelledList != null && cancelledList.contains(currentUserID))
                                    || (confirmedList != null && confirmedList.contains(currentUserID))
                                    || (chosenList != null && chosenList.contains(currentUserID))) {
                                eventList.add(event);  // Add to list if user is an entrant
                                Log.d("FirestoreQuery", "Event added: " + event.getTitle());
                            }
                        }

                        // Update UI based on the data
                        if (eventList.isEmpty()) {
                            // No events, show the "No Events" message
                            noEventsLayout.setVisibility(View.VISIBLE);
                            recyclerViewEvents.setVisibility(View.GONE);
                        } else {
                            // Events exist, show the RecyclerView
                            noEventsLayout.setVisibility(View.GONE);
                            recyclerViewEvents.setVisibility(View.VISIBLE);
                            myEventsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Handle error
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadMyOrganizerEventsFromFirestore() {
        Log.d("FirestoreQuery", "Starting to load organizer events with published='no' and organizer=" + currentUserID);

        db.collection("events")
                .whereEqualTo("published", "yes")
                .whereEqualTo("organizer", currentUserID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FirestoreQuery", "Query successful, processing results");
                        eventList.clear();

                        if (task.getResult() != null) {
                            Log.d("FirestoreQuery", "Found " + task.getResult().size() + " events");

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    Events event = document.toObject(Events.class);
                                    Log.d("FirestoreQuery", "Event loaded: " + event.getTitle() + ", ID: " + event.getEventID());
                                    eventList.add(event);
                                } catch (Exception e) {
                                    Log.e("FirestoreQuery", "Error parsing event document: " + document.getId(), e);
                                }
                            }
                        } else {
                            Log.w("FirestoreQuery", "Query result is null");
                        }

                        // Update UI based on data
                        if (eventList.isEmpty()) {
                            Log.d("FirestoreQuery", "No events found, showing 'No Events' layout");
                            noEventsLayout.setVisibility(View.VISIBLE);
                            recyclerViewEvents.setVisibility(View.GONE);
                        } else {
                            Log.d("FirestoreQuery", "Events found, displaying in RecyclerView");
                            noEventsLayout.setVisibility(View.GONE);
                            recyclerViewEvents.setVisibility(View.VISIBLE);
                            myEventsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("FirestoreQuery", "Query failed, task not successful", task.getException());
                        Toast.makeText(this, "Failed to load draft events", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreQuery", "Error executing Firestore query", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }



    // Fetch userId from latest session in "sessions" collection
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

}
