package com.example.tundra_snow_app;

import android.os.Bundle;
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

public class AdminFacilityViewActivity extends AppCompatActivity {

    private RecyclerView facilitiesRecyclerView;
    private AdminFacilityAdapter adminFacilityAdapter;
    private List<Facilities> facilityList;
    private FirebaseFirestore db;

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
