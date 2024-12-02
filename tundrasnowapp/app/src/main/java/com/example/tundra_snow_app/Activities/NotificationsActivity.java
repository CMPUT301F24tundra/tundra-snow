package com.example.tundra_snow_app.Activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tundra_snow_app.ListAdapters.NotificationsListAdapter;
import com.example.tundra_snow_app.Models.Notifications;
import com.example.tundra_snow_app.R;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewNotifications;
    private LinearLayout noNotificationsLayout, notificationAccess;
    private String currentUserID;
    private boolean notiAccess;
    private ImageView backButton;
    private List<Notifications> notificationList;

    private NotificationsListAdapter notificationsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_notifications);

        recyclerViewNotifications = findViewById(R.id.myNotificationsRecyclerView);
        noNotificationsLayout = findViewById(R.id.noNotificationsLayout);
        notificationAccess = findViewById(R.id.notificationAccess);
        backButton = findViewById(R.id.backButton);

        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        backButton.setOnClickListener(view -> finish());

        fetchSessionUserId(() -> loadUserNotificationsFromFirestore());
    }

    /**
     * Reload notifications when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d("NotificationsActivity", "onResume called. Reloading notifications.");
        loadUserNotificationsFromFirestore();
    }

    private void loadUserNotificationsFromFirestore() {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        notificationList.clear();

                        for (DocumentSnapshot document : task.getResult()) {
                            Notifications notification = document.toObject(Notifications.class);

                            if (notification != null && notification.getUserStatus() != null &&
                                    notification.getUserStatus().containsKey(currentUserID)) {
                                notificationList.add(notification);
                            }
                        }

                        if (notificationList.isEmpty()) {
                            noNotificationsLayout.setVisibility(View.VISIBLE);
                            recyclerViewNotifications.setVisibility(View.GONE);
                        } else if (!notiAccess) {
                            notificationAccess.setVisibility(View.VISIBLE);
                            recyclerViewNotifications.setVisibility(View.GONE);
                        } else {
                            noNotificationsLayout.setVisibility(View.GONE);
                            recyclerViewNotifications.setVisibility(View.VISIBLE);

                            notificationsAdapter = new NotificationsListAdapter(this, notificationList, currentUserID);
                            recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
                            recyclerViewNotifications.setAdapter(notificationsAdapter);
                        }
                    } else {
                        Log.e("NotificationsActivity", "Error fetching notifications", task.getException());
                        noNotificationsLayout.setVisibility(View.VISIBLE);
                        recyclerViewNotifications.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationsActivity", "Failed to load notifications", e);
                    Toast.makeText(this, "Failed to load notifications.", Toast.LENGTH_LONG).show();
                });
    }

    private void fetchSessionUserId(@NonNull Runnable onComplete) {
        db.collection("sessions")
                .orderBy("loginTimestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        notiAccess = Boolean.TRUE.equals(latestSession.getBoolean("notificationsEnabled"));

                        if (currentUserID != null) {
                            onComplete.run();
                        } else {
                            Toast.makeText(this, "Failed to fetch user ID.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching session data.", Toast.LENGTH_SHORT).show());
    }
}
