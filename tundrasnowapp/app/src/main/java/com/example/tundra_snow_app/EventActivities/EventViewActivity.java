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

import com.example.tundra_snow_app.EventAdapters.EventAdapter;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.Helpers.NavigationBarHelper;

import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for the event view. This class is responsible for displaying
 * the list of events.
 */
public class EventViewActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ModePrefs";
    private static final String MODE_KEY = "isOrganizerMode";

    private RecyclerView recyclerViewEvents;
    private LinearLayout noEventsLayout;
    private EventAdapter eventAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;
    private ToggleButton modeToggle;
    private TextView eventTitle;
    private FloatingActionButton addEventButton;
    private String currentUserID;
    private List<String> userRoles = new ArrayList<>();

    /**
     * onCreate method for the activity. Initializes the views and loads the list of events.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ongoing_activities);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        NavigationBarHelper.setupBottomNavigation(this, bottomNavigationView);

        recyclerViewEvents = findViewById(R.id.eventsRecyclerView);
        noEventsLayout = findViewById(R.id.noEventsLayout);

        // Organizer UI components
        modeToggle = findViewById(R.id.modeToggle);
        eventTitle = findViewById(R.id.ongoingEventTitle);
        addEventButton = findViewById(R.id.addEventButton);

        // Initialize Firebase and events list
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // Set up Recycler View
        eventAdapter = new EventAdapter(this, eventList);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);

        // Retrieve mode from SharedPreferences and set the toggle accordingly
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isOrganizerMode = preferences.getBoolean(MODE_KEY, false);
        modeToggle.setChecked(isOrganizerMode);

        // Setting listener for addEventButton
        addEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(EventViewActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        fetchSessionUserId(() -> {
            setMode(isOrganizerMode);

            modeToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Update mode and save state in SharedPreferences
                setMode(isChecked);
                preferences.edit().putBoolean("isOrganizerMode", isChecked).apply();
            });
        });
    }

    /**
     * onResume method for the activity. Reloads the list of events when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (modeToggle.isChecked()) {  // Only reload if in Organizer Mode
            loadOrganizerEventsFromFirestore();
        }
    }

    /**
     * setMode method for the activity. Sets the mode of the activity based 
     * @param isOrganizerMode
     */
    private void setMode(boolean isOrganizerMode) {
        if (isOrganizerMode) {
            loadOrganizerEventsFromFirestore();
            eventTitle.setText("Draft Events View");
            addEventButton.setVisibility(View.VISIBLE);
        } else {
            loadUserEventFromFirestore();
            eventTitle.setText("Ongoing Events View");
            addEventButton.setVisibility(View.GONE);
        }
    }

    /**
     * Loads the list of events from Firestore for a user.
     */
    private void loadUserEventFromFirestore() {
        db.collection("events")
                .whereEqualTo("published", "yes")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Events event = document.toObject(Events.class);
                            eventList.add(event);
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
                            eventAdapter.notifyDataSetChanged();
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

    /**
     * Loads the list of events from Firestore for an organizer.
     */
    private void loadOrganizerEventsFromFirestore() {
        Log.d("FirestoreQuery", "Starting to load organizer events with published='no' and organizer=" + currentUserID);

        db.collection("events")
                .whereEqualTo("published", "no")
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
                            eventAdapter.notifyDataSetChanged();
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
                        // Fetch the user's roles after getting the user ID
                        fetchUserRoles(currentUserID, onComplete);
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }

    /**
     * Fetches the user roles from the user DB in the "users" collection.
     * @param onComplete The callback to execute after fetching the user ID.
     */
    private void fetchUserRoles(String userId, @NonNull Runnable onComplete) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userRoles = (List<String>) documentSnapshot.get("roles");

                        if (userRoles != null && userRoles.contains("organizer")) {
                            modeToggle.setVisibility(View.VISIBLE); // Enable modeToggle if "organizer" role is present
                        }

                        onComplete.run();
                    } else {
                        Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRoles", "Error fetching user roles", e);
                    Toast.makeText(this, "Failed to fetch user roles.", Toast.LENGTH_SHORT).show();
                });
    }

}
