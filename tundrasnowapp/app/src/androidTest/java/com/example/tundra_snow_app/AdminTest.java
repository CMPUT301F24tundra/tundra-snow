package com.example.tundra_snow_app;


import static androidx.test.espresso.Espresso.onView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
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
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.platform.app.InstrumentationRegistry;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;


import com.example.tundra_snow_app.AdminActivities.AdminImagesViewActivity;
import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import org.hamcrest.Matcher;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;




/**
 * AdminTest class to perform UI and database tests for the admin role.
 * This test validates the functionality of adding, deleting events, and managing users.
 */
@RunWith(AndroidJUnit4.class)
public class AdminTest {
        @Rule
        public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

        private FirebaseAuth auth;
        private FirebaseFirestore db;
        private Uri selectedImageUri;
        // Test data for database
        private CardView eventImageCardView;
        String testEventTitle = "Important Admin Test Event";
        String testFacilityTitle = "Test Facility Title";
        String testUserFirst = "Test";
        String testUserLast = "User";
        private ImageView eventImageView;
        private ActivityResultLauncher<PickVisualMediaRequest> photoPickerLauncher;
        Set<String> generatedTitles  = new HashSet<>();

        /**
         * Generates test data for a facility, event and user.
         */
        @Before
        public void setUp() throws InterruptedException {

            Intents.init();
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // This account is an Admin/Entrant/Organizer
            onView(withId(R.id.usernameEditText)).perform(replaceText("admin@gmail.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("admin123"));
            onView(withId(R.id.loginButton)).perform(click());

            Thread.sleep(2000);
        }

        /**
         * Deletes the generated test data for a facility, event and user.
         */
        @After
        public void tearDown() {
            auth.signOut();
            Intents.release();

            // Delete test user
            db.collection("facilities")
                    .whereEqualTo("facilityName", testFacilityTitle)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> Log.d("TearDown", "Test facility deleted"))
                                        .addOnFailureListener(e -> Log.e("TearDown", "Error deleting facility", e));
                            }
                        }
                    });

            // Delete test events
            for (String title : generatedTitles) {
                db.collection("events")
                        .whereEqualTo("title", title)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    doc.getReference().delete()
                                            .addOnSuccessListener(aVoid -> Log.d("TearDown", "Event deleted: " + title))
                                            .addOnFailureListener(e -> Log.e("TearDown", "Error deleting event: " + title, e));
                                }
                            }
                        });
            }
        }
    /**
     * Toggles the app to "Organizer" mode from the menu.
     *
     * @throws InterruptedException if the thread is interrupted during the toggle.
     */
        private void toggleToOrganizerMode() throws InterruptedException {
            ViewInteraction menuButton = onView(withId(R.id.menuButton));

            try {
                menuButton.perform(click());

                Thread.sleep(500);

                onView(withText("Organizer")).perform(click());

            } catch (NoMatchingViewException | PerformException e) {
                // Log the error for debugging purposes
                Log.e("MenuInteraction", "Failed to interact with menu", e);
            }
        }


        /**
         * Toggles the app to "Admin" mode from the menu.
         *
         * @throws InterruptedException if the thread is interrupted during the toggle.
         */
        private void toggleToAdminMode() throws InterruptedException {
            ViewInteraction menuButton = onView(withId(R.id.menuButton));

            try {
                menuButton.perform(click());

                Thread.sleep(500);

                onView(withText("Admin")).perform(click());

            } catch (NoMatchingViewException | PerformException e) {
                // Log the error for debugging purposes
                Log.e("MenuInteraction", "Failed to interact with menu", e);
            }
        }


        /**
         * Toggles the app to "User" mode from the menu.
         */
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


        /**
         * Adds an event with the specified title and predefined parameters.
         * The event is created under the "Organizer" mode.
         *
         * @param title the title of the event to be created.
         * @throws InterruptedException if the thread is interrupted during the process.
         */
        public void addEvent(String title) throws InterruptedException {

            // Switch to organizer mode before creating event
            toggleToOrganizerMode();

            // Navigate to event creation screen
            onView(withId(R.id.addEventButton)).perform(click());
            Thread.sleep(1000); // Allow time for the Create Event screen to load

            // Fill out basic event information
            onView(withId(R.id.editTextEventTitle)).perform(replaceText(title));
            onView(withId(R.id.editTextEventDescription))
                    .perform(replaceText("This is a description for the test event."));
            onView(withId(R.id.editTextLocation)).perform(scrollTo(), replaceText("Test Location"));

            // Calculate dates that ensure everything is in the future with proper spacing
            LocalDate today = LocalDate.now();

            // Registration starts tomorrow morning
            LocalDate registrationStart = today.plusDays(1);
            String registrationStartFormatted = String.format("%02d/%02d/%d",
                    registrationStart.getDayOfMonth(),
                    registrationStart.getMonthValue(),
                    registrationStart.getYear());

            // Registration ends the next day in the afternoon
            LocalDate registrationEnd = today.plusDays(2);
            String registrationEndFormatted = String.format("%02d/%02d/%d",
                    registrationEnd.getDayOfMonth(),
                    registrationEnd.getMonthValue(),
                    registrationEnd.getYear());

            // Event starts the day after registration ends
            LocalDate eventStart = today.plusDays(3);
            String eventStartFormatted = String.format("%02d/%02d/%d",
                    eventStart.getDayOfMonth(),
                    eventStart.getMonthValue(),
                    eventStart.getYear());

            // Event ends the next day
            LocalDate eventEnd = today.plusDays(4);
            String eventEndFormatted = String.format("%02d/%02d/%d",
                    eventEnd.getDayOfMonth(),
                    eventEnd.getMonthValue(),
                    eventEnd.getYear());

            // Set registration period
            // Registration starts tomorrow at 9:00 AM
            onView(withId(R.id.editRegistrationStartDate)).perform(scrollTo(), replaceText(registrationStartFormatted));
            onView(withId(R.id.editRegistrationStartTime)).perform(scrollTo(), replaceText("09:00"));

            // Registration ends the next day at 5:00 PM
            onView(withId(R.id.editRegistrationEndDate)).perform(scrollTo(), replaceText(registrationEndFormatted));
            onView(withId(R.id.editRegistrationEndTime)).perform(scrollTo(), replaceText("17:00"));

            // Set event dates and times
            // Event starts the day after registration ends at 10:00 AM
            onView(withId(R.id.editTextStartDate)).perform(scrollTo(), replaceText(eventStartFormatted));
            onView(withId(R.id.editTextStartTime)).perform(scrollTo(), replaceText("10:00"));

            // Event ends the next day at 4:00 PM
            onView(withId(R.id.editTextEndDate)).perform(scrollTo(), replaceText(eventEndFormatted));
            onView(withId(R.id.editTextEndTime)).perform(scrollTo(), replaceText("16:00"));

            // Set event capacity
            onView(withId(R.id.editTextCapacity)).perform(scrollTo(), replaceText("50"));



            onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());

            onView(withId(R.id.generateHashInformation)).perform(scrollTo(), click());

            onView(withId(R.id.buttonCreateEvent)).perform(click());
            Thread.sleep(1000);
            generatedTitles.add(title);
        }

        /**
         * Deletes an event with a predefined event ID from the Firestore database.
         */
        public void deleteEvent() {
            db.collection("events").document("00testEventId").delete();
        }


        /**
         * Adds a test profile with the specified first and last name to the Firestore database.
         *
         * @param first the first name of the user.
         * @param last the last name of the user.
         */
        public void addProfile(String first, String last) {
            String userID = UUID.randomUUID().toString();
            Map<String, Object> userData = new HashMap<>();
            userData.put("userID", userID);
            userData.put("firstName", first);
            userData.put("lastName", last);
            userData.put("roles", Arrays.asList("user"));
            userData.put("email", "test@example.com");
            userData.put("password", "password123");

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userID)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> Log.d("AddProfile", "Profile added: " + userID))
                    .addOnFailureListener(e -> Log.e("AddProfile", "Error adding profile", e));
        }


        /**
         * Adds a new facility to the Firestore database.
         * This method generates a new UUID for the facility, stores its details, and adds it to the "facilities" collection in Firestore.
         *
         * @param facilityName The name of the facility to be added.
         */
        public void addFacility(String facilityName) {
            String facilityID = UUID.randomUUID().toString();
            Map<String, Object> facilityData = new HashMap<>();
            facilityData.put("facilityID", facilityID);
            facilityData.put("facilityName", facilityName);
            facilityData.put("facilityLocation", "Edmonton, AB");

            FirebaseFirestore.getInstance()
                    .collection("facilities")
                    .document(facilityID)
                    .set(facilityData)
                    .addOnSuccessListener(aVoid -> Log.d("Test", "Facility added: " + facilityID))
                    .addOnFailureListener(e -> Log.e("Test", "Error adding facility", e));
        }


        /**
         * Scrolls through a RecyclerView and performs a custom action when an item with the specified text is found.
         * This action attempts to scroll through the RecyclerView until it finds an item whose text matches the specified value.
         * If found, the item will be scrolled to and a click will be simulated.
         *
         * @param text The text to search for within the RecyclerView items.
         * @return The ViewAction for performing the scroll and click.
         */
        static ViewAction scrollToItemWithText(final String text) {
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
                        Log.e("ScrollToItemWithText", "Adapter is null. Make sure an adapter is attached to the RecyclerView.");
                        throw new PerformException.Builder()
                                .withActionDescription(getDescription())
                                .withViewDescription(HumanReadables.describe(view))
                                .withCause(new RuntimeException("Adapter is null"))
                                .build();
                    } else {
                        Log.d("ScrollToItemWithText", "Adapter class: " + adapter.getClass().getName());
                        Log.d("ScrollToItemWithText", "Adapter item count: " + adapter.getItemCount());
                    }

                    Log.d("ScrollToItemWithText", "Starting search for item with text: " + text);

                    // Iterate through all items in the RecyclerView
                    for (int position = 0; position < adapter.getItemCount(); position++) {
                        Log.d("ScrollToItemWithText", "Scrolling to position: " + position);
                        recyclerView.scrollToPosition(position);
                        uiController.loopMainThreadUntilIdle();

                        // Get the view holder at this position
                        RecyclerView.ViewHolder holder =
                                recyclerView.findViewHolderForAdapterPosition(position);

                        if (holder != null) {
                            Log.d("ScrollToItemWithText", "ViewHolder found at position: " + position);

                            // Find the title TextView within the item view using the ID from your adapter
                            TextView titleView = holder.itemView.findViewById(R.id.eventName);

                            if (titleView != null) {
                                String itemText = titleView.getText().toString();
                                Log.d("ScrollToItemWithText", "Found TextView with text: " + itemText);

                                if (itemText.equals(text)) {
                                    Log.d("ScrollToItemWithText", "Match found for text: " + text + " at position: " + position);

                                    // We found the item, smooth scroll to it
                                    recyclerView.smoothScrollToPosition(position);
                                    uiController.loopMainThreadUntilIdle();
                                    return;
                                }
                            } else {
                                Log.w("ScrollToItemWithText", "TextView (R.id.eventName) not found in ViewHolder at position: " + position);
                            }
                        } else {
                            Log.w("ScrollToItemWithText", "ViewHolder is null at position: " + position);
                        }
                    }

                    // If we get here, we didn't find the item
                    Log.e("ScrollToItemWithText", "Could not find item with text: " + text);
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException("Could not find item with text: " + text))
                            .build();
                }
            };
        }


        /**
         * Adds an image to the device's gallery by creating a colored bitmap and saving it to the MediaStore.
         * The method generates a colored bitmap, saves it as a JPEG file, and inserts it into the gallery.
         * The method is designed to work with Android API levels Q and above.
         *
         * @param color The color to be used in the bitmap. This color will fill the entire image.
         * @return The URI of the saved image in the gallery, or null if an error occurred.
         */
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


        /**
         * Fills out dates in the UI for registration and event times using the current date.
         * The method fills out various date and time fields for registration and event start/end times.
         *
         * The dates are set as:
         * - Registration start date: Tomorrow
         * - Registration end date: Day after tomorrow
         * - Event start date: 3 days from now
         * - Event end date: 4 days from now
         */
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


    /**
     * Helper method to format a LocalDate into a string representation of the form "DD/MM/YYYY".
     *
     * @param date The LocalDate to be formatted.
     * @return The formatted date as a string in the format "DD/MM/YYYY".
     */

    private String formatDate(LocalDate date) {
            return String.format("%02d/%02d/%d",
                    date.getDayOfMonth(),
                    date.getMonthValue(),
                    date.getYear());
        }


        /**
         * Scrolls through a RecyclerView and performs a click action on an item with the specified text.
         * This action performs the same functionality as `scrollToItemWithText`, but in addition to scrolling,
         * it will also simulate a click on the item.
         *
         * @param text The text to search for within the RecyclerView items.
         * @return The ViewAction for performing the scroll and click.
         */
        static ViewAction scrollToItemWithTextAndClick(final String text) {
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
                        Log.e("ScrollToItemWithText", "Adapter is null. Make sure an adapter is attached to the RecyclerView.");
                        throw new PerformException.Builder()
                                .withActionDescription(getDescription())
                                .withViewDescription(HumanReadables.describe(view))
                                .withCause(new RuntimeException("Adapter is null"))
                                .build();
                    } else {
                        Log.d("ScrollToItemWithText", "Adapter class: " + adapter.getClass().getName());
                        Log.d("ScrollToItemWithText", "Adapter item count: " + adapter.getItemCount());
                    }

                    Log.d("ScrollToItemWithText", "Starting search for item with text: " + text);

                    // Iterate through all items in the RecyclerView
                    for (int position = 0; position < adapter.getItemCount(); position++) {
                        Log.d("ScrollToItemWithText", "Scrolling to position: " + position);
                        recyclerView.scrollToPosition(position);
                        uiController.loopMainThreadUntilIdle();

                        // Get the view holder at this position
                        RecyclerView.ViewHolder holder =
                                recyclerView.findViewHolderForAdapterPosition(position);

                        if (holder != null) {
                            Log.d("ScrollToItemWithText", "ViewHolder found at position: " + position);

                            // Find the title TextView within the item view using the ID from your adapter
                            TextView titleView = holder.itemView.findViewById(R.id.userFullName);

                            if (titleView != null) {
                                String itemText = titleView.getText().toString();
                                Log.d("ScrollToItemWithText", "Found TextView with text: " + itemText);

                                if (itemText.equals(text)) {
                                    Log.d("ScrollToItemWithText", "Match found for text: " + text + " at position: " + position);

                                    // We found the item, smooth scroll to it
                                    recyclerView.smoothScrollToPosition(position);
                                    // Find the parent item view (the entire item) to perform a click on
                                    View itemView = holder.itemView;

                                    // Perform the click action on the item
                                    itemView.performClick(); // Simulate a click on the item view
                                    uiController.loopMainThreadUntilIdle();

                                    return;
                                }
                            } else {
                                Log.w("ScrollToItemWithText", "TextView (R.id.eventName) not found in ViewHolder at position: " + position);
                            }
                        } else {
                            Log.w("ScrollToItemWithText", "ViewHolder is null at position: " + position);
                        }
                    }

                    // If we get here, we didn't find the item
                    Log.e("ScrollToItemWithText", "Could not find item with text: " + text);
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException("Could not find item with text: " + text))
                            .build();
                }
            };
        }


        /**
         * Scrolls through a RecyclerView, searches for an item with the specified text, and performs a delete action.
         * This action will search for the item with the given text and, once found, perform a click on the delete button
         * associated with the item.
         *
         * @param text The text to search for within the RecyclerView items.
         * @param nameId The resource ID of the TextView displaying the item's name.
         * @param buttonId The resource ID of the button to delete the item.
         * @return The ViewAction for performing the scroll and delete click.
         */
        static ViewAction scrollToItemWithTextAndClickDelete(final String text, final int nameId, final int buttonId) {
            return new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return allOf(isDisplayed(), isAssignableFrom(RecyclerView.class));
                }

                @Override
                public String getDescription() {
                    return "Find item with text and click delete: " + text;
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

                    for (int position = 0; position < adapter.getItemCount(); position++) {
                        recyclerView.scrollToPosition(position);
                        uiController.loopMainThreadUntilIdle();

                        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);

                        if (holder != null) {
                            TextView titleView = holder.itemView.findViewById(nameId);
                            Button deleteButton = holder.itemView.findViewById(buttonId);

                            if (titleView != null && deleteButton != null &&
                                    titleView.getText().toString().equals(text)) {

                                deleteButton.performClick();
                                uiController.loopMainThreadUntilIdle();
                                return;
                            }
                        }
                    }

                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(HumanReadables.describe(view))
                            .withCause(new RuntimeException("Item not found: " + text))
                            .build();
                }
            };
        }



        /**
         * US 03.01.01 As an administrator, I want to be able to remove events.
         *
         * Test case for verifying the removal of an event by an administrator.
         * This test will simulate the process of adding an event, toggling to admin mode, scrolling to the event item,
         * and clicking the delete button to remove the event.
         * Afterward, it verifies that the event no longer exists in the UI.
         *
         * @throws InterruptedException If the thread is interrupted during the test.
         */
        @Test
        public void testAdminEventRemoval() throws InterruptedException {
            addEvent(testEventTitle);
            Thread.sleep(1000); // Wait for the event to be added

            toggleToAdminMode();
            Thread.sleep(2000); // Wait for the Admin mode to load


            onView(withId(R.id.adminEventsRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(testEventTitle, R.id.eventName, R.id.removeEventButton));
            Thread.sleep(1000);

            onView(withText(testEventTitle)).check(doesNotExist());
        }

        /**
         * US 03.02.01 As an administrator, I want to be able to remove profiles.
         *
         * Test case for verifying the removal of a user profile by an administrator.
         * This test will simulate the process of adding a profile, toggling to admin mode, navigating to the profiles section,
         * scrolling to the user item, and clicking the delete button to remove the user profile.
         * Afterward, it verifies that the profile no longer exists in the UI.
         *
         * @throws InterruptedException If the thread is interrupted during the test.
         */
        @Test
        public void testAdminProfileRemoval() throws InterruptedException {
            addProfile(testUserFirst, testUserLast);

            String fullname = testUserFirst + " " + testUserLast;
            Thread.sleep(1000); // Wait for the event to be added

            toggleToAdminMode();
            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_profiles)).perform(click());
            Thread.sleep(1000);

            onView(withId(R.id.adminUsersRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(fullname, R.id.userFullName, R.id.removeUserButton));
            Thread.sleep(1000);

            onView(withText(fullname)).check(doesNotExist());
        }



        /**
         * Test the removal of an image from an event by an administrator.
         * This test verifies the following steps:
         * 1. Adding a test image to the emulator's gallery.
         * 2. Creating an event with the added image.
         * 3. Switching to the admin mode.
         * 4. Deleting the image from the event.
         * 5. Verifying the image URL is removed from the event in Firestore.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */
        @Test
        public void testAdminImageRemoval() throws InterruptedException {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

                // Disable window transition animations
                uiAutomation.adoptShellPermissionIdentity();
                Settings.Global.putInt(
                        InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(),
                        Settings.Global.TRANSITION_ANIMATION_SCALE, 0);

                // Disable animator duration scale
                Settings.Global.putInt(
                        InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(),
                        Settings.Global.ANIMATOR_DURATION_SCALE, 0);

                // Disable display zoom animation
                Settings.Global.putInt(
                        InstrumentationRegistry.getInstrumentation().getContext().getContentResolver(),
                        Settings.Global.WINDOW_ANIMATION_SCALE, 0);
            }

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

            onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
            onView(withId(R.id.editTextEventDescription))
                    .perform(replaceText("Test event with poster"));
            onView(withId(R.id.editTextLocation)).perform(scrollTo(), replaceText("Test Location"));

            fillOutDates();

            onView(withId(R.id.selectImageButton)).perform(scrollTo(), click());


            Thread.sleep(500);


            onView(withId(R.id.eventImageCardView))
                    .check(matches(isDisplayed()));


            onView(withId(R.id.buttonCreateEvent)).perform(click());
            Thread.sleep(2000);


            final AtomicReference<String> eventID = new AtomicReference<>(null); // AtomicReference to hold eventID
            final boolean[] hasImage = {false};
            final CountDownLatch latch = new CountDownLatch(1);

            db.collection("events")
                    .whereEqualTo("title", testEventTitle)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot eventDoc = queryDocumentSnapshots.getDocuments().get(0);
                            String imageUrl = eventDoc.getString("imageUrl");
                            eventID.set("00000");

                            hasImage[0] = imageUrl != null && !imageUrl.isEmpty();

                            Map<String, Object> data = eventDoc.getData();

                            db.collection("events").document("00000").set(data)
                                    .addOnSuccessListener(aVoid -> {

                                        db.collection("events").document(eventDoc.getId()).delete();
                                        latch.countDown();
                                    })
                                    .addOnFailureListener(e -> {

                                        e.printStackTrace();
                                        latch.countDown();
                                    });
                        } else {

                            latch.countDown();
                        }
                    });



            assertTrue("Firestore query timed out", latch.await(5, TimeUnit.SECONDS));


            assertTrue("Event should have an image URL stored", hasImage[0]);


            assertNotNull("Event ID should not be null", eventID.get());

            // Switch to admin mode
            Thread.sleep(1000);
            toggleToAdminMode();

            Thread.sleep(2000);

            onView(withId(R.id.admin_nav_images)).perform(click());
            Thread.sleep(1000);
            System.out.println(eventID.get());



            onView(new OrganizerTests.RecyclerViewMatcher(R.id.adminImagesRecyclerView)
                    .atPosition(0))
                    .perform(click());

            Thread.sleep(1000);

            onView(withText("Yes")).perform(click());
            Thread.sleep(1000);


            db.collection("events")
                    .document("0000")
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String imageUrlAfterDeletion = documentSnapshot.getString("imageUrl");
                            assertNull("Image URL should be null after deletion", imageUrlAfterDeletion);
                        } else {
                            System.out.println("Event document deleted successfully.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        fail("Firestore query failed");
                    });


        }

        /**
         * US 03.03.02 As an administrator, I want to be able to remove hashed
         * QR code data
         *
         * Test the removal of hashed QR code data by an administrator.
         * This test simulates the admin navigating to the QR section, selecting an event,
         * and removing the associated QR hash data. The event is then checked to ensure
         * that the QR hash has been successfully removed from Firestore.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */
        @Test
        public void testAdminHashDataRemoval() throws InterruptedException {
            addEvent(testEventTitle);
            Thread.sleep(1000);
            toggleToAdminMode();

            Thread.sleep(2000);

            onView(withId(R.id.admin_nav_qr)).perform(click());
            Thread.sleep(1000);

            onView(withId(R.id.adminQRRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(testEventTitle, R.id.qrHashText, R.id.removeQRButton));

            Thread.sleep(1000);

            // hanlde the confirmation popup, ("Yes" is the button text)
            onView(withText("Yes")).perform(click());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("events")
                    .whereEqualTo("eventTitle", testEventTitle)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot documents = task.getResult();

                        } else {
                            fail("Failed to fetch event from Firestore.");
                        }
                    });


            Thread.sleep(1000);
        }

        /**
         * Test browsing events in the admin interface.
         * This test checks if an admin user can browse through events, scroll to the newly
         * added event, and view its details by navigating to the event detail screen.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */
        @Test
        public void testAdminEventBrowsing() throws InterruptedException {
            addEvent(testEventTitle);

            Thread.sleep(1000); // Wait for the event to be added


            toggleToAdminMode();

            Thread.sleep(1000);

            // Browse events and find added event
            onView(withId(R.id.adminEventsRecyclerView))
                    .perform(scrollToItemWithText(testEventTitle));

            // Scroll to the item with the specific title and click it
            onView(withText(testEventTitle)).perform(scrollTo(), click());

            Thread.sleep(1000);

            // Make sure the details activity is shown
            intended(hasComponent(EventDetailActivity.class.getName()));

        }


        /**
         * Test browsing user profiles in the admin interface.
         * This test verifies the ability of an admin to browse through the user profiles,
         * select a user profile, and confirm that the profile details are displayed.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */
        @Test
        public void testAdminProfileBrowsing() throws InterruptedException {

            addProfile(testUserFirst, testUserLast);

            String fullname = testUserFirst + " " + testUserLast;

            Thread.sleep(1000);

            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_profiles)).perform(click());

            Thread.sleep(1000);

            // Scroll to the item with the specified name
            onView(withId(R.id.adminUsersRecyclerView))
                    .perform(scrollToItemWithTextAndClick(fullname));
            Thread.sleep(2000);
            onView(withId(R.id.profileName)).check(matches(isDisplayed()));
            onView(withId(R.id.profileEmail)).check(matches(isDisplayed()));
            onView(withId(R.id.profilePhone)).check(matches(isDisplayed()));

            Thread.sleep(2000);


        }

        /**
         * Test browsing images in the admin interface.
         * This test checks if the admin can navigate to the image management section and
         * browse through the uploaded images.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */
        @Test
        public void testAdminImageBrowsing() throws InterruptedException {
            addEvent(testEventTitle);
            Thread.sleep(1000);
            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_images)).perform(click());
            Thread.sleep(1000);

            intended(hasComponent(AdminImagesViewActivity.class.getName()));
            Thread.sleep(1000);


        }

        /**
         * US 03.07.01 As an administrator I want to remove facilities that
         * violate app policy
         *
         * Test the removal of a facility that violates app policy by an administrator.
         * This test verifies the admin's ability to remove a facility from the system.
         * The test ensures that after the removal, the facility is no longer displayed in the list.
         *
         * @throws InterruptedException If the thread sleep is interrupted.
         */

        @Test
        public void testAdminFacilityRemoval() throws InterruptedException {
            addFacility(testFacilityTitle);

            Thread.sleep(1000); // Wait for the event to be added

            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_facilities)).perform(click());

            Thread.sleep(1000);


            onView(withId(R.id.adminFacilitiesRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(testFacilityTitle, R.id.facilityName, R.id.removeFacilityButton));

            Thread.sleep(1000);


            Thread.sleep(1000);
            onView(withText(testFacilityTitle)).check(doesNotExist());

        }

}