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
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.res.Resources;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

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
import android.util.Log;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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

    private void createListTestEvent() throws InterruptedException {
        testEventTitle = "Organizer List Testing Event";
        generatedTitles.add(testEventTitle);
        createTestEvent(testEventTitle, Boolean.FALSE);
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
    private Uri addImageToGallery(int color) {
        // Get the app's context
        Context context = ApplicationProvider.getApplicationContext();

        // Create a test image (a simple colored bitmap)
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(color);

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

    private void addUserToWaitingList(String eventId, String userId) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        // Add user to the waitingList array in Firestore
        db.collection("events")
                .document(eventId)
                .update("entrantList", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());

        assertTrue("Adding user to waiting list timed out", latch.await(5, TimeUnit.SECONDS));
    }

    private void verifyUserMovedToEntrantList(String eventId, String userId) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] isMoved = {false};

        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Check if user is in entrantList and not in waitingList
                        java.util.List<String> entrantList = (java.util.List<String>) documentSnapshot.get("chosenList");
                        java.util.List<String> waitingList = (java.util.List<String>) documentSnapshot.get("entrantList");

                        isMoved[0] = (entrantList != null && entrantList.contains(userId)) &&
                                (waitingList == null || !waitingList.contains(userId));
                    }
                    latch.countDown();
                });

        assertTrue("Verifying user move timed out", latch.await(5, TimeUnit.SECONDS));
        assertTrue("User should be moved to entrant list", isMoved[0]);
    }

    public static class RecyclerViewMatcher {
        private final int recyclerViewId;

        public RecyclerViewMatcher(int recyclerViewId) {
            this.recyclerViewId = recyclerViewId;
        }

        public Matcher<View> atPosition(final int position) {
            return new TypeSafeMatcher<View>() {
                Resources resources = null;
                View childView;

                public void describeTo(Description description) {
                    String idDescription = Integer.toString(recyclerViewId);
                    if (resources != null) {
                        idDescription = resources.getResourceName(recyclerViewId);
                    }
                    description.appendText("with id: " + idDescription + " at position: " + position);
                }

                public boolean matchesSafely(View view) {
                    resources = view.getResources();

                    if (childView == null) {
                        RecyclerView recyclerView = view.getRootView().findViewById(recyclerViewId);
                        if (recyclerView != null && recyclerView.getId() == recyclerViewId) {
                            childView = recyclerView.findViewHolderForAdapterPosition(position).itemView;
                        } else {
                            return false;
                        }
                    }

                    if (childView == null) {
                        return false;
                    }

                    return view == childView;
                }
            };
        }
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
        testEventTitle = "QR Storage Test Event " + randomNumber;

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

        createListTestEvent();

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(scrollTo(), click());

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
        onView(withId(R.id.editTextEventTitle)).perform(scrollTo(), replaceText(testEventTitle));
        onView(withId(R.id.editTextEventDescription))
                .perform(scrollTo(), replaceText("This is a description for the test event."));
        onView(withId(R.id.editTextLocation)).perform(scrollTo(), replaceText("Test Location"));

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
        Uri imageUri = addImageToGallery(Color.RED);

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
        testEventTitle = "Poster Test Event " + randomNumber;
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

        Thread.sleep(500);

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
     * US 02.04.02 As an organizer I want to update an event poster to provide
     * visual information to entrants
     * @throws InterruptedException
     */
    @Test
    public void testEventPosterUpdate() throws InterruptedException {
        // First add a test image to the emulator's gallery
        Uri imageUri = addImageToGallery(Color.RED);

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
        testEventTitle = "Poster Test Event " + randomNumber;
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

        Thread.sleep(500);

        // Verify image card view becomes visible
        onView(withId(R.id.eventImageCardView))
                .check(matches(isDisplayed()));

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.editButton)).perform(click());

        Thread.sleep(1000);

        // Create and add a new blue test image
        Uri blueImageUri = addImageToGallery(Color.BLUE);

        // Update the intent stub with new image
        Instrumentation.ActivityResult newResult = new Instrumentation.ActivityResult(
                Activity.RESULT_OK,
                new Intent().setData(blueImageUri)
        );

        intending(hasAction(MediaStore.ACTION_PICK_IMAGES))
                .respondWith(newResult);

        // Select new image
        onView(withId(R.id.updateImageButton)).perform(scrollTo(), click());
        Thread.sleep(500);

        // Verify image card view is still visible
        onView(withId(R.id.eventImageCardView))
                .check(matches(isDisplayed()));

        // Save the updated event
        onView(withId(R.id.saveButton)).perform(click());
        Thread.sleep(3000);

        // Verify the event was updated with a new image
        final String[] originalImageUrl = {null};
        final String[] updatedImageUrl = {null};
        final CountDownLatch latch = new CountDownLatch(1);

        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                        updatedImageUrl[0] = eventDoc.getString("imageUrl");
                    }
                    latch.countDown();
                });

        // Wait for Firestore query to complete
        assertTrue("Firestore query timed out", latch.await(5, TimeUnit.SECONDS));

        // Verify new image was uploaded and URL is different
        assertNotNull("Event should have an image URL stored", updatedImageUrl[0]);
        assertNotEquals("Image URL should be different after update", imageUri.toString(), updatedImageUrl[0]);
    }

    /**
     * US 02.05.01 As an organizer I want to send a notification to chosen entrants
     * to sign up for events.
     * @throws InterruptedException
     */
    @Test
    public void testNotifisToChosen() throws InterruptedException {
        createListTestEvent();
        Thread.sleep(1000);

        // First store current user ID and get event info
        final String[] eventId = {null};
        final String[] userId = {null};
        final CountDownLatch idLatch = new CountDownLatch(1);

        // Get the event ID and user ID
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventId[0] = queryDocumentSnapshots.getDocuments().get(0).getId();
                        userId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("organizer");
                    }
                    idLatch.countDown();
                });

        assertTrue("Getting event ID timed out", idLatch.await(5, TimeUnit.SECONDS));
        assertNotNull("Event ID should not be null", eventId[0]);
        assertNotNull("User ID should not be null", userId[0]);

        // Add current user to waiting list
        addUserToWaitingList(eventId[0], userId[0]);

        toggleToOrganizerMode();
        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);

        onView(withText(testEventTitle)).perform(scrollTo(), click());
        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());
        Thread.sleep(1000);

        intended(hasComponent(ViewParticipantListActivity.class.getName()));

        onView(withId(R.id.selectRegSampleButton)).perform(scrollTo(), click());
        Thread.sleep(5000);

        // Verify user was moved to chosen list
        verifyUserMovedToEntrantList(eventId[0], userId[0]);

        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(2000);

        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(1000);

        // Switch back to user mode to check notifications
        toggleToUserMode();
        Thread.sleep(1000);

        // Navigate to notifications
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(3000);

        // Check if the notification about being chosen is the first item
        onView(new RecyclerViewMatcher(R.id.myNotificationsRecyclerView)
                .atPosition(0))
                .perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.statusMessage)).check(matches(withText("You have been invited!")));
    }

    /**
     * US 02.05.02 As an organizer I want to set the system to sample a specified number
     *  of attendees to register for the event
     * @throws InterruptedException
     */
    @Test
    public void testRegistrationSample() throws InterruptedException {
        createListTestEvent();

        Thread.sleep(1000);

        // First store current user ID since we're already logged in from setUp()
        final String[] eventId = {null};
        final String[] userId = {null};
        final CountDownLatch idLatch = new CountDownLatch(1);

        // Get the event ID and user ID
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventId[0] = queryDocumentSnapshots.getDocuments().get(0).getId();
                        // The owner of the event is the current user
                        userId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("organizer");
                    }
                    idLatch.countDown();
                });

        assertTrue("Getting event ID timed out", idLatch.await(5, TimeUnit.SECONDS));
        assertNotNull("Event ID should not be null", eventId[0]);
        assertNotNull("User ID should not be null", userId[0]);

        // Add current user to waiting list
        addUserToWaitingList(eventId[0], userId[0]);

        toggleToOrganizerMode();
        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);

        onView(withText(testEventTitle)).perform(scrollTo(), click());
        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());
        Thread.sleep(1000);

        intended(hasComponent(ViewParticipantListActivity.class.getName()));

        onView(withId(R.id.selectRegSampleButton)).perform(scrollTo(), click());
        Thread.sleep(5000);

        // Verify that user was moved from waiting list to entrant list
        verifyUserMovedToEntrantList(eventId[0], userId[0]);
    }

    /**
     * TODO US 02.05.03 As an organizer I want to be able to draw a replacement applicant from
     *  the pooling system when a previously selected applicant cancels or rejects the invitation
     * @throws InterruptedException
     */
    @Ignore
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

        createListTestEvent();

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(scrollTo(), click());

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

        createListTestEvent();

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(scrollTo(), click());

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

        createListTestEvent();

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewEnrolledList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewConfirmedParticipantListActivity.class.getName()));

    }

    /**
     * Testing US 02.06.04 As an organizer I want to cancel entrants that
     *  did not sign up for the event
     * @throws InterruptedException
     */
    @Test
    public void testDeletingUserFromChosenList() throws InterruptedException {
        // Create test event
        createListTestEvent();
        Thread.sleep(1000);

        // First store current user ID and get event info
        final String[] eventId = {null};
        final String[] userId = {null};
        final CountDownLatch idLatch = new CountDownLatch(1);

        // Get the event ID and user ID
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventId[0] = queryDocumentSnapshots.getDocuments().get(0).getId();
                        userId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("organizer");
                    }
                    idLatch.countDown();
                });

        assertTrue("Getting event ID timed out", idLatch.await(5, TimeUnit.SECONDS));
        assertNotNull("Event ID should not be null", eventId[0]);
        assertNotNull("User ID should not be null", userId[0]);

        // Add user to entrant list directly
        final CountDownLatch addLatch = new CountDownLatch(1);
        db.collection("events")
                .document(eventId[0])
                .update("chosenList", FieldValue.arrayUnion(userId[0]))
                .addOnSuccessListener(aVoid -> addLatch.countDown())
                .addOnFailureListener(e -> addLatch.countDown());

        assertTrue("Adding user to chosen list timed out", addLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(1000);

        // Switch to organizer mode
        toggleToOrganizerMode();
        Thread.sleep(1000);

        // Navigate to organizer's events
        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);

        // Click on the test event
        onView(withText(testEventTitle)).perform(scrollTo(), click());
        Thread.sleep(1000);

        // Verify we're in organizer event detail activity
        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        // Click to view chosen list
        onView(withId(R.id.viewChosenList)).perform(scrollTo(), click());
        Thread.sleep(1000);

        // Verify we're in the chosen list activity
        intended(hasComponent(ViewChosenParticipantListActivity.class.getName()));

        // Click cancel button for the user
        onView(withId(R.id.cancelUserButton)).perform(scrollTo(), click());
        Thread.sleep(2000);

        // Verify user was removed from chosen list and added to declined list
        final CountDownLatch verifyLatch = new CountDownLatch(1);
        final boolean[] isMovedCorrectly = {false};

        db.collection("events")
                .document(eventId[0])
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        java.util.List<String> chosenList =
                                (java.util.List<String>) documentSnapshot.get("chosenList");
                        java.util.List<String> declinedList =
                                (java.util.List<String>) documentSnapshot.get("declinedList");

                        boolean removedFromChosen = chosenList == null || !chosenList.contains(userId[0]);

                        isMovedCorrectly[0] = removedFromChosen;
                    }
                    verifyLatch.countDown();
                });

        assertTrue("Verifying user status change timed out", verifyLatch.await(5, TimeUnit.SECONDS));
        assertTrue("User should be removed from chosen list", isMovedCorrectly[0]);
    }

    /**
     * TODO US 02.07.01 As an organizer I want to send notifications to all entrants
     *  on the waiting list
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testSendingNotifsToWaitlist() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.07.02 As an organizer I want to send notifications to all
     *  selected entrants
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testSendingNotifsToSelected() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 02.07.03 As an organizer I want to send a notification to all
     *  cancelled entrants
     * @throws InterruptedException
     */
    @Ignore
    @Test
    public void testSendingNotifsToCancelled() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
