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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class AdminImagesViewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AdminImageAdapter adapter;
    private List<String> imageUrls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_image_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        recyclerView = findViewById(R.id.adminImagesRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        imageUrls = new ArrayList<>();
        adapter = new AdminImageAdapter(this, imageUrls);
        recyclerView.setAdapter(adapter);

        fetchImagesFromFirebase();
    }

    private void fetchImagesFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("event_images");

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrls.add(uri.toString());
                    adapter.notifyDataSetChanged();
                });
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to fetch images: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }
}
