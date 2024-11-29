package com.example.tundra_snow_app.EventActivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.Activities.NotificationsActivity;
import com.example.tundra_snow_app.AdminActivities.AdminEventViewActivity;
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
    private static final String MODE_KEY = "currentMode";

    private RecyclerView recyclerViewEvents;
    private LinearLayout noEventsLayout;
    private EventAdapter eventAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;
    private TextView eventTitle;
    private FloatingActionButton addEventButton;
    private String currentUserID;
    private List<String> userRoles = new ArrayList<>();
    private ImageView menuButton, notificationButton;
    private boolean isOrganizerMode;

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
        notificationButton = findViewById(R.id.notificationButton);
        menuButton = findViewById(R.id.menuButton);
        eventTitle = findViewById(R.id.ongoingEventTitle);
        addEventButton = findViewById(R.id.addEventButton);

        // Initialize Firebase and events list
        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // Set up Recycler View
        eventAdapter = new EventAdapter(this, eventList);
        recyclerViewEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEvents.setAdapter(eventAdapter);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Setting listener for addEventButton
        addEventButton.setOnClickListener(view -> {
            Intent intent = new Intent(EventViewActivity.this, CreateEventActivity.class);
            startActivity(intent);
        });

        // Setting listener for notification button
        notificationButton.setOnClickListener(view -> {
            Log.d("EventViewActivity", "Notification button clicked. Starting NotificationsActivity...");

            Intent intent = new Intent(EventViewActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });

        try {
            String mode = preferences.getString(MODE_KEY, "user");  // Attempt to retrieve it as a string
            fetchSessionUserId(() -> setMode(mode));
        } catch (ClassCastException e) {
            // If stored as a boolean, handle and reset it as a string
            boolean modeAsBoolean = preferences.getBoolean(MODE_KEY, false);
            String mode = modeAsBoolean ? "organizer" : "user";  // Convert boolean to appropriate string
            fetchSessionUserId(() -> setMode(mode));
            preferences.edit().putString(MODE_KEY, mode).apply();  // Store as string
        }

        // Set up menu button to show the mode menu
        setupMenuButton(preferences);
    }

    /**
     * Setup menu button to display the popup menu for switching modes.
     */
    private void setupMenuButton(SharedPreferences preferences) {
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(EventViewActivity.this, menuButton);
            popupMenu.getMenuInflater().inflate(R.menu.mode_menu, popupMenu.getMenu());

            // Hide the admin menu item if the user does not have "admin" role
            if (userRoles != null && !userRoles.contains("admin")) {
                popupMenu.getMenu().findItem(R.id.menu_admin).setVisible(false);
            }

            popupMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.menu_user) {
                    setMode("user");
                    preferences.edit().putString(MODE_KEY, "user").apply();
                    return true;
                } else if (itemId == R.id.menu_organizer) {
                    if (userRoles.contains("organizer")) {
                        setMode("organizer");
                        preferences.edit().putString(MODE_KEY, "organizer").apply();
                    } else {
                        Toast.makeText(this, "You do not have organizer permissions.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } else if (itemId == R.id.menu_admin) {
                    if (userRoles.contains("admin")) {
                        setMode("admin");
                        preferences.edit().putString(MODE_KEY, "admin").apply();
                    } else {
                        Toast.makeText(this, "You do not have admin permissions.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            });

            popupMenu.show();
        });
    }

    /**
     * onResume method for the activity if in organizer mode. Reloads the list of events when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isOrganizerMode()) {
            loadOrganizerEventsFromFirestore();
        } else {
            loadUserEventFromFirestore();
        }
    }

    /**
     * Check if the current mode is organizer mode.
     */
    private boolean isOrganizerMode() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = preferences.getString(MODE_KEY, "user");
        return "organizer".equals(mode);
    }

    /**
     * setMode method for the activity. Sets the mode of the activity based 
     * @param mode The selected mode.
     */
    private void setMode(String mode) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        preferences.edit().putString(MODE_KEY, mode).apply();

        switch (mode) {
            case "organizer":
                loadOrganizerEventsFromFirestore();
                eventTitle.setText("Draft Events View");
                addEventButton.setVisibility(View.VISIBLE);
                notificationButton.setVisibility(View.GONE);
                break;
            case "admin":
                Intent intent = new Intent(EventViewActivity.this, AdminEventViewActivity.class); // Regular user view
                startActivity(intent);
            default:
                loadUserEventFromFirestore();
                eventTitle.setText("Events View");
                addEventButton.setVisibility(View.GONE);
                notificationButton.setVisibility(View.VISIBLE);
                break;
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

                                // Check if the currentUserID is not in these lists
                                List<String> entrantList = (List<String>) document.get("entrantList");
                                List<String> chosenList = (List<String>) document.get("chosenList");
                                List<String> declinedList = (List<String>) document.get("declinedList");

                                if ((entrantList == null || !entrantList.contains(currentUserID)) &&
                                        (chosenList == null || !chosenList.contains(currentUserID)) &&
                                        (declinedList == null || !declinedList.contains(currentUserID))) {
                                    eventList.add(event); // Add the event if user is not in the lists
                                } else {
                                    Log.d("EventFilter", "User is already in entrant list for event: " + event.getTitle());
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
         * Loads the list of draft events from Firestore for an organizer.
         */
        private void loadOrganizerEventsFromFirestore () {
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
        private void fetchSessionUserId (@NonNull Runnable onComplete){
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
        private void fetchUserRoles (String userId, @NonNull Runnable onComplete){
            db.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            userRoles = (List<String>) documentSnapshot.get("roles");

                            // Check roles and adjust menu button visibility
                            if (userRoles == null || userRoles.isEmpty() || userRoles.size() == 1 && userRoles.contains("user")) {
                                // Only "user" role is present, hide the menu button
                                menuButton.setVisibility(View.GONE);
                            } else {
                                // Show the menu button if additional roles are available
                                menuButton.setVisibility(View.VISIBLE);
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
