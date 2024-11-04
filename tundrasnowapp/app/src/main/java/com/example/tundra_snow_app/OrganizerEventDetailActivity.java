package com.example.tundra_snow_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class OrganizerEventDetailActivity extends AppCompatActivity {

    private EditText eventTitle, eventDate, eventLocation, eventDescription;
    private Button saveButton, editButton, backButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID;
    private TextView viewWaitingList, viewEnrolledList, viewChosenList, viewCancelledList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_detail);

        eventTitle = findViewById(R.id.organizerEventTitle);
        eventDate = findViewById(R.id.organizerEventDate);
        eventLocation = findViewById(R.id.organizerEventLocation);
        eventDescription = findViewById(R.id.organizerEventDescription);

        viewWaitingList = findViewById(R.id.viewWaitingList);
        viewEnrolledList = findViewById(R.id.viewEnrolledList);
        viewChosenList = findViewById(R.id.viewChosenList);
        viewCancelledList = findViewById(R.id.viewCancelledList);

        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");  // Get event ID from intent

        fetchSessionUserId(() -> {
            loadEventDetails();

            // Transition to ViewParticipantListActivity on viewEnrolledList click
            viewWaitingList.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewParticipantListActivity.class);
                intent.putExtra("eventID", eventID);  // Pass event ID to the detail activity
                startActivity(intent);
            });
            // Transition to ViewConfirmedParticipantListActivity on viewEnrolledList
            viewEnrolledList.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewConfirmedParticipantListActivity.class);
                intent.putExtra("eventID", eventID);  // Pass event ID to the detail activity
                startActivity(intent);
            });

            backButton.setOnClickListener(view -> finish());
            editButton.setOnClickListener(view -> enableEditing(true));
            saveButton.setOnClickListener(v -> saveEventUpdates());
            eventDate.setOnClickListener(v -> showDatePickerDialog(eventDate));
        });
    }

    private void loadEventDetails() {
        db.collection("events").document(eventID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if the current user is the organizer
                        String organizerId = documentSnapshot.getString("organizer");
                        if (organizerId == null || !organizerId.equals(currentUserID)) {
                            // If not the organizer, show error and go back
                            Toast.makeText(this, "ERROR: You are not the organizer of this event!", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        // If the user is the organizer, proceed to load event details
                        Events event = documentSnapshot.toObject(Events.class);
                        if (event != null) {
                            eventTitle.setText(event.getTitle());
                            eventDate.setText(event.getFormattedDate(event.getStartDate()));  // Use formatted date
                            eventLocation.setText(event.getLocation());
                            eventDescription.setText(event.getDescription());
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    private void saveEventUpdates() {
        String editedEventTitle = eventTitle.getText().toString();
        Date editedEventDate = parseDate(eventDate.getText().toString());
        String editedEventLocation = eventLocation.getText().toString();
        String editedEventDescription = eventDescription.getText().toString();

        if (editedEventDate == null) {
            Toast.makeText(this, "Please enter a valid date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the user profile in the "users" collection
        db.collection("events").document(eventID)
                .update("title", editedEventTitle,
                        "startDate", new Timestamp(editedEventDate),
                        "location", editedEventLocation,
                        "description", editedEventDescription)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    // Show DatePicker dialog
    public void showDatePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    // Parse date from String
    public Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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

    private void enableEditing(boolean isEditable) {
        eventTitle.setEnabled(isEditable);
        eventDate.setEnabled(isEditable);
        eventDate.setClickable(isEditable);
        eventLocation.setEnabled(isEditable);
        eventDescription.setEnabled(isEditable);

        // Toggle visibility of buttons
        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
