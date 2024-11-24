package com.example.tundra_snow_app.EventActivities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tundra_snow_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;

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

/**
 * Activity class for creating an event. This class handles the creation of an event
 * and saving it to the Firestore database.
 */
public class CreateEventActivity extends AppCompatActivity{
    private EditText eventTitleEditText;
    private EditText eventDescriptionEditText;
    private EditText eventImageURLEditText;
    private EditText eventLocationEditText;
    private EditText eventStartDatePicker;
    private EditText eventEndDatePicker;

    private EditText eventRegistrationStartDatePicker, eventRegistrationEndDatePicker, eventCapacityEditText;
    private Button createEventButton, backButton, saveButton;
    private String eventID, currentUserID;
    private ToggleButton toggleGeolocationButton;
    private List<String> facility, entrantList, confirmedList, declinedList, cancelledList, chosenList;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseAuth auth;

    private boolean isEditingDraft = false;

    /**
     * onCreate method for the CreateEventActivity. Initializes the activity and sets up the UI.
     * @param savedInstanceState The saved instance state.
     */
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

        eventID = getIntent().getStringExtra("eventID");
        isEditingDraft = eventID != null;

        if (isEditingDraft) {
            // Update UI to reflect editing mode
            createEventButton.setText("Publish Event");
            loadDraftEvent();
        } else {
            eventID = UUID.randomUUID().toString();
        }

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

    private void loadDraftEvent() {
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Populate form fields with existing data
                        eventTitleEditText.setText(documentSnapshot.getString("title"));
                        eventDescriptionEditText.setText(documentSnapshot.getString("description"));
                        eventLocationEditText.setText(documentSnapshot.getString("location"));
                        eventCapacityEditText.setText(String.valueOf(documentSnapshot.getLong("capacity")));

                        // Handle dates
                        Date startDate = documentSnapshot.getDate("startDate");
                        Date endDate = documentSnapshot.getDate("endDate");
                        Date regStartDate = documentSnapshot.getDate("registrationStartDate");
                        Date regEndDate = documentSnapshot.getDate("registrationEndDate");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                        if (startDate != null) eventStartDatePicker.setText(dateFormat.format(startDate));
                        if (endDate != null) eventEndDatePicker.setText(dateFormat.format(endDate));
                        if (regStartDate != null) eventRegistrationStartDatePicker.setText(dateFormat.format(regStartDate));
                        if (regEndDate != null) eventRegistrationEndDatePicker.setText(dateFormat.format(regEndDate));

                        // Handle geolocation toggle
                        String geoRequirement = documentSnapshot.getString("geolocationRequirement");
                        toggleGeolocationButton.setChecked(geoRequirement != null && geoRequirement.equals("Remote"));

                        // Load other fields as needed...
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading draft event", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error loading draft", e);
                });
    }

    /**
     * Fetches the userId from the latest session in the "sessions" collection.
     * @param onComplete The runnable to execute after fetching the userId.
     */
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

    /**
     * Creates an event in the Firestore database.
     */
    private void createEvent() {
        Log.d("Debug", "createEvent called");
        Map<String, Object> event = collectEventDetails("yes");

        // If editing, preserve existing lists
        if (isEditingDraft) {
            preserveExistingLists(event);
        }

        db.collection("events").document(eventID)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    String message = isEditingDraft ? "Event updated and published!" : "Event published successfully!";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error publishing event", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error publishing document", e);
                });
    }

    private void preserveExistingLists(Map<String, Object> event) {
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Preserve existing lists
                        event.put("entrantList", documentSnapshot.get("entrantList"));
                        event.put("confirmedList", documentSnapshot.get("confirmedList"));
                        event.put("declinedList", documentSnapshot.get("declinedList"));
                        event.put("cancelledList", documentSnapshot.get("cancelledList"));
                        event.put("chosenList", documentSnapshot.get("chosenList"));
                    }
                });
    }

    /**
     * Collects the details of the event from the UI.
     * @param publishedStatus The status of the event.
     * @return A map of the event details.
     */
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

    /**
     * Saves the event as a draft in the Firestore database.
     */
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

    /**
     * Shows a DatePickerDialog for the given EditText.
     * @param editText The EditText to set the date to.
     */
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

    /**
     * Parses a date string into a Date object.
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
}
