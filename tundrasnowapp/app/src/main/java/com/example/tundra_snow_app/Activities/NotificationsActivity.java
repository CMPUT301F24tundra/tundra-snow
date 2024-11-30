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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private RecyclerView recylcerViewNotifications;
    private LinearLayout noNotificationsLayout;
    private String currentUserID;
    private ImageView backButton;
    private List<Notifications> notificationList;

    private NotificationsListAdapter notificationsAdapter;
    private FirebaseFirestore db;

    /**
     * onCreate method for the activity. Initializes the views and loads the list of events.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_notifications);

        Log.d("NotificationsActivity", "NotificationsActivity started.");

        recylcerViewNotifications = findViewById(R.id.myNotificationsRecyclerView);
        noNotificationsLayout = findViewById(R.id.noNotificationsLayout);

        // Organizer UI components
        backButton = findViewById(R.id.backButton);

        // Initialize Firebase and events list
        db = FirebaseFirestore.getInstance();
        notificationList = new ArrayList<>();

        // Set up Recycler View
        notificationsAdapter = new NotificationsListAdapter(this, notificationList);
        recylcerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        recylcerViewNotifications.setAdapter(notificationsAdapter);

        // Back Button Functionality
        backButton.setOnClickListener(view -> finish());

        fetchSessionUserId(() -> loadUserNotificationsFromFirestore());
    }

    /**
     * Loads the list of notifications from Firestore for the current user.
     */
    private void loadUserNotificationsFromFirestore() {
        Log.d("NotificationsActivity", "Loading notifications for userID: " + currentUserID);

        db.collection("notifications")
                .whereArrayContains("userIDs", currentUserID) // Check if the user is in userIDs
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        notificationList.clear();
                        Log.d("NotificationsActivity", "Query successful. Notifications found: " + task.getResult().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Notifications notification = document.toObject(Notifications.class);

                            Log.d("NotificationsActivity", "Retrieved Notification: "
                                    + "ID=" + notification.getNotificationID()
                                    + ", EventName=" + notification.getEventName()
                                    + ", Type=" + notification.getNotificationType());

                            notificationList.add(notification);
                        }

                        // Update UI based on the data
                        if (notificationList.isEmpty()) {
                            Log.d("NotificationsActivity", "Notification list is empty. Showing 'No Notifications' layout.");

                            noNotificationsLayout.setVisibility(View.VISIBLE);
                            recylcerViewNotifications.setVisibility(View.GONE);
                        } else {
                            Log.d("NotificationsActivity", "Notifications loaded successfully. Updating RecyclerView.");

                            noNotificationsLayout.setVisibility(View.GONE);
                            recylcerViewNotifications.setVisibility(View.VISIBLE);
                            notificationsAdapter.notifyDataSetChanged(); // Notify the adapter
                        }
                    } else {
                        if (task.getResult() != null) {
                            Log.d("NotificationsActivity", "Query successful but no notifications found.");
                        } else {
                            Log.e("NotificationsActivity", "Query failed or task result is null.", task.getException());
                        }

                        noNotificationsLayout.setVisibility(View.VISIBLE);
                        recylcerViewNotifications.setVisibility(View.GONE);
                        Toast.makeText(this, "No notifications found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationsActivity", "Error loading notifications: " + e.getMessage(), e);
                    Toast.makeText(this, "Error loading notifications: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        Log.d("NotificationsActivity", "loadUserNotificationsFromFirestore() method completed.");
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

                        if (currentUserID != null) {
                            Log.d("NotificationsActivity", "User ID fetched successfully: " + currentUserID);
                            onComplete.run(); // Invoke the callback here
                        } else {
                            Log.e("NotificationsActivity", "User ID is null. Cannot proceed.");
                            Toast.makeText(this, "Failed to fetch user ID.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("NotificationsActivity", "No active session found or query failed.");
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Session", "Error fetching session data", e);
                    Toast.makeText(this, "Error fetching session data.", Toast.LENGTH_SHORT).show();
                });
    }
}
