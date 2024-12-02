package com.example.tundra_snow_app.AdminActivities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.AdminAdapters.AdminImageAdapter;
import com.example.tundra_snow_app.Helpers.AdminNavbarHelper;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminImagesViewActivity displays a grid of event-related images for administrators.
 * This activity retrieves images from Firestore and allows admins to view and manage them.
 *
 * Features:
 * - Displays images in a grid layout using a RecyclerView.
 * - Fetches image URLs and their corresponding event IDs from Firestore.
 * - Integrates with a custom adapter to handle image display and interactions.
 * - Includes navigation through the admin-specific bottom navigation bar.
 *
 * This class extends {@link AppCompatActivity}.
 */
public class AdminImagesViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private List<String> imageUrls;
    private Map<String, String> imageUrlToEventIdMap; // Map of image URLs to event IDs


    /**
     * Called when the activity is created. Sets up the UI components, initializes the adapter,
     * and fetches images from Firestore to display in the RecyclerView.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        recyclerView = findViewById(R.id.adminImagesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        imageUrls = new ArrayList<>();
        imageUrlToEventIdMap = new HashMap<>();
        adapter = new AdminImageAdapter(this, imageUrls, imageUrlToEventIdMap);
        recyclerView.setAdapter(adapter);

        fetchImagesFromDatabase();
    }

    /**
     * Fetches event-related images from Firestore and updates the RecyclerView.
     * The method retrieves all documents from the "events" collection, extracts
     * image URLs and their corresponding event IDs, and populates the RecyclerView.
     */
    private void fetchImagesFromDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events") // Adjust to your collection name
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        queryDocumentSnapshots.forEach(document -> {
                            String imageUrl = document.getString("imageUrl");
                            String eventId = document.getId();

                            if (imageUrl != null) {
                                imageUrls.add(imageUrl);
                                imageUrlToEventIdMap.put(imageUrl, eventId);
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                }).addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch images: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
