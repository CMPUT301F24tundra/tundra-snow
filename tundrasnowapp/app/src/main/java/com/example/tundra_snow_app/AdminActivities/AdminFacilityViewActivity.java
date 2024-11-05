package com.example.tundra_snow_app.AdminActivities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.AdminAdapters.AdminFacilityAdapter;
import com.example.tundra_snow_app.Helpers.AdminNavbarHelper;
import com.example.tundra_snow_app.Models.Facilities;
import com.example.tundra_snow_app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity class for the admin facility view. This class is responsible for displaying
 * the list of facilities in the RecyclerView.
 */
public class AdminFacilityViewActivity extends AppCompatActivity {

    private RecyclerView facilitiesRecyclerView;
    private AdminFacilityAdapter adminFacilityAdapter;
    private List<Facilities> facilityList;
    private FirebaseFirestore db;

    /**
     * Initializes the activity, sets the content view, and sets up the bottom navigation bar.
     * Loads the facilities from the Firestore database.
     * @param savedInstanceState The saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_facility_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        facilitiesRecyclerView = findViewById(R.id.adminFacilitiesRecyclerView);
        facilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        facilityList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        adminFacilityAdapter = new AdminFacilityAdapter(this, facilityList);
        facilitiesRecyclerView.setAdapter(adminFacilityAdapter);

        loadFacilities();
    }

    /**
     * Loads the facilities from the Firestore database and updates the RecyclerView.
     */
    private void loadFacilities() {
        db.collection("facilities")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        facilityList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Facilities facility = document.toObject(Facilities.class);
                            facility.setFacilityID(document.getId());  // Ensure ID is set for deletion
                            facilityList.add(facility);
                        }
                        adminFacilityAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load facilities.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}

