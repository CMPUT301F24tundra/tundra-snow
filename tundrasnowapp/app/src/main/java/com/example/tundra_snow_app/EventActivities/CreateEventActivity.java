package com.example.tundra_snow_app.EventActivities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
    private EditText eventStartTimePicker;
    private EditText eventEndTimePicker;

    private EditText eventRegistrationStartDatePicker;
    private EditText eventRegistrationEndDatePicker;
    private EditText eventRegistrationStartTimePicker;
    private EditText eventRegistrationEndTimePicker;

    private EditText eventCapacityEditText;
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
        eventStartTimePicker = findViewById(R.id.editTextStartTime);
        eventEndTimePicker = findViewById(R.id.editTextEndTime);

        eventStartTimePicker.setOnClickListener(v -> showTimePickerDialog(eventStartTimePicker));
        eventEndTimePicker.setOnClickListener(v -> showTimePickerDialog(eventEndTimePicker));

        eventRegistrationStartDatePicker = findViewById(R.id.editRegistrationStartDate);
        eventRegistrationEndDatePicker = findViewById(R.id.editRegistrationEndDate);
        eventRegistrationStartTimePicker = findViewById((R.id.editRegistrationStartTime));
        eventRegistrationEndTimePicker = findViewById((R.id.editRegistrationEndTime));

        eventRegistrationStartTimePicker.setOnClickListener(v -> showTimePickerDialog(eventRegistrationStartTimePicker));
        eventRegistrationEndTimePicker.setOnClickListener(v -> showTimePickerDialog(eventRegistrationEndTimePicker));

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

    /**
     * Loads an existing draft event from the Firestore database.
     */
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

    /**
     * Preserves the existing lists for an event when updating it.
     * This means that the lists of entrants, confirmed, declined, cancelled, and chosen
     * participants are preserved (not overwritten) when updating an event.
     * 
     * @param event The event map to update.
     */
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

        // Parse capacity
        int eventCapacity = 0;
        String capacityStr = eventCapacityEditText.getText().toString().trim();
        if (!capacityStr.isEmpty()) {
            try {
                eventCapacity = Integer.parseInt(capacityStr);
            } catch (NumberFormatException e) {
                Log.e("CreateEvent", "Error parsing capacity: " + capacityStr, e);
            }
        }

        // Parse and combine dates and times
        Calendar eventStartDateTime = combineDateAndTime(
                parseDate(eventStartDatePicker.getText().toString().trim()),
                parseTime(eventStartTimePicker.getText().toString().trim())
        );
        Calendar eventEndDateTime = combineDateAndTime(
                parseDate(eventEndDatePicker.getText().toString().trim()),
                parseTime(eventEndTimePicker.getText().toString().trim())
        );
        Calendar regStartDateTime = combineDateAndTime(
                parseDate(eventRegistrationStartDatePicker.getText().toString().trim()),
                parseTime(eventRegistrationStartTimePicker.getText().toString().trim())
        );
        Calendar regEndDateTime = combineDateAndTime(
                parseDate(eventRegistrationEndDatePicker.getText().toString().trim()),
                parseTime(eventRegistrationEndTimePicker.getText().toString().trim())
        );

        // Initialize empty lists for entrants and statuses
        List<String> entrantList = new ArrayList<>();
        List<String> confirmedList = new ArrayList<>();
        List<String> declinedList = new ArrayList<>();
        List<String> cancelledList = new ArrayList<>();
        List<String> chosenList = new ArrayList<>();

        // Determine geolocation requirement based on toggle state
        String geolocationRequirement = toggleGeolocationButton.isChecked() ? "Remote" : "In-person";

        // Build event map
        Map<String, Object> event = new HashMap<>();
        event.put("eventID", eventID);
        event.put("title", eventTitle);
        event.put("status", "open");
        event.put("organizer", currentUserID);
        event.put("published", publishedStatus);
        event.put("geolocationRequirement", geolocationRequirement);

        // Only add non-empty fields
        if (!eventDescription.isEmpty()) event.put("description", eventDescription);
        if (!eventLocation.isEmpty()) event.put("location", eventLocation);
        if (eventCapacity > 0) event.put("capacity", eventCapacity);

        // Add dates and times if available
        event.put("startDateTime", eventStartDateTime.getTime());
        event.put("endDateTime", eventEndDateTime.getTime());
        event.put("registrationStartDateTime", regStartDateTime.getTime());
        event.put("registrationEndDateTime", regEndDateTime.getTime());

        // Add facility if available
        if (facility != null && !facility.isEmpty()) event.put("facility", facility);

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

    /**
     * Parses a time string into a Date object.
     * @param timeString The time string in HH:mm format.
     * @return The parsed Date object or null if invalid.
     */
    private Date parseTime(String timeString) {
        if (timeString.isEmpty()) return null;
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        try {
            return timeFormat.parse(timeString);
        } catch (ParseException e) {
            Log.e("CreateEvent", "Error parsing time: " + timeString, e);
            return null;
        }
    }

    /**
     * Combines a date and a time into a Calendar object.
     * @param date The Date object representing the date.
     * @param time The Date object representing the time.
     * @return A Calendar object with the combined date and time, or null if either is null.
     */
    private Calendar combineDateAndTime(Date date, Date time) {
        if (date == null || time == null) return null;

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(date);

        Calendar timeCal = Calendar.getInstance();
        timeCal.setTime(time);

        dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
        dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
        dateCal.set(Calendar.SECOND, 0);
        dateCal.set(Calendar.MILLISECOND, 0);

        return dateCal;
    }

    /**
     * Shows an error message to the user.
     * @param message The error message to display.
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Validates the dates and times for the event.
     * @return True if all dates and times are valid, false otherwise.
     */
    private boolean validateDates() {
        // Parse start and end dates
        Date startDate = parseDate(eventStartDatePicker.getText().toString());
        Date endDate = parseDate(eventEndDatePicker.getText().toString());
        Date regStartDate = parseDate(eventRegistrationStartDatePicker.getText().toString());
        Date regEndDate = parseDate(eventRegistrationEndDatePicker.getText().toString());

        // Parse start and end times
        Date startTime = parseTime(eventStartTimePicker.getText().toString());
        Date endTime = parseTime(eventEndTimePicker.getText().toString());
        Date regStartTime = parseTime(eventRegistrationStartTimePicker.getText().toString());
        Date regEndTime = parseTime(eventRegistrationEndTimePicker.getText().toString());

        // Ensure dates and times are provided
        if (startDate == null || endDate == null || regStartDate == null || regEndDate == null) {
            showError("All dates must be selected");
            return false;
        }
        if (startTime == null || endTime == null || regStartTime == null || regEndTime == null) {
            showError("All times must be selected");
            return false;
        }

        // Combine dates and times into Calendar objects
        Calendar startDateTime = combineDateAndTime(startDate, startTime);
        Calendar endDateTime = combineDateAndTime(endDate, endTime);
        Calendar regStartDateTime = combineDateAndTime(regStartDate, regStartTime);
        Calendar regEndDateTime = combineDateAndTime(regEndDate, regEndTime);

        // Get the current date and time
        Calendar currentDateTime = Calendar.getInstance();

        // Validate future dates
        if (startDateTime.before(currentDateTime)) {
            showError("Event start date and time must be in the future");
            return false;
        }
        if (endDateTime.before(currentDateTime)) {
            showError("Event end date and time must be in the future");
            return false;
        }
        if (regStartDateTime.before(currentDateTime)) {
            showError("Registration start date and time must be in the future");
            return false;
        }
        if (regEndDateTime.before(currentDateTime)) {
            showError("Registration end date and time must be in the future");
            return false;
        }

        // Validate logical relationships
        if (endDateTime.before(startDateTime)) {
            showError("Event end date and time must be after start date and time");
            return false;
        }
        if (regEndDateTime.before(regStartDateTime)) {
            showError("Registration end date and time must be after start date and time");
            return false;
        }
        if (regStartDateTime.after(startDateTime)) {
            showError("Registration must start before the event starts");
            return false;
        }
        if (regEndDateTime.after(startDateTime)) {
            showError("Registration must end before the event starts");
            return false;
        }

        // Additional validations
        if (startDateTime.getTimeInMillis() - regStartDateTime.getTimeInMillis() < 60 * 60 * 1000) {
            showError("Registration must start at least 1 hour before the event");
            return false;
        }
        if (endDateTime.getTimeInMillis() - startDateTime.getTimeInMillis() < 30 * 60 * 1000) {
            showError("Event duration must be at least 30 minutes");
            return false;
        }
        if (regEndDateTime.getTimeInMillis() - regStartDateTime.getTimeInMillis() < 60 * 60 * 1000) {
            showError("Registration duration must be at least 1 hour");
            return false;
        }

        // Validate event not too far in the future (e.g., 2 years)
        Calendar twoYearsFromNow = Calendar.getInstance();
        twoYearsFromNow.add(Calendar.YEAR, 2);
        if (startDateTime.after(twoYearsFromNow)) {
            showError("Event cannot be scheduled more than 2 years in advance");
            return false;
        }

        return true;
    }

    /**
     * Validates the input fields for creating an event.
     * @return True if all required fields are valid, false otherwise.
     */
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
     * Shows a TimePickerDialog for the given EditText.
     * @param editText The EditText to set the time to.
     */
    public void showTimePickerDialog(final EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    editText.setText(formattedTime);
                },
                hour, minute, true
        );

        timePickerDialog.show();
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
