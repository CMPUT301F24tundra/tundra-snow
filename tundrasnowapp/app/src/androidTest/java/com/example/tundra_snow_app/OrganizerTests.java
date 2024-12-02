package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.tundra_snow_app.Activities.QrScanActivity;
import com.example.tundra_snow_app.EventActivities.CreateEventActivity;
import com.example.tundra_snow_app.EventActivities.MyEventDetailActivity;
import com.example.tundra_snow_app.EventActivities.OrganizerEventDetailActivity;
import com.example.tundra_snow_app.ListActivities.ViewCancelledParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewChosenParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewConfirmedParticipantListActivity;
import com.example.tundra_snow_app.ListActivities.ViewParticipantListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String permanentEvent = "Important Test Event";
    String permanentEventID = "ef375549-e7b5-4078-8d22-959be14937f0";
    String permanentEntrant = "Main Organizer";
    String permanentEntrantID = "ef375549-e7b5-4078-8d22-959be14937f0";

    String testEventTitle = "";

    Set<String> generatedTitles  = new HashSet<>();

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // This account is an Entrant/Organizer
        onView(withId(R.id.usernameEditText)).perform(replaceText("333@gmail.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("333"));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);
    }

    @After
    public void tearDown() throws InterruptedException {
        // Log out after all tests
        auth.signOut();
        Intents.release();

        // Clean up test images from MediaStore
        Context context = ApplicationProvider.getApplicationContext();
        ContentResolver resolver = context.getContentResolver();

        // Delete test images from gallery
        resolver.delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DISPLAY_NAME + " LIKE ?",
                new String[]{"test_image_%"}
        );

        for (String title : generatedTitles) {
            // Query and delete test events with the specified title
            db.collection("events")
                    .whereEqualTo("title", title)  // Use the current title in the loop
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("TearDown", "Test event '" + title + "' deleted successfully"))
                                        .addOnFailureListener(e ->
                                                Log.e("TearDown", "Error deleting test event '" + title + "'", e));
                            }
                        } else {
                            Log.e("TearDown", "Error finding test events for '" + title + "'", task.getException());
                        }
                    });
        }
    }

    private void toggleToOrganizerMode() {
        ViewInteraction menuButton = onView(withId(R.id.menuButton));

        try {
            menuButton.perform(click());

            SystemClock.sleep(500);

            onView(withText("Organizer")).perform(click());

        } catch (NoMatchingViewException | PerformException e) {
            // Log the error for debugging purposes
            Log.e("MenuInteraction", "Failed to interact with menu", e);
        }
    }

    private void toggleToUserMode() {
        // First, find and click the menu button to open the menu
        ViewInteraction menuButton = onView(withId(R.id.menuButton));

        try {
            // Click the menu button to open the menu
            menuButton.perform(click());

            // After opening the menu, we need to wait briefly for the menu items to be displayed
            // This helps ensure menu animations are complete and items are clickable
            SystemClock.sleep(500);

            // Now find and click the organizer option in the opened menu
            // Note: You'll need to replace "Organizer" with the exact text that appears in your menu
            onView(withText("User")).perform(click());

        } catch (NoMatchingViewException | PerformException e) {
            // Log the error for debugging purposes
            Log.e("MenuInteraction", "Failed to interact with menu", e);
            // Optionally handle the error case based on your app's needs
        }
    }

    private void addEntrantToWaitingList() {
        db.collection("events")
                .document(permanentEventID)
                .update("entrantList", FieldValue.arrayUnion(permanentEntrantID))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Entrant added to waiting list"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding entrant to waiting list", e));

    }

    private void createTestEvent(String testEventTitle, Boolean generateHash) throws InterruptedException {

        generatedTitles.add(testEventTitle);

        // Switch to organizer mode before creating event
        toggleToOrganizerMode();

        // Navigate to event creation screen
        onView(withId(R.id.addEventButton)).perform(click());
        Thread.sleep(1000); // Allow time for the Create Event screen to load

        // Fill out basic event information
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
        onView(withId(R.id.editTextEventDescription))
                .perform(replaceText("This is a description for the test event."));
        onView(withId(R.id.editTextLocation)).perform(scrollTo(), replaceText("Test Location"));

        fillOutDates();

        // Set event capacity
        onView(withId(R.id.editTextCapacity)).perform(scrollTo(), replaceText("50"));

        // Toggle geolocation requirement (default is Enabled, clicking makes it Disabled)
        onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());

        if (generateHash)
        {
            onView(withId(R.id.generateHashInformation)).perform(scrollTo(), click());
        }

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(2000); // Allow time for event creation and database update

        // Switch back to attendee mode to verify event is visible
        toggleToUserMode();
        Thread.sleep(1000);
    }

    public static ViewAction scrollToItemWithText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                // The view must be a RecyclerView and be displayed
                return allOf(isDisplayed(), isAssignableFrom(RecyclerView.class));
            }

            @Override
            public String getDescription() {
                return "Scroll RecyclerView to find item with text: " + text;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.Adapter adapter = recyclerView.getAdapter();

                if (adapter == null) {
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException("Adapter is null"))
                            .build();
                }

                // Iterate through all items in the RecyclerView
                for (int position = 0; position < adapter.getItemCount(); position++) {
                    // Scroll to the position
                    recyclerView.scrollToPosition(position);
                    uiController.loopMainThreadUntilIdle();

                    // Get the view holder at this position
                    RecyclerView.ViewHolder holder =
                            recyclerView.findViewHolderForAdapterPosition(position);

                    if (holder != null) {
                        // Find the title TextView within the item view using the ID from your adapter
                        TextView titleView = holder.itemView.findViewById(R.id.eventName);

                        if (titleView != null && titleView.getText().toString().equals(text)) {
                            // We found the item, smooth scroll to it
                            recyclerView.smoothScrollToPosition(position);
                            uiController.loopMainThreadUntilIdle();
                            return;
                        }
                    }
                }

                // If we get here, we didn't find the item
                throw new PerformException.Builder()
                        .withActionDescription(getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .withCause(new RuntimeException("Could not find item with text: " + text))
                        .build();
            }
        };
    }

    private void fillOutDates() {
        LocalDate today = LocalDate.now();

        // Registration start (tomorrow)
        String regStartDate = formatDate(today.plusDays(1));
        onView(withId(R.id.editRegistrationStartDate)).perform(scrollTo(), replaceText(regStartDate));
        onView(withId(R.id.editRegistrationStartTime)).perform(scrollTo(), replaceText("09:00"));

        // Registration end (day after tomorrow)
        String regEndDate = formatDate(today.plusDays(2));
        onView(withId(R.id.editRegistrationEndDate)).perform(scrollTo(), replaceText(regEndDate));
        onView(withId(R.id.editRegistrationEndTime)).perform(scrollTo(), replaceText("17:00"));

        // Event start (in 3 days)
        String eventStartDate = formatDate(today.plusDays(3));
        onView(withId(R.id.editTextStartDate)).perform(scrollTo(), replaceText(eventStartDate));
        onView(withId(R.id.editTextStartTime)).perform(scrollTo(), replaceText("10:00"));

        // Event end (in 4 days)
        String eventEndDate = formatDate(today.plusDays(4));
        onView(withId(R.id.editTextEndDate)).perform(scrollTo(), replaceText(eventEndDate));
        onView(withId(R.id.editTextEndTime)).perform(scrollTo(), replaceText("16:00"));
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear());
    }

    // Helper method to add image to gallery
    private Uri addImageToGallery() {
        // Get the app's context
        Context context = ApplicationProvider.getApplicationContext();

        // Create a test image (a simple colored bitmap)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.RED);  // Create a red square image

        // Save bitmap to MediaStore (gallery)
        String imageFileName = "test_image_" + System.currentTimeMillis() + ".jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
            values.put(MediaStore.Images.Media.IS_PENDING, 1);
        }

        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try {
            if (imageUri != null) {
                OutputStream out = resolver.openOutputStream(imageUri);
                if (out != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.close();
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    values.clear();
                    values.put(MediaStore.Images.Media.IS_PENDING, 0);
                    resolver.update(imageUri, values, null, null);
                }
            }
        } catch (IOException e) {
            Log.e("TestSetup", "Error saving image to gallery", e);
            return null;
        }

        return imageUri;
    }

    /**
     * US 02.01.01 As an organizer I want to create a new event and
     * generate a unique promotional QR code that links to the event description
     * and event poster in the app
     * @throws InterruptedException
     */
    @Test
    public void testHashGeneration() throws InterruptedException {
        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;

        // Create event with QR code generation enabled
        createTestEvent(testEventTitle, Boolean.TRUE);
        Thread.sleep(2000); // Allow time for event creation and database update

        // Query Firestore to get the generated QR hash for the event
        final String[] qrHash = new String[1];
        final String[] eventId = new String[1];
        final CountDownLatch latch = new CountDownLatch(1);

        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                    qrHash[0] = eventDoc.getString("qrHash");
                    eventId[0] = eventDoc.getString("eventID");
                    latch.countDown();
                });

        // Wait for Firestore query to complete
        latch.await(5, TimeUnit.SECONDS);

        // Verify that a QR hash was generated
        assertNotNull("QR hash should not be null", qrHash[0]);
        assertFalse("QR hash should not be empty", qrHash[0].isEmpty());

        // Navigate to QR scanner activity
        onView(withId(R.id.nav_qr)).perform(click());
        Thread.sleep(1000);

        // Simulate scanning the QR code by directly calling handleScannedQRCode
        Intent qrScanIntent = new Intent(ApplicationProvider.getApplicationContext(), QrScanActivity.class);
        ActivityScenario<QrScanActivity> qrScanScenario = ActivityScenario.launch(qrScanIntent);

        // Simulate scanning the QR code
        qrScanScenario.onActivity(activity -> {
            activity.handleScannedQRCode(qrHash[0]);
        });
        Thread.sleep(2000);
        Thread.sleep(2000);

        // Verify that we're taken to the event details screen
        intended(hasComponent(MyEventDetailActivity.class.getName()));

        // Verify we're viewing the correct event
        onView(withId(R.id.detailEventTitle))
                .check(matches(withText(testEventTitle)));

        // Clean up - delete test event
        db.collection("events")
                .document(eventId[0])
                .delete();
    }

    /**
     * US 02.01.02 As an organizer I want to store the generated QR code
     * in my database
     * @throws InterruptedException
     */
    @Test
    public void testHashGenerationStored() throws InterruptedException {
        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        String testEventTitle = "QR Storage Test Event " + randomNumber;

        // Create event with QR code generation enabled
        createTestEvent(testEventTitle, Boolean.TRUE);
        Thread.sleep(2000); // Allow time for event creation and database update

        // Create a latch to wait for the async Firestore query
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] hasQrHash = {false};

        // Query Firestore to check if QR hash exists for the event
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String qrHash = eventDoc.getString("qrHash");
                        hasQrHash[0] = qrHash != null && !qrHash.isEmpty();
                    }
                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    Log.e("TestHashStorage", "Error querying Firestore", e);
                    latch.countDown();
                });

        // Wait for the Firestore query to complete (with timeout)
        assertTrue("Firestore query timed out", latch.await(5, TimeUnit.SECONDS));

        // Assert that the QR hash was found in the database
        assertTrue("QR hash should be stored in the database", hasQrHash[0]);
    }

    /**
     * Testing US 02.01.03
     * As an organizer, I want to create and manage my facility profile.
     * @throws InterruptedException
     */
    @Test
    public void testManagingFacilityProfile() throws InterruptedException {
        // Navigate to the profile
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(1000);

        toggleToOrganizerMode();
        Thread.sleep(1000);

        // Click on the "Add Facility" button
        onView(withId(R.id.addFacilityButton)).perform(click());
        Thread.sleep(1000);

        // Enter a facility name in the dialog and confirm
        String facilityName = "Test Facility";
        onView(withHint("Enter facility name")).perform(replaceText(facilityName));
        onView(withText("Add")).perform(click());
        Thread.sleep(1000);

        // Verify the facility was added by checking if it’s displayed
        onView(withText(facilityName)).check(matches(isDisplayed()));

        // Open facility options dialog for the newly added facility
        onView(withText(facilityName)).perform(click());
        Thread.sleep(1000);

        // Select "Edit" option
        onView(withText("Edit")).perform(click());
        Thread.sleep(1000);

        // Enter a new name in the edit dialog and confirm
        String editedFacilityName = "Updated Facility";
        onView(withText(facilityName)) // Matches the current name of the facility in the text field
                .perform(replaceText(editedFacilityName));
        onView(withText("Save")).perform(click());
        Thread.sleep(1000);

        // Verify the facility name was updated
        onView(withText(editedFacilityName)).check(matches(isDisplayed()));

        // Open facility options dialog for the edited facility
        onView(withText(editedFacilityName)).perform(click());
        Thread.sleep(1000);

        // Select "Delete" option from the options dialog
        onView(withText("Delete")).perform(click());
        Thread.sleep(1000);

        // Confirm deletion in the delete confirmation dialog
        onView(withText("Delete Facility")).check(matches(isDisplayed())); // Verifies the confirmation dialog title
        onView(withText("Are you sure you want to delete this facility?")).check(matches(isDisplayed())); // Verifies the message
        onView(withText("Delete")).perform(click()); // Confirm deletion
        Thread.sleep(1000);

        // Verify the facility was removed from the list
        onView(withText(facilityName)).check(doesNotExist());
    }

    /**
     * Testing US 02.02.01.
     * As an organizer I want to view the list of entrants who joined my event waiting list
     * @throws InterruptedException
     */
    @Test
    public void testViewWaitingList() throws InterruptedException {

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewParticipantListActivity.class.getName()));
    }

    /**
     * Testing US 02.02.03
     * As an organizer I want to enable or disable the geolocation requirement for my event.
     * @throws InterruptedException
     */
    @Test
    public void testCreateEventGeolocation() throws InterruptedException {
        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;

        createTestEvent(testEventTitle, Boolean.FALSE);

        // Switch back to attendee mode to verify event is visible
        toggleToUserMode();
        Thread.sleep(1000);

        // Verify the event appears in the list
        // Find and scroll to our event in the RecyclerView
        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testEventTitle));

        // Verify the event is visible
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
    }

    /**
     * Testing US 02.03.01
     * As an organizer I want to OPTIONALLY limit the number of entrants who
     * can join my waiting list
     * @throws InterruptedException
     */
    @Test
    public void testCreateEventNoCapacity() throws InterruptedException {

        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;
        generatedTitles.add(testEventTitle);

        // Switch to organizer mode before creating event
        toggleToOrganizerMode();

        // Navigate to event creation screen
        onView(withId(R.id.addEventButton)).perform(click());
        Thread.sleep(1000); // Allow time for the Create Event screen to load

        // Fill out basic event information
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
        onView(withId(R.id.editTextEventDescription))
                .perform(replaceText("This is a description for the test event."));
        onView(withId(R.id.editTextLocation)).perform(replaceText("Test Location"));

        fillOutDates();

        // Toggle geolocation requirement (default is Enabled, clicking makes it Disabled)
        onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(2000); // Allow time for event creation and database update

        // Switch back to attendee mode to verify event is visible
        toggleToUserMode();
        Thread.sleep(1000);

        // Verify the event appears in the list
        // Find and scroll to our event in the RecyclerView
        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testEventTitle));

        // Verify the event is visible
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
    }


    /**
     * US 02.04.01 As an organizer I want to upload an event poster to provide
     * visual information to entrants
     * @throws InterruptedException
     */
    @Test
    public void testEventPosterUpload() throws InterruptedException {
        // First add a test image to the emulator's gallery
        Uri imageUri = addImageToGallery();

        // Register the ActivityResult before launching the activity
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                new Intent().setData(imageUri)
        );

        // Set up intent stub for photo picker
        intending(hasAction(MediaStore.ACTION_PICK_IMAGES))
                .respondWith(result);

        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        String testEventTitle = "Poster Test Event " + randomNumber;
        generatedTitles.add(testEventTitle);

        // Switch to organizer mode
        toggleToOrganizerMode();
        Thread.sleep(1000);

        // Navigate to event creation screen
        onView(withId(R.id.addEventButton)).perform(click());
        Thread.sleep(1000);

        // Fill out required event details first so we can scroll properly
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
        onView(withId(R.id.editTextEventDescription))
                .perform(replaceText("Test event with poster"));
        onView(withId(R.id.editTextLocation)).perform(scrollTo(), replaceText("Test Location"));

        fillOutDates();

        // Scroll back to top to access image selection and trigger the picker
        onView(withId(R.id.selectImageButton)).perform(scrollTo(), click());

        // The photo picker will automatically return our registered result
        // So we don't need to interact with the system UI

        Thread.sleep(2000);

        // Verify image card view becomes visible
        onView(withId(R.id.eventImageCardView))
                .check(matches(isDisplayed()));

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(2000);

        // Verify the event was created with an image
        final boolean[] hasImage = {false};
        final CountDownLatch latch = new CountDownLatch(1);

        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String imageUrl = eventDoc.getString("imageUrl");
                        hasImage[0] = imageUrl != null && !imageUrl.isEmpty();
                    }
                    latch.countDown();
                });

        // Wait for Firestore query to complete
        assertTrue("Firestore query timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify image was uploaded
        assertTrue("Event should have an image URL stored", hasImage[0]);
    }

    /**
     * TODO US 02.04.02 As an organizer I want to update an event poster to provide
     *  visual information to entrants
     * @throws InterruptedException
     */
    @Test
    public void testEventPosterUpdate(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.05.01 As an organizer I want to send a notification to chosen entrants
     *  to sign up for events.
     * @throws InterruptedException
     */
    @Test
    public void testNotifisToChosen(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.05.02 As an organizer I want to set the system to sample a specified number
     *  of attendees to register for the event
     * @throws InterruptedException
     */
    @Test
    public void testRegistrationSample(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.05.03 As an organizer I want to be able to draw a replacement applicant from
     *  the pooling system when a previously selected applicant cancels or rejects the invitation
     * @throws InterruptedException
     */
    @Test
    public void testDrawingReplacement(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Testing US 02.06.01
     * As an organizer I want to view a list of all chosen entrants who are
     * invited to apply
     * @throws InterruptedException
     */
    @Test
    public void testViewChosenList() throws InterruptedException {

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewChosenList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewChosenParticipantListActivity.class.getName()));

    }

    /**
     * Testing US 02.06.02
     * As an organizer I want to see a list of all the cancelled entrants
     * @throws InterruptedException
     */
    @Test
    public void testViewCancelledList() throws InterruptedException {

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewCancelledList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewCancelledParticipantListActivity.class.getName()));

    }

    /**
     * Testing US 02.06.03
     * As an organizer I want to see a final list of entrants who enrolled
     * for the event
     * @throws InterruptedException
     */
    @Test
    public void testViewFinalEntrantList() throws InterruptedException {

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewEnrolledList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewConfirmedParticipantListActivity.class.getName()));

    }

    /**
     * TODO Testing US 02.06.04 As an organizer I want to cancel entrants that
     *  did not sign up for the event
     * @throws InterruptedException
     */
    @Test
    public void testDeletingEntrantFromWaitlist() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.06.04 As an organizer I want to cancel entrants that did not
     *  sign up for the event
     * @throws InterruptedException
     */
    @Test
    public void testCancellingEntrants() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.07.01 As an organizer I want to send notifications to all entrants
     *  on the waiting list
     * @throws InterruptedException
     */
    @Test
    public void testSendingNotifsToWaitlist() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.07.02 As an organizer I want to send notifications to all
     *  selected entrants
     * @throws InterruptedException
     */
    @Test
    public void testSendingNotifsToSelected() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.07.03 As an organizer I want to send a notification to all
     *  cancelled entrants
     * @throws InterruptedException
     */
    @Test
    public void testSendingNotifsToCancelled() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
