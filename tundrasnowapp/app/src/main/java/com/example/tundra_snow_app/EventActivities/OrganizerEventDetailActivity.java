package com.example.tundra_snow_app.EventActivities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.ListActivities.ViewCancelledParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewChosenParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewConfirmedParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewParticipantListActivity;

import com.example.tundra_snow_app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Activity class for the Organizer Event Detail screen. This class is responsible for
 * displaying the details of an event and allowing the organizer to edit the event details.
 */
public class OrganizerEventDetailActivity extends AppCompatActivity {

    private EditText eventTitle, eventDate, eventLocation, eventDescription;
    private Button saveButton, editButton, backButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID;
    private TextView viewWaitingList, viewEnrolledList, viewChosenList, viewCancelledList;
    private ToggleButton toggleGeolocationButton;

    /**
     * Initializes the views and loads the event details.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_detail);

        eventTitle = findViewById(R.id.organizerEventTitle);
        eventDate = findViewById(R.id.organizerEventDate);
        eventLocation = findViewById(R.id.organizerEventLocation);
        eventDescription = findViewById(R.id.organizerEventDescription);
        toggleGeolocationButton = findViewById(R.id.toggleGeolocationRequirement);

        viewWaitingList = findViewById(R.id.viewWaitingList);
        viewEnrolledList = findViewById(R.id.viewEnrolledList);
        viewChosenList = findViewById(R.id.viewChosenList);
        viewCancelledList = findViewById(R.id.viewCancelledList);

        backButton = findViewById(R.id.backButton);
        editButton = findViewById(R.id.editButton);
        saveButton = findViewById(R.id.saveButton);

        db = FirebaseFirestore.getInstance();
        eventID = getIntent().getStringExtra("eventID");  // Get event ID from intent

        // Set listener to update button text based on toggle state
        toggleGeolocationButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleGeolocationButton.setText(isChecked ? "Remote" : "In-person");
        });

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
            // Transition to ViewChosenParticipantListActivity on viewChosenList
            viewChosenList.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewChosenParticipantListActivity.class);
                intent.putExtra("eventID", eventID);  // Pass event ID to the detail activity
                startActivity(intent);
            });
            viewCancelledList.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewCancelledParticipantListActivity.class);
                intent.putExtra("eventID", eventID);  // Pass event ID to the detail activity
                startActivity(intent);
            });

            backButton.setOnClickListener(view -> finish());
            editButton.setOnClickListener(view -> enableEditing(true));
            saveButton.setOnClickListener(v -> saveEventUpdates());
            eventDate.setOnClickListener(v -> showDatePickerDialog(eventDate));
        });
    }

    /**
     * Load the event details from Firestore and populate the view with the event data.
     */
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

                        // Set the toggle state based on the geoLocationRequirement in Firestore
                        String geoLocationRequirement = documentSnapshot.getString("geoLocationRequirement");
                        if (geoLocationRequirement != null) {
                            toggleGeolocationButton.setChecked(geoLocationRequirement.equals("Remote"));
                            toggleGeolocationButton.setText(geoLocationRequirement.equals("Remote") ? "Remote" : "In-person");
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Save the updated event details to Firestore.
     */
    private void saveEventUpdates() {
        String editedEventTitle = eventTitle.getText().toString();
        Date editedEventDate = parseDate(eventDate.getText().toString());
        String editedEventLocation = eventLocation.getText().toString();
        String editedEventDescription = eventDescription.getText().toString();

        if (editedEventDate == null) {
            Toast.makeText(this, "Please enter a valid date.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the geoLocationRequirement value based on the toggle state
        String geoLocationRequirement = toggleGeolocationButton.isChecked() ? "Remote" : "In-person";

        // Update the user profile in the "users" collection
        db.collection("events").document(eventID)
                .update("title", editedEventTitle,
                        "startDate", new Timestamp(editedEventDate),
                        "location", editedEventLocation,
                        "description", editedEventDescription,
                        "geoLocationRequirement", geoLocationRequirement)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    /**
     * Show a date picker dialog to select a date.
     * @param editText The EditText view to set the selected date.
     */
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

    /**
     * Parse a date string in the format "dd/MM/yyyy" to a Date object.
     * @param dateString The date string to parse.
     * @return The parsed Date object.
     */
    public Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetch the userId of the current user from the latest session in the "sessions" collection.
     * @param onComplete The callback to execute after fetching the userId
     */
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

    /**
     * Enable or disable editing of the event details.
     * @param isEditable True to enable editing, false to disable.
     */
    private void enableEditing(boolean isEditable) {
        eventTitle.setEnabled(isEditable);
        eventDate.setEnabled(isEditable);
        eventDate.setClickable(isEditable);
        eventLocation.setEnabled(isEditable);
        eventDescription.setEnabled(isEditable);
        toggleGeolocationButton.setEnabled(isEditable);

        // Toggle visibility of buttons
        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
