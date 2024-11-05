package com.example.tundra_snow_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminUsersViewActivity extends AppCompatActivity {
    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter usersAdapter;
    private List<Users> userList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_users_view);

        BottomNavigationView bottomNavigationView = findViewById(R.id.adminBottomNavigationView);
        AdminNavbarHelper.setupBottomNavigation(this, bottomNavigationView);

        // Initialize RecyclerView and set layout
        usersRecyclerView = findViewById(R.id.adminUsersRecyclerView);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize user list and Firestore
        userList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();

        // Fetch users from Firestore
        fetchUsersFromFirestore();
    }

    private void fetchUsersFromFirestore() {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Map Firestore document to Users object
                        Users user = document.toObject(Users.class);

                        if (user != null) {
                            List<String> roles = user.getRoles();  // Assuming getRoles() retrieves the roles list

                            // Check if roles contains "user" before adding
                            if (roles != null && roles.contains("user")) {
                                userList.add(user);
                            }
                        }
                    }

                    // Initialize adapter and set it on the RecyclerView
                    usersAdapter = new AdminUsersAdapter(this, userList);
                    usersRecyclerView.setAdapter(usersAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminUsersView", "Error fetching users", e);
                    Toast.makeText(this, "Error loading users. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}
