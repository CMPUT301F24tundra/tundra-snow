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

                        // Handle capacity - check if it exists first
                        Long capacity = documentSnapshot.getLong("capacity");
                        if (capacity != null) {
                            eventCapacityEditText.setText(String.valueOf(capacity));
                        } else {
                            eventCapacityEditText.setText("");  // Clear the field if no capacity was set
                        }

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

        if (!validateInputs()) {
            return;
        }

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

        // Handle capacity - default to 0 if empty
        int eventCapacity = 0;
        String capacityStr = eventCapacityEditText.getText().toString().trim();
        if (!capacityStr.isEmpty()) {
            try {
                eventCapacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                Log.e("CreateEvent", "Error parsing capacity", e);
            }
        }

        // Handle dates - could be null if not provided
        Date eventStartDate = null;
        Date eventEndDate = null;
        Date registrationStartDate = null;
        Date registrationEndDate = null;

        String startDateStr = eventStartDatePicker.getText().toString().trim();
        String endDateStr = eventEndDatePicker.getText().toString().trim();
        String regStartDateStr = eventRegistrationStartDatePicker.getText().toString().trim();
        String regEndDateStr = eventRegistrationEndDatePicker.getText().toString().trim();

        if (!startDateStr.isEmpty()) {
            eventStartDate = parseDate(startDateStr);
        }
        if (!endDateStr.isEmpty()) {
            eventEndDate = parseDate(endDateStr);
        }
        if (!regStartDateStr.isEmpty()) {
            registrationStartDate = parseDate(regStartDateStr);
        }
        if (!regEndDateStr.isEmpty()) {
            registrationEndDate = parseDate(regEndDateStr);
        }

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

        // Only add non-empty fields
        if (!eventDescription.isEmpty()) {
            event.put("description", eventDescription);
        }
        if (!eventLocation.isEmpty()) {
            event.put("location", eventLocation);
        }
        if (eventCapacity > 0) {
            event.put("capacity", eventCapacity);
        }
        if (eventStartDate != null) {
            event.put("startDate", eventStartDate);
        }
        if (eventEndDate != null) {
            event.put("endDate", eventEndDate);
        }
        if (registrationStartDate != null) {
            event.put("registrationStartDate", registrationStartDate);
        }
        if (registrationEndDate != null) {
            event.put("registrationEndDate", registrationEndDate);
        }

        event.put("status", "open");
        event.put("organizer", currentUserID);
        event.put("published", publishedStatus);

        if (facility != null && !facility.isEmpty()) {
            event.put("facility", facility);
        }

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

        if (eventTitleEditText.getText().toString().trim().isEmpty()) {
            showError("Event title is required even for drafts");
            return;
        }

        // If capacity is provided, validate it
        String capacityStr = eventCapacityEditText.getText().toString().trim();
        if (!capacityStr.isEmpty()) {
            try {
                int capacity = Integer.parseInt(capacityStr);
                if (capacity <= 0) {
                    showError("If specified, capacity must be greater than 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number for capacity");
                return;
            }
        }

        // Validate dates if any are provided
        if (!validateDates()) {
            return;
        }

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

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private boolean validateDates() {
        Date startDate = null;
        Date endDate = null;
        Date regStartDate = null;
        Date regEndDate = null;

        // Get current date without time component for fair comparison
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date currentDate = cal.getTime();

        // Parse all provided dates
        if (!eventStartDatePicker.getText().toString().isEmpty()) {
            startDate = parseDate(eventStartDatePicker.getText().toString());
            if (startDate == null) {
                showError("Invalid start date format");
                return false;
            }
        }

        if (!eventEndDatePicker.getText().toString().isEmpty()) {
            endDate = parseDate(eventEndDatePicker.getText().toString());
            if (endDate == null) {
                showError("Invalid end date format");
                return false;
            }
        }

        if (!eventRegistrationStartDatePicker.getText().toString().isEmpty()) {
            regStartDate = parseDate(eventRegistrationStartDatePicker.getText().toString());
            if (regStartDate == null) {
                showError("Invalid registration start date format");
                return false;
            }
        }

        if (!eventRegistrationEndDatePicker.getText().toString().isEmpty()) {
            regEndDate = parseDate(eventRegistrationEndDatePicker.getText().toString());
            if (regEndDate == null) {
                showError("Invalid registration end date format");
                return false;
            }
        }

        // Check if dates are after current date
        if (startDate != null && startDate.before(currentDate)) {
            showError("Event start date must be in the future");
            return false;
        }

        if (endDate != null && endDate.before(currentDate)) {
            showError("Event end date must be in the future");
            return false;
        }

        if (regStartDate != null && regStartDate.before(currentDate)) {
            showError("Registration start date must be in the future");
            return false;
        }

        if (regEndDate != null && regEndDate.before(currentDate)) {
            showError("Registration end date must be in the future");
            return false;
        }

        // Validate logical date relationships
        if (startDate != null && endDate != null) {
            // Check if end date is at least 30 minutes after start date
            long timeDifference = endDate.getTime() - startDate.getTime();
            long thirtyMinutesInMillis = 30 * 60 * 1000;
            if (timeDifference < thirtyMinutesInMillis) {
                showError("Event must be at least 30 minutes long");
                return false;
            }
        }

        // Basic order checks
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            showError("Event end date must be after start date");
            return false;
        }

        if (regStartDate != null && regEndDate != null && regEndDate.before(regStartDate)) {
            showError("Registration end date must be after registration start date");
            return false;
        }

        // Registration period must be before event start
        if (startDate != null && regStartDate != null && regStartDate.after(startDate)) {
            showError("Registration must start before event starts");
            return false;
        }

        if (startDate != null && regEndDate != null && regEndDate.after(startDate)) {
            showError("Registration must end before event starts");
            return false;
        }

        // Check reasonable time windows
        if (regStartDate != null && regEndDate != null) {
            // Registration period should be at least 1 hour
            long regPeriod = regEndDate.getTime() - regStartDate.getTime();
            long oneHourInMillis = 60 * 60 * 1000;
            if (regPeriod < oneHourInMillis) {
                showError("Registration period must be at least 1 hour");
                return false;
            }
        }

        // Check if event is too far in the future (e.g., more than 2 years)
        if (startDate != null) {
            Calendar twoYearsFromNow = Calendar.getInstance();
            twoYearsFromNow.add(Calendar.YEAR, 2);
            if (startDate.after(twoYearsFromNow.getTime())) {
                showError("Event cannot be scheduled more than 2 years in advance");
                return false;
            }
        }

        // For published events, registration should start at least 1 hour before event
        if (startDate != null && regStartDate != null && !eventTitleEditText.getText().toString().trim().isEmpty()) {
            long timeBeforeEvent = startDate.getTime() - regStartDate.getTime();
            long oneHourInMillis = 60 * 60 * 1000;
            if (timeBeforeEvent < oneHourInMillis) {
                showError("Registration must start at least 1 hour before event");
                return false;
            }
        }

        return true;
    }

    private boolean validateInputs() {
        // Check for empty required fields
        if (eventTitleEditText.getText().toString().trim().isEmpty()) {
            showError("Event title is required");
            return false;
        }

        if (eventDescriptionEditText.getText().toString().trim().isEmpty()) {
            showError("Event description is required");
            return false;
        }

        if (eventLocationEditText.getText().toString().trim().isEmpty()) {
            showError("Event location is required");
            return false;
        }

        // Check capacity
        String capacityStr = eventCapacityEditText.getText().toString().trim();
        if (capacityStr.isEmpty()) {
            showError("Event capacity is required");
            return false;
        }
        try {
            int capacity = Integer.parseInt(capacityStr);
            if (capacity <= 0) {
                showError("Capacity must be greater than 0");
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for capacity");
            return false;
        }

        // For a published event, all dates must be provided
        if (eventStartDatePicker.getText().toString().isEmpty()) {
            showError("Start date is required");
            return false;
        }

        if (eventEndDatePicker.getText().toString().isEmpty()) {
            showError("End date is required");
            return false;
        }

        if (eventRegistrationStartDatePicker.getText().toString().isEmpty()) {
            showError("Registration start date is required");
            return false;
        }

        if (eventRegistrationEndDatePicker.getText().toString().isEmpty()) {
            showError("Registration end date is required");
            return false;
        }

        // Validate the dates
        return validateDates();
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
