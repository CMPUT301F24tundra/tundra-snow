package com.example.tundra_snow_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminEventViewActivity extends AppCompatActivity {

    private RecyclerView adminEventsRecyclerView;
    private LinearLayout noEventsLayout;
    private AdminEventAdapter adminEventAdapter;
    private List<Events> eventList;
    private FirebaseFirestore db;

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