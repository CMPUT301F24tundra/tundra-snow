package com.example.tundra_snow_app.AdminActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.AdminAdapters.AdminEventAdapter;
import com.example.tundra_snow_app.Helpers.AdminNavbarHelper;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for the admin event view. This class is responsible for displaying
 * the list of events in the RecyclerView.
 */
public class AdminEventViewActivity extends AppCompatActivity {

    private RecyclerView adminEventsRecyclerView;
    private LinearLayout noEventsLayout;
    private AdminEventAdapter adminEventAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets the content view, and sets up the bottom navigation bar.
     * Loads the events from the Firestore database.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_active_events);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        adminEventsRecyclerView = findViewById(R.id.adminEventsRecyclerView);
        noEventsLayout = findViewById(R.id.noEventsLayout);

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

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

        loadEvents();
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
}
