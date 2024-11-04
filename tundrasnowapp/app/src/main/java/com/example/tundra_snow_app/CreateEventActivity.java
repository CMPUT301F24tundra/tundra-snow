package com.example.tundra_snow_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity{
    private EditText eventTitleEditText, eventDescriptionEditText, eventImageURLEditText, eventLocationEditText, eventStartDatePicker, eventEndDatePicker;
    private EditText eventRegistrationStartDatePicker, eventRegistrationEndDatePicker, eventCapacityEditText;
    private Button createEventButton, backButton, saveButton;
    private String eventID, currentUserID;
    private ToggleButton toggleGeolocationButton;
    private List<String> facility, entrantList, confirmedList, declinedList, cancelledList, chosenList;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_view);
        FirebaseFirestore.setLoggingEnabled(true);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // TODO ADD IMAGE
        eventTitleEditText = findViewById(R.id.editTextEventTitle);
        eventDescriptionEditText = findViewById(R.id.editTextEventDescription);
        eventLocationEditText = findViewById(R.id.editTextLocation);
        toggleGeolocationButton = findViewById(R.id.toggleGeolocationRequirement);
        eventCapacityEditText = findViewById(R.id.editTextCapacity);

        // Date sliders
        eventStartDatePicker = findViewById(R.id.editTextStartDate);
        eventEndDatePicker = findViewById(R.id.editTextEndDate);
        eventRegistrationStartDatePicker = findViewById(R.id.editRegistrationStartDate);
        eventRegistrationEndDatePicker = findViewById(R.id.editRegistrationEndDate);

        // Buttons
        createEventButton = findViewById(R.id.buttonCreateEvent);
        backButton = findViewById(R.id.buttonBack);
        saveButton = findViewById(R.id.saveButton);

        // Click listeners
        eventStartDatePicker.setOnClickListener(v -> showDatePickerDialog(eventStartDatePicker));
        eventEndDatePicker.setOnClickListener(v -> showDatePickerDialog(eventEndDatePicker));
        eventRegistrationStartDatePicker.setOnClickListener(v -> showDatePickerDialog(eventRegistrationStartDatePicker));
        eventRegistrationEndDatePicker.setOnClickListener(v -> showDatePickerDialog(eventRegistrationEndDatePicker));

        createEventButton.setOnClickListener(v -> createEvent());
        saveButton.setOnClickListener(v -> saveEvent());
        backButton.setOnClickListener(v -> finish());

        // Set listener to update based on toggle state
        toggleGeolocationButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                toggleGeolocationButton.setText("Remote");
            } else {
                toggleGeolocationButton.setText("In-person");
            }
        });

        // Set default or passed eventID
        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null) {
            eventID = UUID.randomUUID().toString();
        }

        // Retrieve facility before setting listeners
        fetchSessionUserId(() -> {
            createEventButton.setOnClickListener(v -> createEvent());
            saveButton.setOnClickListener(v -> saveEvent());
            backButton.setOnClickListener(v -> finish());
        });
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

                        List<String> facilityList = (List<String>) latestSession.get("facilityList");
                        if (facilityList != null) {
                            // Process each facility string if needed, or assign it directly
                            for (String facility : facilityList) {
                                Log.d("Session", "Facility: " + facility);
                            }
                            // Or, if you have a field for facility in the class, store the list:
                            this.facility = facilityList; // assuming facility is a List<String>
                        } else {
                            Log.d("Session", "No facilities found.");
                        }
                        onComplete.run();
                    } else {
                        Toast.makeText(this, "No active session found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("Session", "Error fetching session data", e));
    }


    private void createEvent() {
        Log.d("Debug", "createEvent called");
        Map<String, Object> event = collectEventDetails("yes");

        db.collection("events").document(eventID)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event published successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error publishing event", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error publishing document", e);
                });
    }

    private Map<String, Object> collectEventDetails(String publishedStatus) {
        String eventTitle = eventTitleEditText.getText().toString().trim();
        String eventDescription = eventDescriptionEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();
        int eventCapacity = Integer.parseInt(eventCapacityEditText.getText().toString().trim());

        Date eventStartDate = parseDate(eventStartDatePicker.getText().toString());
        Date eventEndDate = parseDate(eventEndDatePicker.getText().toString());
        Date registrationStartDate = parseDate(eventRegistrationStartDatePicker.getText().toString());
        Date registrationEndDate = parseDate(eventRegistrationEndDatePicker.getText().toString());

        // Initialize empty lists for entrants and statuses
        List<String> entrantList = new ArrayList<>();
        List<String> confirmedList = new ArrayList<>();
        List<String> declinedList = new ArrayList<>();
        List<String> cancelledList = new ArrayList<>();
        List<String> chosenList = new ArrayList<>();

        // Determine geolocation requirement based on toggle state
        String geolocationRequirement = toggleGeolocationButton.isChecked() ? "Remote" : "In-person";

        Map<String, Object> event = new HashMap<>();
        event.put("eventID", eventID);
        event.put("title", eventTitle);
        event.put("description", eventDescription);
        event.put("location", eventLocation);
        event.put("startDate", eventStartDate);
        event.put("endDate", eventEndDate);
        event.put("registrationStartDate", registrationStartDate);
        event.put("registrationEndDate", registrationEndDate);
        event.put("capacity", eventCapacity);
        event.put("status", "open");
        event.put("organizer", currentUserID);
        event.put("published", publishedStatus);
        event.put("facility", facility);
        event.put("geolocationRequirement", geolocationRequirement);

        // Initialize lists as empty arrays
        event.put("entrantList", entrantList);
        event.put("confirmedList", confirmedList);
        event.put("declinedList", declinedList);
        event.put("cancelledList", cancelledList);
        event.put("chosenList", chosenList);

        return event;
    }

    private void saveEvent() {
        Log.d("Debug", "saveEvent called");
        Map<String, Object> event = collectEventDetails("no");

        db.collection("events").document(eventID)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Event saved as draft!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving event as draft", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error saving draft document", e);
                });
    }

    public void showDatePickerDialog(final EditText editText) {
        // Set default to todays date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Set selected date to EditText
                    String formattedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(formattedDate);
                },
                year, month, day
        );

        // Enabling spinners
        datePickerDialog.getDatePicker().setCalendarViewShown(false);
        datePickerDialog.getDatePicker().setSpinnersShown(true);
        datePickerDialog.show();
    }

    public Date parseDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
