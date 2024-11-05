package com.example.tundra_snow_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;

import java.util.Date;

public class EventDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, locationTextView, descriptionTextView, geoLocationTextView;
    private Button signUpButton, backButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_event_view);

        // Initialize views
        titleTextView = findViewById(R.id.detailEventTitle);
        dateTextView = findViewById(R.id.detailEventDate);
        locationTextView = findViewById(R.id.detailEventLocation);
        descriptionTextView = findViewById(R.id.detailEventDescription);
        geoLocationTextView = findViewById(R.id.geoLocationNotification);
        backButton = findViewById(R.id.backButton);
        signUpButton = findViewById(R.id.buttonSignUpForEvent);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");  // Get event ID from intent

        fetchSessionUserId(() -> {
            loadEventDetails();
            backButton.setOnClickListener(view -> finish());
            signUpButton.setOnClickListener(v -> signUpForEvent());
        });
    }

    private void loadEventDetails() {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null) {
                            titleTextView.setText(event.getTitle());
                            dateTextView.setText(event.getFormattedDate(event.getStartDate()));  // Use formatted date
                            locationTextView.setText(event.getLocation());
                            descriptionTextView.setText(event.getDescription());

                            String geolocation = documentSnapshot.getString("geolocationRequirement");
                            if (geolocation != null && geolocation.equals("In-person")) {
                                geoLocationTextView.setVisibility(View.VISIBLE);
                            } else {
                                geoLocationTextView.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    private void signUpForEvent() {
        db.collection("events")
                .document(eventID)
                .update("entrantList", FieldValue.arrayUnion(currentUserID))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sign-up failed", Toast.LENGTH_SHORT).show());
    }

    // Fetch userId from latest session in "sessions" collection
    private void fetchSessionUserId(@NonNull Runnable onComplete) {
        CollectionReference sessionsRef = db.collection("sessions");
        sessionsRef.orderBy("loginTimestamp", Query.Direction.DESCENDING).limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot latestSession = task.getResult().getDocuments().get(0);
                        currentUserID = latestSession.getString("userId");
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }
}
