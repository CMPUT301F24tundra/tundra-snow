package com.example.tundra_snow_app.AdminActivities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.AdminAdapters.AdminQRAdapter;
import com.example.tundra_snow_app.Helpers.AdminNavbarHelper;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminQRViewActivity extends AppCompatActivity {

    private RecyclerView qrRecyclerView;
    private LinearLayout noQRLayout;
    private AdminQRAdapter qrAdapter;
    private List<String> eventTitles;
    private List<String> qrHashes;
    private List<String> eventIds; // Keeps track of corresponding event IDs
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qr_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        // Initialize UI components
        qrRecyclerView = findViewById(R.id.adminQRRecyclerView);
        noQRLayout = findViewById(R.id.noQRLayout);

        // Initialize Firestore and data lists
        db = FirebaseFirestore.getInstance();
        eventTitles = new ArrayList<>();
        qrHashes = new ArrayList<>();
        eventIds = new ArrayList<>();

        // Set up RecyclerView and Adapter
        qrAdapter = new AdminQRAdapter(this, eventTitles, qrHashes, eventIds, this::deleteQRHash);
        qrRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        qrRecyclerView.setAdapter(qrAdapter);

        // Load events with QR hashes
        loadEventsWithQRHashes();
    }

    private void loadEventsWithQRHashes() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventTitles.clear();
                        qrHashes.clear();
                        eventIds.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String qrHash = document.getString("qrHash");
                            String eventId = document.getId();

                            if (title != null && qrHash != null) {
                                eventTitles.add(title);
                                qrHashes.add(qrHash);
                                eventIds.add(eventId);
                            }
                        }
                        qrAdapter.notifyDataSetChanged();
                        toggleNoQRLayout();
                    } else {
                        Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                        Log.e("AdminQRViewActivity", "Error fetching events", task.getException());
                    }
                });
    }

    private void toggleNoQRLayout() {
        if (eventTitles.isEmpty()) {
            noQRLayout.setVisibility(View.VISIBLE);
            qrRecyclerView.setVisibility(View.GONE);
        } else {
            noQRLayout.setVisibility(View.GONE);
            qrRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void deleteQRHash(int position) {
        String eventId = eventIds.get(position);

        db.collection("events").document(eventId).update("qrHash", null)
                .addOnSuccessListener(aVoid -> {
                    // Remove the entry from the lists
                    qrHashes.remove(position);
                    eventTitles.remove(position);
                    eventIds.remove(position);

                    // Notify the adapter about the removed item
                    qrAdapter.notifyItemRemoved(position);
                    qrAdapter.notifyItemRangeChanged(position, qrHashes.size());

                    // Check if RecyclerView is empty and toggle UI
                    toggleNoQRLayout();

                    Toast.makeText(this, "QR Hash removed successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to remove QR Hash.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminQRViewActivity", "Error removing QR Hash", e);
                });
    }
    }
