package com.example.tundra_snow_app.EventActivities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

    private EditText eventTitle, eventDate, eventEndDate, regStartDate, regEndDate, eventLocation, eventDescription;
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
        eventDate = findViewById(R.id.organizerStartDate);
        eventEndDate = findViewById(R.id.organizerEndDate);
        regStartDate = findViewById(R.id.organizerRegStartDate);
        regEndDate = findViewById(R.id.organizerRegEndDate);
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

            // Setting date-time picker for each date field
            eventDate.setOnClickListener(v -> showDateTimePickerDialog(eventDate));
            eventEndDate.setOnClickListener(v -> showDateTimePickerDialog(eventEndDate));
            regStartDate.setOnClickListener(v -> showDateTimePickerDialog(regStartDate));
            regEndDate.setOnClickListener(v -> showDateTimePickerDialog(regEndDate));
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
                            eventDate.setText(event.getFormattedDate(event.getStartDate()));
                            eventEndDate.setText(event.getFormattedDate(event.getEndDate()));
                            regStartDate.setText(event.getFormattedDate(event.getRegistrationStartDate()));
                            regEndDate.setText(event.getFormattedDate(event.getRegistrationEndDate()));
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
        Date editedEventEndDate = parseDate(eventEndDate.getText().toString());
        Date editedRegStartDate = parseDate(regStartDate.getText().toString());
        Date editedRegEndDate = parseDate(regEndDate.getText().toString());
        String editedEventLocation = eventLocation.getText().toString();
        String editedEventDescription = eventDescription.getText().toString();

        if (editedEventDate == null || editedEventEndDate == null || editedRegStartDate == null || editedRegEndDate == null) {
            Toast.makeText(this, "Please enter valid dates for all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String geoLocationRequirement = toggleGeolocationButton.isChecked() ? "Remote" : "In-person";

        db.collection("events").document(eventID)
                .update("title", editedEventTitle,
                        "startDate", new Timestamp(editedEventDate),
                        "endDate", new Timestamp(editedEventEndDate),
                        "registrationStartDate", new Timestamp(editedRegStartDate),
                        "registrationEndDate", new Timestamp(editedRegEndDate),
                        "location", editedEventLocation,
                        "description", editedEventDescription,
                        "geoLocationRequirement", geoLocationRequirement)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event Entry Updated Successfully.", Toast.LENGTH_LONG).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update event.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    /**
     * Show a combined date and time picker dialog to select a date and time.
     * @param editText The EditText view to set the selected date and time.
     */
    public void showDateTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();

        // Try to prefill the dialog with the existing date-time in the EditText
        String currentDateTimeText = editText.getText().toString();
        if (!currentDateTimeText.isEmpty()) {
            Date existingDate = parseDate(currentDateTimeText);
            if (existingDate != null) {
                calendar.setTime(existingDate);
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // First, show the date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Save the selected date and move to the time picker
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                    // Show time picker after selecting the date
                    showTimePickerDialog(calendar, editText);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    /**
     * Show a time picker dialog to select the time.
     * The combined date and time are set in the provided EditText in the desired format.
     * @param calendar The Calendar object with the selected date.
     * @param editText The EditText view to set the combined date and time.
     */
    private void showTimePickerDialog(Calendar calendar, final EditText editText) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    // Save the selected time and format the full date-time
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);

                    String formattedDateTime = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault())
                            .format(calendar.getTime());
                    editText.setText(formattedDateTime);
                },
                hour, minute, false
        );

        timePickerDialog.show();
    }

    /**
     * Parse a date string in the format "MMM dd, yyyy, hh:mm a" to a Date object.
     * @param dateString The date string to parse.
     * @return The parsed Date object, or null if parsing fails.
     */
    public Date parseDate(String dateString) {

        Log.w("DEBUG", "Parsing: " + dateString);

        // Match the format of the date string being passed
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a", Locale.getDefault());
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("parseDate", "Error parsing date: " + dateString, e);
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
        eventEndDate.setEnabled(isEditable);
        regStartDate.setEnabled(isEditable);
        regEndDate.setEnabled(isEditable);
        eventDate.setClickable(isEditable);
        eventEndDate.setClickable(isEditable);
        regStartDate.setClickable(isEditable);
        regEndDate.setClickable(isEditable);
        eventLocation.setEnabled(isEditable);
        eventDescription.setEnabled(isEditable);
        toggleGeolocationButton.setEnabled(isEditable);

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
