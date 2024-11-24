package com.example.tundra_snow_app.AdminActivities;

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

import com.example.tundra_snow_app.AdminAdapters.AdminEventAdapter;
import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.example.tundra_snow_app.Helpers.AdminNavbarHelper;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for the admin event view. This class is responsible for displaying
 * the list of events in the RecyclerView.
 */
public class AdminEventViewActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ModePrefs";
    private static final String MODE_KEY = "currentMode";
    private RecyclerView adminEventsRecyclerView;
    private LinearLayout noEventsLayout;
    private AdminEventAdapter adminEventAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;
    private ImageView menuButton;
    private List<String> userRoles = new ArrayList<>();
    private String currentUserID;

    /**
     * Initializes the activity, sets the content view, and sets up the bottom navigation bar.
     * Loads the events from the Firestore database.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_active_events);
        TextView adminTitle = findViewById(R.id.adminTitle);
        adminTitle.setText("Admin: Event View");

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        menuButton = findViewById(R.id.menuButton);
        adminEventsRecyclerView = findViewById(R.id.adminEventsRecyclerView);
        noEventsLayout = findViewById(R.id.noEventsLayout);

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // Retrieve mode from SharedPreferences and set the menu mode accordingly
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String mode = preferences.getString(MODE_KEY, "admin");

        adminEventAdapter = new AdminEventAdapter(this, eventList, position -> {
            eventList.remove(position);
            adminEventAdapter.notifyItemRemoved(position);
            if (eventList.isEmpty()) {
                noEventsLayout.setVisibility(View.VISIBLE);
                adminEventsRecyclerView.setVisibility(View.GONE);
            }
        });

        adminEventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminEventsRecyclerView.setAdapter(adminEventAdapter);

        fetchSessionUserId(() -> setMode(mode));

        // Set up menu button to show the mode menu
        setupMenuButton(preferences);
    }

    /**
     * Setup menu button to display the popup menu for switching modes.
     */
    private void setupMenuButton(SharedPreferences preferences) {
        menuButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(AdminEventViewActivity.this, menuButton);
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

    private void setMode(String mode) {
        Intent intent;
        switch (mode) {
            case "organizer":
                intent = new Intent(AdminEventViewActivity.this, EventViewActivity.class);
                startActivity(intent);
            case "user":
                intent = new Intent(AdminEventViewActivity.this, EventViewActivity.class);
                startActivity(intent);
            default:
                loadEvents();
        }
    }

    /**
     * Loads the events from the Firestore database and updates the RecyclerView.
     */
    private void loadEvents() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Events event = document.toObject(Events.class);
                            eventList.add(event);
                        }
                        adminEventAdapter.notifyDataSetChanged();
                        noEventsLayout.setVisibility(eventList.isEmpty() ? View.VISIBLE : View.GONE);
                        adminEventsRecyclerView.setVisibility(eventList.isEmpty() ? View.GONE : View.VISIBLE);
                    } else {
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("AdminEventViewActivity", "Error loading events", e));
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
