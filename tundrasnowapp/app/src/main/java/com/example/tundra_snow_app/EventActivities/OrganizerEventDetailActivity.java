package com.example.tundra_snow_app.EventActivities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.tundra_snow_app.Models.Events;
import com.example.tundra_snow_app.ListActivities.ViewCancelledParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewChosenParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewConfirmedParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewParticipantListActivity;

import com.example.tundra_snow_app.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity class for the Organizer Event Detail screen. This class is responsible for
 * displaying the details of an event and allowing the organizer to edit the event details.
 */
public class OrganizerEventDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText eventTitle, eventDate, eventEndDate, regStartDate, regEndDate, eventLocation, eventDescription;
    private Button saveButton, editButton, backButton;
    private FirebaseFirestore db;
    private String eventID, currentUserID, hashedData;
    private TextView viewWaitingList, viewEnrolledList, viewChosenList, viewCancelledList;
    private MapView mapView;
    private GoogleMap googleMap;
    private final Map<String, Integer> markerDuplicates = new HashMap<>();
    private List<Marker> markerList = new ArrayList<>();
    private FrameLayout qrView;
    private ImageView qrImageView;
    private ImageView eventImageView;
    private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
    private Uri selectedImageUri;
    private Button updateImageButton;

    /**
     * Initializes the views and loads the event details.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.organizer_event_detail);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        qrView = findViewById(R.id.QRView);
        qrImageView = findViewById(R.id.qrImageView);

        eventImageView = findViewById(R.id.eventImageView);
        updateImageButton = findViewById(R.id.updateImageButton);

        eventTitle = findViewById(R.id.organizerEventTitle);
        eventDate = findViewById(R.id.organizerStartDate);
        eventEndDate = findViewById(R.id.organizerEndDate);
        regStartDate = findViewById(R.id.organizerRegStartDate);
        regEndDate = findViewById(R.id.organizerRegEndDate);
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


        // Photo Picker Logic
        photoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        Glide.with(this).load(uri).into(eventImageView); // Show selected image
                        uploadImageToFirebase(); // Trigger upload to Firebase
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        updateImageButton.setOnClickListener(v -> {
            photoPickerLauncher.launch(new PickVisualMediaRequest.Builder().build());
        });
    }

    /**
     * Called when the Google Map is ready to be used. Sets the map instance and loads participant locations.
     *
     * @param map The Google Map object ready for use.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        Log.d("OrganizerEventDetail", "Google Map is ready.");

        loadParticipantLocations();
    }

    /**
     * Resumes the MapView lifecycle when the activity is resumed.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Pauses the MapView lifecycle when the activity is paused.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Cleans up the MapView lifecycle when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * Handles low memory situations by delegating to the MapView lifecycle.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Uploads the selected image to Firebase Storage and updates the event's image URL in Firestore.
     */
    private void uploadImageToFirebase() {
        if (selectedImageUri != null) {
            StorageReference fileReference = FirebaseStorage.getInstance()
                    .getReference("event_images/" + eventID + ".jpg");

            fileReference.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        fileReference.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    String downloadUrl = uri.toString();
                                    updateImageInFirestore(downloadUrl); // Update Firestore with new image URL
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                                    Log.e("ImageUpload", "Error fetching download URL", e);
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show();
                        Log.e("ImageUpload", "Error uploading image", e);
                    });
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the event's image URL in Firestore with the given download URL.
     *
     * @param downloadUrl The URL of the uploaded image.
     */
    private void updateImageInFirestore(String downloadUrl) {
        db.collection("events").document(eventID)
                .update("imageUrl", downloadUrl)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Image updated successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update image URL in database", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreUpdate", "Error updating Firestore", e);
                });
    }

    /**
     * Fetches participant locations for the event from Firestore and places markers on the map.
     */
    private void loadParticipantLocations() {
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(eventSnapshot -> {
                    if (eventSnapshot.exists()) {
                        List<String> entrantList = (List<String>) eventSnapshot.get("entrantList");
                        if (entrantList != null && !entrantList.isEmpty()) {
                            Log.d("OrganizerEventDetail", "Entrant list: " + entrantList);

                            for (String userId : entrantList) {
                                fetchUserLocationAndAddPin(userId);
                            }

                            // Delay camera adjustment to ensure all markers are added
                            new Handler().postDelayed(this::adjustCameraBounds, 1000);
                        } else {
                            Log.d("OrganizerEventDetail", "No participants found for this event.");
                        }
                    } else {
                        Log.e("OrganizerEventDetail", "Event not found: " + eventID);
                    }
                })
                .addOnFailureListener(e -> Log.e("OrganizerEventDetail", "Error fetching event details", e));
    }

    /**
     * Fetches the location of a user by their ID and adds a marker to the map at their location.
     *
     * @param userId The ID of the user whose location needs to be fetched.
     */
    private void fetchUserLocationAndAddPin(String userId) {
        Log.d("OrganizerEventDetail", "Fetching location for user ID: " + userId);

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        String address = userSnapshot.getString("location");
                        String firstName = userSnapshot.getString("firstName");
                        String lastName = userSnapshot.getString("lastName");
                        String name = (firstName != null ? firstName : "Unknown") + " " + (lastName != null ? lastName : "User");

                        Log.d("OrganizerEventDetail", "User " + name + " has location: " + address);

                        if (address != null) {
                            LatLng latLng = getLatLngFromAddress(address);

                            if (latLng != null) {
                                LatLng adjustedLatLng = adjustForDuplicateMarkers(latLng);
                                Log.d("OrganizerEventDetail", "Adding marker for " + name + " at: " + adjustedLatLng);

                                // Add marker to map
                                Marker marker = googleMap.addMarker(new MarkerOptions()
                                        .position(adjustedLatLng)
                                        .title(name)
                                        .snippet(address));
                                markerList.add(marker);

                                Log.d("OrganizerEventDetail", "Marker added at " + adjustedLatLng);
                            } else {
                                Log.w("OrganizerEventDetail", "Could not convert address to LatLng: " + address);
                            }
                        } else {
                            Log.w("OrganizerEventDetail", "User " + userId + " does not have a location.");
                        }
                    } else {
                        Log.e("OrganizerEventDetail", "User document not found for user ID: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("OrganizerEventDetail", "Error fetching user document for user ID: " + userId, e));
    }

    /**
     * Converts a string address into a LatLng object using the Geocoder.
     *
     * @param address The address to convert into coordinates.
     * @return The LatLng object representing the location, or null if conversion fails.
     */
    private LatLng getLatLngFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Log.d("OrganizerEventDetail", "Converting address to LatLng: " + address);

            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address location = addresses.get(0);
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                Log.d("OrganizerEventDetail", "Address converted to LatLng: " + latLng);

                return latLng;
            } else {
                Log.w("OrganizerEventDetail", "No LatLng found for address: " + address);
            }
        } catch (IOException e) {
            Log.e("OrganizerEventDetail", "Error getting LatLng from address: " + address, e);
        }
        return null;
    }

    /**
     * Adjusts marker positions slightly to avoid overlapping markers for duplicate coordinates.
     *
     * @param latLng The original LatLng object.
     * @return A new LatLng object adjusted for duplicates.
     */
    private LatLng adjustForDuplicateMarkers(LatLng latLng) {
        String key = latLng.latitude + "," + latLng.longitude;

        // Track duplicate marker adjustments
        int offsetCount = markerDuplicates.getOrDefault(key, 0);
        markerDuplicates.put(key, offsetCount + 1);

        // Apply a slight offset for duplicates
        double offset = offsetCount * 0.0001; // Adjust this value as needed
        return new LatLng(latLng.latitude + offset, latLng.longitude + offset);
    }

    /**
     * Adjusts the Google Map camera to include all markers or default to a city view if no markers exist.
     */
    private void adjustCameraBounds() {
        LatLng edmontonCenter = new LatLng(53.5461, -113.4938);

        if (markerList.isEmpty()) {
            Log.w("OrganizerEventDetail", "No markers to adjust camera bounds. Defaulting to city view.");

            // Example: Center the map on Edmonton, Alberta with a reasonable zoom level
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(edmontonCenter, 8));
            return;
        }

        // Build bounds to include all markers
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Marker marker : markerList) {
            boundsBuilder.include(marker.getPosition());
        }

        try {
            LatLngBounds bounds = boundsBuilder.build();
            int padding = 300;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } catch (IllegalArgumentException e) {
            Log.e("OrganizerEventDetail", "Error adjusting camera bounds: " + e.getMessage(), e);

            // If there is an issue with the bounds, fallback to a city view
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(edmontonCenter, 8));
        }
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

                        // Load image using Glide (or your preferred library)
                        String imageUrl = documentSnapshot.getString("imageUrl");
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .into(eventImageView);
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

                            // Fetch QR hash and generate QR code
                            hashedData = documentSnapshot.getString("qrHash");
                            if (hashedData != null) {
                                generateQRCode(hashedData);
                            } else {
                                qrView.setVisibility(View.GONE); // Hide QR view if no hash
                                Log.w("OrganizerEventDetail", "No QR hash found for event.");
                            }
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load event details", Toast.LENGTH_SHORT).show());
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
     * @return True if a field is invalid, otherwise false.
     */
    private boolean validateDates() {
        // Parse all dates
        Date startDateTime = parseDate(eventDate.getText().toString());
        Date endDateTime = parseDate(eventEndDate.getText().toString());
        Date regStartDateTime = parseDate(regStartDate.getText().toString());
        Date regEndDateTime = parseDate(regEndDate.getText().toString());

        // Check if any date is null
        if (startDateTime == null || endDateTime == null ||
                regStartDateTime == null || regEndDateTime == null) {
            showError("All dates and times must be selected");
            return true;
        }

        // Get current date/time
        Date currentDateTime = new Date();

        // Future date validations
        if (startDateTime.before(currentDateTime)) {
            showError("Event start must be in the future");
            return true;
        }
        if (endDateTime.before(currentDateTime)) {
            showError("Event end must be in the future");
            return true;
        }
        if (regStartDateTime.before(currentDateTime)) {
            showError("Registration start must be in the future");
            return true;
        }
        if (regEndDateTime.before(currentDateTime)) {
            showError("Registration end must be in the future");
            return true;
        }

        // Logical relationship validations
        if (endDateTime.before(startDateTime)) {
            showError("Event end must be after start");
            return true;
        }
        if (regEndDateTime.before(regStartDateTime)) {
            showError("Registration end must be after start");
            return true;
        }
        if (regStartDateTime.after(startDateTime)) {
            showError("Registration must start before event starts");
            return true;
        }
        if (regEndDateTime.after(startDateTime)) {
            showError("Registration must end before event starts");
            return true;
        }

        // Duration validations
        long hourInMillis = 60 * 60 * 1000;
        if (startDateTime.getTime() - regStartDateTime.getTime() < hourInMillis) {
            showError("Registration must start at least 1 hour before event");
            return true;
        }
        if (endDateTime.getTime() - startDateTime.getTime() < 30 * 60 * 1000) {
            showError("Event must be at least 30 minutes long");
            return true;
        }
        if (regEndDateTime.getTime() - regStartDateTime.getTime() < hourInMillis) {
            showError("Registration period must be at least 1 hour");
            return true;
        }

        // Check if event is too far in future
        Calendar twoYearsFromNow = Calendar.getInstance();
        twoYearsFromNow.add(Calendar.YEAR, 2);
        if (startDateTime.after(twoYearsFromNow.getTime())) {
            showError("Event cannot be scheduled more than 2 years ahead");
            return true;
        }

        return false;
    }

    /**
     * Validates the input fields for creating an event.
     * @return True if a field is invalid, otherwise false.
     */
    private boolean validateInputs() {
        // Check for empty required fields
        if (eventTitle.getText().toString().trim().isEmpty()) {
            showError("Event title is required");
            return true;
        }

        if (eventDescription.getText().toString().trim().isEmpty()) {
            showError("Event description is required");
            return true;
        }

        if (eventLocation.getText().toString().trim().isEmpty()) {
            showError("Event location is required");
            return true;
        }

        // For a published event, all dates must be provided
        if (eventDate.getText().toString().isEmpty()) {
            showError("Start date is required");
            return true;
        }

        if (eventEndDate.getText().toString().isEmpty()) {
            showError("End date is required");
            return true;
        }

        if (regStartDate.getText().toString().isEmpty()) {
            showError("Registration start date is required");
            return true;
        }

        if (regEndDate.getText().toString().isEmpty()) {
            showError("Registration end date is required");
            return true;
        }

        // Validate the dates
        return validateDates();
    }

    /**
     * Save the updated event details to Firestore.
     */
    private void saveEventUpdates() {

        if(validateInputs()){
            return;
        }

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

        // Update the user profile in the "users" collection
        db.collection("events").document(eventID)
                .update("title", editedEventTitle,
                        "startDate", new Timestamp(editedEventDate),
                        "endDate", new Timestamp(editedEventEndDate),
                        "registrationStartDate", new Timestamp(editedRegStartDate),
                        "registrationEndDate", new Timestamp(editedRegEndDate),
                        "location", editedEventLocation,
                        "description", editedEventDescription)
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

        editButton.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        saveButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        updateImageButton.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    /**
     * Generates a QR code for the given hashed data and displays it in the qrImageView.
     *
     * @param hashedData The hashed data to encode in the QR code.
     */
    private void generateQRCode(String hashedData) {
        Log.d("OrganizerEventDetail", "Hash Data: " + hashedData);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        try {
            int size = 512;
            Bitmap qrBitmap;
            com.google.zxing.common.BitMatrix bitMatrix = qrCodeWriter.encode(hashedData, BarcodeFormat.QR_CODE, size, size);

            qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            qrImageView.setImageBitmap(qrBitmap);
            qrView.setVisibility(View.VISIBLE);

            Log.d("OrganizerEventDetail", "QR code generated successfully.");
        } catch (WriterException e) {
            Log.e("OrganizerEventDetail", "Error generating QR code: " + e.getMessage(), e);
            Toast.makeText(this, "Failed to generate QR code.", Toast.LENGTH_SHORT).show();
        }
    }
}
