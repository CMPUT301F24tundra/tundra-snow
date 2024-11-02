package com.example.tundra_snow_app;

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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity{
    private EditText eventTitleEditText;
    private EditText eventDescriptionEditText;
    private EditText eventImageURLEditText;
    private EditText eventLocationEditText;
    private DatePicker eventStartDatePicker;
    private DatePicker eventEndDatePicker;
    private DatePicker eventRegistrationStartDatePicker;
    private DatePicker eventRegistrationEndDatePicker;
    private EditText eventCapacityEditText;
    private Button createEventButton;
    private Button backButton;

    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_event_view);
        FirebaseFirestore.setLoggingEnabled(true);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // TODO ADD IMAGE
        // TODO ADD USERID
        eventTitleEditText = findViewById(R.id.editTextEventTitle);
        eventDescriptionEditText = findViewById(R.id.editTextEventDescription);
        eventLocationEditText = findViewById(R.id.editTextLocation);
        eventStartDatePicker = findViewById(R.id.datePickerStartDate);
        eventEndDatePicker = findViewById(R.id.datePickerEndDate);
        eventRegistrationStartDatePicker = findViewById(R.id.datePickerRegistrationStart);
        eventRegistrationEndDatePicker = findViewById(R.id.datePickerRegistrationEnd);
        eventCapacityEditText = findViewById(R.id.editTextCapacity);
        createEventButton = findViewById(R.id.buttonCreateEvent);
        backButton = findViewById(R.id.buttonBack);

        createEventButton.setOnClickListener(v -> createEvent());
        backButton.setOnClickListener(v -> finish());
    }

    public void createEvent() {
        Log.d("Debug", "createEvent called");

        // Collecting details from user inputs
        String eventTitle = eventTitleEditText.getText().toString().trim();
        String eventDescription = eventDescriptionEditText.getText().toString().trim();
        String eventLocation = eventLocationEditText.getText().toString().trim();
        int eventCapacity = Integer.parseInt(eventCapacityEditText.getText().toString().trim());

        // Collecting dates from DatePickers
        int startDay = eventStartDatePicker.getDayOfMonth();
        int startMonth = eventStartDatePicker.getMonth();
        int startYear = eventStartDatePicker.getYear();
        Date eventStartDate = new Date(startYear - 1900, startMonth, startDay); // Date constructor is deprecated, use Calendar instead if preferred

        int endDay = eventEndDatePicker.getDayOfMonth();
        int endMonth = eventEndDatePicker.getMonth();
        int endYear = eventEndDatePicker.getYear();
        Date eventEndDate = new Date(endYear - 1900, endMonth, endDay);

        int regStartDay = eventRegistrationStartDatePicker.getDayOfMonth();
        int regStartMonth = eventRegistrationStartDatePicker.getMonth();
        int regStartYear = eventRegistrationStartDatePicker.getYear();
        Date registrationStartDate = new Date(regStartYear - 1900, regStartMonth, regStartDay);

        int regEndDay = eventRegistrationEndDatePicker.getDayOfMonth();
        int regEndMonth = eventRegistrationEndDatePicker.getMonth();
        int regEndYear = eventRegistrationEndDatePicker.getYear();
        Date registrationEndDate = new Date(regEndYear - 1900, regEndMonth, regEndDay);

        // Create a unique ID for the event
        String eventID = UUID.randomUUID().toString();

        // Prepare data to save in Firestore
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
        event.put("status", "open"); // You can adjust this as needed

        // Save to Firestore
        CollectionReference eventsRef = db.collection("events");
        eventsRef.document(eventID).set(event)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateEventActivity.this, "Event created successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("Firestore", "DocumentSnapshot successfully written!");
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateEventActivity.this, "Error creating event", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error writing document", e);
                });
    }
}
