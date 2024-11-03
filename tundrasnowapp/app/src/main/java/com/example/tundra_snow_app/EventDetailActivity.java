package com.example.tundra_snow_app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

public class EventDetailActivity extends AppCompatActivity {

    private TextView titleTextView, dateTextView, locationTextView, descriptionTextView;
    private Button signUpButton;
    private FirebaseFirestore db;
    private String eventID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_event_view);

        // Initialize views
        titleTextView = findViewById(R.id.detailEventTitle);
        dateTextView = findViewById(R.id.detailEventDate);
        locationTextView = findViewById(R.id.detailEventLocation);
        descriptionTextView = findViewById(R.id.detailEventDescription);
        signUpButton = findViewById(R.id.buttonSignUpForEvent);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");  // Get event ID from intent

        loadEventDetails();

        signUpButton.setOnClickListener(v -> signUpForEvent());
    }

    private void loadEventDetails() {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null) {
                            titleTextView.setText(event.getTitle());
                            dateTextView.setText(event.getFormattedStartDate());
                            locationTextView.setText(event.getLocation());
                            descriptionTextView.setText(event.getDescription());
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    private void signUpForEvent() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("events")
                .document(eventID)
                .update("entrantList", FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Signed up successfully!", Toast.LENGTH_SHORT).show();
                    signUpButton.setEnabled(false);  // Disable button after signing up
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Sign-up failed", Toast.LENGTH_SHORT).show());
    }
}
