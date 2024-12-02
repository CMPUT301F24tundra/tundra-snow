package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.UiAutomation;
import android.os.SystemClock;
import android.util.Log;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.example.tundra_snow_app.Activities.ProfileViewActivity;
import com.example.tundra_snow_app.Activities.QrScanActivity;
import com.example.tundra_snow_app.Activities.SettingsViewActivity;
import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Test class that has multiple test cases for Entrant User Story functionalities.
 */
public class EntrantTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private UiDevice mDevice;
    String permanentEvent = "Important Test Event";
    String permanentEventID = "ef375549-e7b5-4078-8d22-959be14937f0";
    String mainOrganizerId = "123";
    String testEntrantID = "2bbfb1db-d2d7-4941-a8c0-5e4a5ca30b8c";

    private String testUserId;  // Store the created user's ID
    private static final String TEST_USER_EMAIL = "testuser@example.com";
    private static final String TEST_USER_FIRST_NAME = "Test333";
    private static final String TEST_USER_LAST_NAME = "User333";

    String testEventTitle = "";

    Set<String> generatedTitles  = new HashSet<>();

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

    /**
     * Ensures that notifications and geolocation are enabled in settings
     * @throws InterruptedException
     */
    private void ensureSettingsEnabled() throws InterruptedException {
        onView(withId(R.id.nav_settings)).perform(click());
        Thread.sleep(1000);

        // Enable notifications if disabled
        boolean isNotificationsChecked = isNotificationsCheckedState();
        if (!isNotificationsChecked) {
            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isChecked()));
        }

        // Enable geolocation if disabled
        boolean isGeolocationChecked = isGeolocationCheckedState();
        if (!isGeolocationChecked) {
            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }

        // Return to main screen
        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(1000);
    }

    private String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%d",
                date.getDayOfMonth(),
                date.getMonthValue(),
                date.getYear());
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

    private void createTestEvent(String testEventTitle) throws InterruptedException {

        generatedTitles.add(testEventTitle);

        // Switch to organizer mode before creating event
        toggleToOrganizerMode();

        Thread.sleep(2000);

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
        onView(withId(R.id.editTextCapacity)).perform(scrollTo(), replaceText("1"));

        // Toggle geolocation requirement (default is Enabled, clicking makes it Disabled)
        onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());

        onView(withId(R.id.generateHashInformation)).perform(scrollTo(), click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(2000); // Allow time for event creation and database update

        toggleToUserMode();
        Thread.sleep(1000);
    }

    /**
     *  Method to determine the current state of the notifications checkbox.
     *
     * @return true if the checkbox is checked, false otherwise.
     */
    private boolean isNotificationsCheckedState() {
        final boolean[] isChecked = {false};  // Default to false

        // Check if the checkbox is currently checked
        onView(withId(R.id.notificationsCheckbox)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (view instanceof Checkable) {
                    isChecked[0] = ((Checkable) view).isChecked();
                }
            }
        });

        return isChecked[0];
    }

    /**
     *  Method to determine the current state of the geolocation checkbox.
     *
     * @return true if the checkbox is checked, false otherwise.
     */
    private boolean isGeolocationCheckedState() {
        final boolean[] isChecked = {false};  // Default to false

        // Check if the checkbox is currently checked
        onView(withId(R.id.geolocationCheckbox)).check(new ViewAssertion() {
            @Override
            public void check(View view, NoMatchingViewException noViewFoundException) {
                if (view instanceof Checkable) {
                    isChecked[0] = ((Checkable) view).isChecked();
                }
            }
        });

        return isChecked[0];
    }

    /**
     * Method to move a user from the waitingList to the chosenList
     */
    private void moveFromWaitlistToChosenList(){
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> entrantList = (List<String>) document.get("entrantList");
                            List<String> chosenList = (List<String>) document.get("chosenList");

                            // Initialize lists if they are null
                            if (entrantList == null) entrantList = new ArrayList<>();
                            if (chosenList == null) chosenList = new ArrayList<>();

                            // Check if user is in any of the lists
                            boolean needsUpdate = false;
                            Map<String, Object> updates = new HashMap<>();

                            // Remove user from Entrant list (Waiting list) if present
                            if (entrantList.contains(mainOrganizerId)) {
                                entrantList.remove(mainOrganizerId);
                                updates.put("entrantList", entrantList);
                                needsUpdate = true;
                            }

                            // Add user to Chosen list if not present
                            if (!chosenList.contains(mainOrganizerId)) {
                                chosenList.add(mainOrganizerId);
                                updates.put("chosenList", chosenList);
                                needsUpdate = true;
                            }

                            // If user was found in any list, update the document
                            if (needsUpdate) {
                                document.getReference().update(updates)
                                        .addOnSuccessListener(aVoid -> Log.d("TearDown", "Removed user from event " + document.getId()))
                                        .addOnFailureListener(e -> Log.e("TearDown", "Error removing user from event " + document.getId(), e));
                            }
                        }

                    } else {
                        Log.e("TearDown", "Error finding events for user cleanup", task.getException());
                    }
                });

    }

    public void clearUserNotifications(String userID) {
        // Get a reference to the notifications collection
        CollectionReference notificationsRef = db.collection("notifications");

        // Query to find all notifications where the userID is present in the "userIDs" field
        notificationsRef.whereArrayContains("userIDs", userID)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the notification ID
                            String notificationID = document.getId();

                            // Remove the user from the "userIDs" field
                            List<String> userIDs = (List<String>) document.get("userIDs");
                            if (userIDs != null && userIDs.contains(userID)) {
                                userIDs.remove(userID);
                            }

                            // Update the notification to reflect the removal
                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("userIDs", userIDs);

                            // Save the updated notification document
                            document.getReference().update(updateData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("Notifications", "User " + userID + " cleared from notification: " + notificationID);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Notifications", "Failed to clear user " + userID + " from notification " + notificationID, e);
                                    });
                        }
                    } else {
                        Log.d("Notifications", "No notifications found for user " + userID);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Notifications", "Failed to retrieve notifications for user " + userID, e);
                });
    }

    /**
     * Helper method to clean up test user's participation in events
     */
    private void cleanUser(String eventTitle, Boolean cleanALL) {
        // Query events collection to find events where this user is registered
        db.collection("events")
                .whereEqualTo("title", eventTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (DocumentSnapshot document : task.getResult()) {
                            // Get all the lists that might contain the user
                            List<String> entrantList = (List<String>) document.get("entrantList");
                            List<String> confirmedList = (List<String>) document.get("confirmedList");
                            List<String> declinedList = (List<String>) document.get("declinedList");
                            List<String> cancelledList = (List<String>) document.get("cancelledList");
                            List<String> chosenList = (List<String>) document.get("chosenList");

                            // Check if user is in any of the lists
                            boolean needsUpdate = false;
                            Map<String, Object> updates = new HashMap<>();
                            if(cleanALL){
                                chosenList.clear();
                                updates.put("chosenList", chosenList);
                                cancelledList.clear();
                                updates.put("cancelledList", cancelledList);
                                declinedList.clear();
                                updates.put("declinedList", declinedList);
                                confirmedList.clear();
                                updates.put("confirmedList", confirmedList);
                                entrantList.clear();
                                updates.put("entrantList", entrantList);
                                needsUpdate = true;
                            }else {
                                // Remove user from each list if present
                                if ((entrantList != null && entrantList.contains(testEntrantID))) {
                                    entrantList.remove(testEntrantID);
                                    updates.put("entrantList", entrantList);
                                    needsUpdate = true;
                                }
                                if ((confirmedList != null && confirmedList.contains(testEntrantID))) {
                                    confirmedList.remove(testEntrantID);
                                    updates.put("confirmedList", confirmedList);
                                    needsUpdate = true;
                                }
                                if ((declinedList != null && declinedList.contains(testEntrantID))) {
                                    declinedList.remove(testEntrantID);
                                    updates.put("declinedList", declinedList);
                                    needsUpdate = true;
                                }
                                if ((cancelledList != null && cancelledList.contains(testEntrantID))) {
                                    cancelledList.remove(testEntrantID);
                                    updates.put("cancelledList", cancelledList);
                                    needsUpdate = true;
                                }
                                if ((chosenList != null && chosenList.contains(testEntrantID))) {
                                    chosenList.remove(testEntrantID);
                                    updates.put("chosenList", chosenList);
                                    needsUpdate = true;
                                }
                            }
                            // If user was found in any list, update the document
                            if (needsUpdate) {
                                document.getReference().update(updates)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("TearDown", "Removed user from event " + document.getId()))
                                        .addOnFailureListener(e ->
                                                Log.e("TearDown", "Error removing user from event " + document.getId(), e));
                            }
                        }
                    } else {
                        Log.e("TearDown", "Error finding events for user cleanup", task.getException());
                    }
                });
    }

    public Uri saveBitmapToFile(Bitmap bitmap, Context context) throws IOException {
        // Create a file in the app's cache directory
        File file = new File(context.getCacheDir(), "test_image.png");

        // Create an output stream to write the Bitmap to the file
        FileOutputStream outStream = new FileOutputStream(file);

        // Compress the bitmap into PNG format and save it to the file
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.close(); // Don't forget to close the output stream

        // Return a URI pointing to the saved file
        return Uri.fromFile(file);
    }

    private Matcher<? super View> withDrawable(int testImage) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            protected boolean matchesSafely(ImageView imageView) {
                // Get the drawable from the ImageView
                Drawable drawable = imageView.getDrawable();

                // Check if the drawable is not null and if it matches the expected drawable
                if (drawable != null) {
                    Drawable expectedDrawable = ContextCompat.getDrawable(imageView.getContext(), testImage);  // Using ContextCompat to load drawable
                    return drawable.getConstantState().equals(expectedDrawable.getConstantState());  // Compare the constant state
                }

                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with drawable resource ID: " + testImage);
            }
        };
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

    public Activity getCurrentActivity() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            Collection<Activity> resumedActivities = ActivityLifecycleMonitorRegistry.getInstance()
                    .getActivitiesInStage(Stage.RESUMED);
            if (!resumedActivities.isEmpty()) {
                currentActivity[0] = resumedActivities.iterator().next();
            }
        });
        return currentActivity[0];
    }

    private String fetchQrHash(String eventId) {
        // Create a Task to hold the result of the Firestore query
        Task<DocumentSnapshot> task = db.collection("events")
                .document(eventId)
                .get();

        try {
            // Block the thread until the task completes, making the operation synchronous
            DocumentSnapshot documentSnapshot = Tasks.await(task);

            // Check if the document exists and retrieve the qrHash
            if (documentSnapshot.exists()) {
                return documentSnapshot.getString("qrHash");
            } else {
                Log.e("Firestore", "Event not found: " + eventId);
                return null;
            }
        } catch (Exception e) {
            Log.e("Firestore", "Error fetching event", e);
            return null;
        }
    }

    private void registerforTestEvent(String testCase) throws InterruptedException {
        // Step 1: Register For Test Event
        onView(withId(R.id.nav_settings)).perform(click());
        Thread.sleep(3000);
        intended(hasComponent(SettingsViewActivity.class.getName()));

        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));
        boolean isChecked = isGeolocationCheckedState();

        if (!isChecked) {
            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }
        isChecked = isNotificationsCheckedState();
        if (!isChecked) {
            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testCase));
        onView(withText(testCase)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.buttonSignUpForEvent)).perform(click());
    }

    private void logOutUser(Boolean isEntrant) throws InterruptedException {
        Thread.sleep(3000);

        onView(withId(R.id.nav_settings)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.logoutButton)).perform(click());
        onView(withText("Logout"))
                .check(matches(isDisplayed())); // Verify dialog is displayed
        onView(withText("Yes"))
                .perform(click()); // Click the positive button
        Thread.sleep(3000);
        if (isEntrant){
            onView(withId(R.id.usernameEditText)).perform(replaceText("admin@gmail.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("admin123"));
            onView(withId(R.id.loginButton)).perform(click());
        }else{

            onView(withId(R.id.usernameEditText)).perform(replaceText("333@gmail.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("333"));
            onView(withId(R.id.loginButton)).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.nav_settings)).perform(click());
            Thread.sleep(2000);
            onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));
            boolean isChecked = isGeolocationCheckedState();
            if (!isChecked) {
                onView(withId(R.id.geolocationCheckbox)).perform(click());
                onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
            }
            isChecked = isNotificationsCheckedState();
            if (!isChecked) {
                onView(withId(R.id.notificationsCheckbox)).perform(click());
                onView(withId(R.id.notificationsCheckbox)).check(matches(isChecked()));
            }

        }
        Thread.sleep(2000);
    }

    public static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    private void resetCapacity(String eventName){
        db.collection("events")
                .whereEqualTo("title", eventName)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        document.getReference().update("capacity", 50);
                    }
                });
    }

    private void createTestUserAndAddToWaitlist(String eventId) throws InterruptedException {
        testUserId = UUID.randomUUID().toString();

        // Create user document
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userID", testUserId);
        userMap.put("firstName", TEST_USER_FIRST_NAME);
        userMap.put("lastName", TEST_USER_LAST_NAME);
        userMap.put("email", TEST_USER_EMAIL);
        userMap.put("password", "testpass");
        userMap.put("dateOfBirth", "01/01/2000");
        userMap.put("phoneNumber", "1234567890");
        userMap.put("notificationsEnabled", true);
        userMap.put("geolocationEnabled", false);
        userMap.put("deviceID", "test_device");
        userMap.put("location", "Test Location");
        userMap.put("roles", Arrays.asList("user"));
        userMap.put("userEventList", new ArrayList<>());

        // Add user to Firestore
        final CountDownLatch userLatch = new CountDownLatch(1);
        db.collection("users")
                .document(testUserId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> userLatch.countDown())
                .addOnFailureListener(e -> userLatch.countDown());

        assertTrue("Creating test user timed out", userLatch.await(5, TimeUnit.SECONDS));

        // Add test user to event's waiting list
        final CountDownLatch waitlistLatch = new CountDownLatch(1);
        db.collection("events")
                .document(eventId)
                .update("entrantList", FieldValue.arrayUnion(testUserId))
                .addOnSuccessListener(aVoid -> waitlistLatch.countDown())
                .addOnFailureListener(e -> waitlistLatch.countDown());

        assertTrue("Adding test user to waitlist timed out", waitlistLatch.await(5, TimeUnit.SECONDS));
        Thread.sleep(1000);
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

    private void createMainOrganizerIfNotExists() {
        // Create user document
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userID", mainOrganizerId);  // Using the existing entrantUserId constant
        userMap.put("firstName", "Main");
        userMap.put("lastName", "Organizer");
        userMap.put("email", "333@gmail.com");
        userMap.put("password", "333");
        userMap.put("dateOfBirth", "01/01/2000");
        userMap.put("phoneNumber", "1234567890");
        userMap.put("notificationsEnabled", true);
        userMap.put("geolocationEnabled", true);
        userMap.put("deviceID", "test_device");
        userMap.put("location", "Test Location");
        userMap.put("roles", Arrays.asList("user", "organizer"));
        userMap.put("userEventList", new ArrayList<>());
        userMap.put("facilityList", new ArrayList<>());
        userMap.put("organizerEventList", new ArrayList<>());

        final CountDownLatch latch = new CountDownLatch(1);

        // First check if user exists
        db.collection("users")
                .document(mainOrganizerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().exists()) {
                        // User doesn't exist, create it
                        db.collection("users")
                                .document(mainOrganizerId)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Setup", "Main organizer account created successfully");
                                    latch.countDown();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Setup", "Error creating main organizer account", e);
                                    latch.countDown();
                                });
                    } else {
                        // User already exists
                        Log.d("Setup", "Main organizer account already exists");
                        latch.countDown();
                    }
                });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e("Setup", "Timeout waiting for main organizer account creation", e);
        }
    }

    /**
     * Set up method that initializes Intents.
     */
    @Before
    public void setUp() throws InterruptedException,IOException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Create main organizer account (333)
        createMainOrganizerIfNotExists();

        Thread.sleep(1000);

        // This account is an Entrant (and Organizer)
        onView(withId(R.id.usernameEditText)).perform(replaceText("333@gmail.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("333"));
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(1000);
    }

    /**
     * Release Intents after tests are complete.
     */
    @After
    public void tearDown(){
        auth.signOut();
        Intents.release();

        // cleanUser(permanentEvent, Boolean.FALSE);

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

        // Clean up test user if one was created
        db.collection("users")
                .whereEqualTo("email", TEST_USER_EMAIL)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot) {
                        String email = document.getString("email");
                        if (email != null && !email.equals("333@gmail.com")) {  // Protect the test account
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> Log.d("TearDown", "Test user deleted successfully"))
                                    .addOnFailureListener(e -> Log.e("TearDown", "Error deleting test user", e));
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e("TearDown", "Error finding test user", e));
    }

    /**
     * US 01.01.01 As an entrant, I want to join the waiting list for a specific event
     * @throws InterruptedException
     */
    @Test
    public void testJoinWaitingList() throws InterruptedException {
        createTestEvent(permanentEvent);

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (!isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(permanentEvent));
        onView(withText(permanentEvent)).perform(click());

        Thread.sleep(3000);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        Thread.sleep(4000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        onView(allOf(
                withId(R.id.eventName),
                withText(permanentEvent)
        )).check(matches(isDisplayed()));
    }

    /**
     * US 01.01.02 As an entrant, I want to leave the waiting list for a specific event
     * @throws InterruptedException
     */
    @Test
    public void testLeaveWaitingList() throws InterruptedException {

        createTestEvent(permanentEvent);

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (!isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(permanentEvent));
        onView(withText(permanentEvent)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(4000);

        onView(allOf(
                withId(R.id.eventName),
                withText(permanentEvent)
        )).perform(click());

        Thread.sleep(1000);

        // Remove self from waitlist (end up in cancelledList)
        onView(withId(R.id.rightButton)).perform(click());

        Thread.sleep(1000);

        onView(allOf(
                withId(R.id.eventStatus),
                withText("Cancelled.")
        )).check(matches(isDisplayed()));
    }

    /**
     * US 01.02.02 As an entrant I want to update information such as name,
     * email and contact information on my profile
     * @throws InterruptedException
     */
    @Test
    public void testUpdateProfileInformation() throws InterruptedException {

        onView(withId(R.id.nav_profile)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(ProfileViewActivity.class.getName()));

        // Update email and phone number
        onView(withId(R.id.editButton)).perform(click());

        onView(withId(R.id.profileEmail)).perform(replaceText("updated.email@example.com"));
        onView(withId(R.id.profilePhone)).perform(replaceText("123-456-7890"));


        onView(withId(R.id.saveButton)).perform(click());


        Thread.sleep(1000);

        // Ensure the fields were updated
        onView(withId(R.id.profileEmail)).check(matches(withText("updated.email@example.com")));
        onView(withId(R.id.profilePhone)).check(matches(withText("123-456-7890")));


        // Revert the email and phone number
        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.profileEmail)).perform(replaceText("333@gmail.com"));
        onView(withId(R.id.profilePhone)).perform(replaceText("780-777-7777"));
        onView(withId(R.id.saveButton)).perform(click());


        Thread.sleep(1000);

        // Verify the reverted information is displayed correctly
        onView(withId(R.id.profileEmail)).check(matches(withText("333@gmail.com")));
        onView(withId(R.id.profilePhone)).check(matches(withText("780-777-7777")));
    }

    /**
     * US 01.03.01 As an entrant I want to upload a profile picture for a more
     * personalized experience
     */
    @Test
    public void testProfilePictureUpload() throws InterruptedException, IOException {
        onView(withId(R.id.nav_profile)).perform(click());

        ProfileViewActivity activity = (ProfileViewActivity) getCurrentActivity();
        Drawable drawable = activity.getResources().getDrawable(R.drawable.test_image, null);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

        // Save the Bitmap to a file and get the Uri
        Uri imageUri = saveBitmapToFile(bitmap, getInstrumentation().getTargetContext());

        // Call your simulateProfileUpload method with the Uri
        activity.simulateProfileUpload(imageUri);
        Thread.sleep(3000);
        onView(withId(R.id.profileImageView))
                .check(matches(withTagValue(equalTo(imageUri.toString())))); // Check if the tag matches
    }

    /**
     * US 01.03.02 As an entrant I want remove profile picture if need be
     * @throws InterruptedException
     */
    @Test
    public void testProfilePictureRemoval() throws InterruptedException {
        // Step 1: Navigate to the profile screen
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(1000);
        // Step 2: Set the profile picture
        onView(withId(R.id.generatePictureButton)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.profileImageView))
                .check(matches(not(withDrawable(R.drawable.default_profile_picture))));

        // Step 3: Remove the profile picture
        onView(withId(R.id.removePictureButton)).perform(click());

        onView(withText("Remove Profile Picture"))
                .check(matches(isDisplayed())); // Verify dialog is displayed

        onView(withText("Yes"))
                .perform(click()); // Click the positive button

        // Step 4: Verify that the profile picture is removed
        Thread.sleep(3000);
        onView(withId(R.id.profileImageView))
                .check(matches(withDrawable(R.drawable.default_profile_picture)));
    }

    /**
     * US 01.03.03 As an entrant I want my profile picture to be deterministically
     * generated from my profile name if I haven't uploaded a profile image yet.
     * @throws InterruptedException
     */
    @Test
    public void testAutomaticProfilePicture() throws InterruptedException {
        // Step 1: Navigate to the profile screen
        onView(withId(R.id.nav_profile)).perform(click());

        Thread.sleep(3000);
        // Step 2: Ensure the default profile picture
        boolean isDefaultProfilePicture;
        try {
            onView(withId(R.id.profileImageView))
                    .check(matches(withDrawable(R.drawable.default_profile_picture)));
            isDefaultProfilePicture = true; // If no exception, the condition matched
        } catch (AssertionError e) {
            isDefaultProfilePicture = false; // The condition did not match
        }
        Thread.sleep(2000);
        // Use the result in your if statement
        if(isDefaultProfilePicture) {
            onView(withId(R.id.removePictureButton)).perform(click());

            onView(withText("Remove Profile Picture"))
                    .check(matches(isDisplayed())); // Verify dialog is displayed

            onView(withText("Yes"))
                    .perform(click()); // Click the positive button
            onView(withId(R.id.profileImageView))
                    .check(matches(withDrawable(R.drawable.default_profile_picture)));
        }
        Thread.sleep(2000);

        // Step 3: Set the profile picture
        onView(withId(R.id.generatePictureButton)).perform(click());

        Thread.sleep(3000);

        //Step 4: Ensure the profile picture has changed
        onView(withId(R.id.profileImageView))
                .check(matches(not(withDrawable(R.drawable.default_profile_picture))));
    }

    /**
     * US 01.04.03 As an entrant I want to opt out of receiving notifications from
     * organizers and admin
     * @throws InterruptedException
     */
    @Test
    public void testUpdatingNotifications() throws InterruptedException {

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.notificationsCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isNotificationsCheckedState();

        if (isChecked) {

            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isNotChecked()));
        } else {

            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isChecked()));
        }
    }

    /**
     * US 01.05.01 As an entrant I want another chance to be chosen from the waiting list
     * if a selected user declines an invitation to sign up
     * @throws InterruptedException
     */
    @Test
    public void testSigningUpAfterDeclined() throws InterruptedException {
        // Create test event with capacity
        testEventTitle = "Drawing Replacement Test Event";
        createTestEvent(testEventTitle);
        Thread.sleep(1000);

        clearUserNotifications(mainOrganizerId);

        Thread.sleep(500);

        // Get event ID and current user ID
        final String[] eventId = {null};
        final String[] currentUserId = {null};
        final CountDownLatch idLatch = new CountDownLatch(1);

        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        eventId[0] = queryDocumentSnapshots.getDocuments().get(0).getId();
                        currentUserId[0] = queryDocumentSnapshots.getDocuments().get(0).getString("organizer");
                    }
                    idLatch.countDown();
                });

        assertTrue("Getting event ID timed out", idLatch.await(5, TimeUnit.SECONDS));

        // Create test user and add both users to waitlist
        createTestUserAndAddToWaitlist(eventId[0]);
        addUserToWaitingList(eventId[0], currentUserId[0]);
        Thread.sleep(2000);

        // Switch to organizer mode and navigate to event
        toggleToOrganizerMode();
        Thread.sleep(2000);

        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(2000);

        onView(withText(testEventTitle)).perform(scrollTo(), click());
        Thread.sleep(1000);

        // View waiting list and select initial entrant
        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());
        Thread.sleep(1000);

        // Select user from waiting list
        onView(withId(R.id.selectRegSampleButton)).perform(scrollTo(), click());
        Thread.sleep(2000);

        // Navigate to chosen list and decline the selected user
        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.viewChosenList)).perform(scrollTo(), click());
        Thread.sleep(1000);

        onView(withId(R.id.cancelUserButton)).perform(scrollTo(), click());
        Thread.sleep(2000);

        // Draw replacement from remaining waitlist
        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(1000);

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());
        Thread.sleep(2000);

        // Verify replace button is available and click it
        onView(withId(R.id.selectReplaceButton))
                .check(matches(isDisplayed()))
                .perform(click());
        Thread.sleep(2000);

        // Return to main screen
        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.backButton)).perform(click());
        Thread.sleep(1500);

        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(1500);

        // Switch to user mode and check notification
        toggleToUserMode();
        Thread.sleep(2000);

        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);


        // Verify notification with either replacement or selected text
        onView(childAtPosition(withId(R.id.contentLayout), 0))
                .check(matches(either(
                        hasDescendant(withText(containsString("replacement")))
                ).or(
                        hasDescendant(withText(containsString("selected")))
                )));

        Thread.sleep(1000);

        clearUserNotifications(mainOrganizerId);
    }

    /**
     * US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign
     * up when chosen to participate in an event
      * @throws InterruptedException
      */
    @Test
    public void testAcceptEventInvitation() throws InterruptedException {

        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Entrant Test Event " + randomNumber;

        createTestEvent(testEventTitle);

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1500);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (!isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testEventTitle));
        onView(withText(testEventTitle)).perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        moveFromWaitlistToChosenList();

        Thread.sleep(4000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(3000);

        onView(allOf(
                withId(R.id.eventName),
                withText(testEventTitle)
        )).perform(click());

        Thread.sleep(2000);

        // Accept Invitation (end up in confirmedList)
        onView(withId(R.id.rightButton)).perform(click());

        Thread.sleep(2000);

        onView(allOf(
                withId(R.id.eventStatus),
                withText("Accepted!")
        )).check(matches(isDisplayed()));
    }

    /**
     * US 01.05.03 As an entrant I want to be able to decline an invitation when chosen to
     * participate in an event
     */
    @Test
    public void testDeclineEventInvitation() throws InterruptedException {

        // Generate a random event title to ensure uniqueness
        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Entrant Test Event " + randomNumber;

        createTestEvent(testEventTitle);

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1500);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (!isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testEventTitle));
        onView(withText(testEventTitle)).perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        moveFromWaitlistToChosenList();

        Thread.sleep(4000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(3000);

        onView(allOf(
                withId(R.id.eventName),
                withText(testEventTitle)
        )).perform(click());

        Thread.sleep(2000);

        // Decline Invitation (end up in cancelledList)
        onView(withId(R.id.middleButton)).perform(click());

        Thread.sleep(2000);

        onView(allOf(
                withId(R.id.eventStatus),
                withText("Cancelled.")
        )).check(matches(isDisplayed()));
    }

    /**
     * US 01.06.01 As an entrant I want to view event details within the app by scanning
     * the promotional QR code
     */
    @Test
    public void testEventDetailsQrCode() throws InterruptedException, UiObjectNotFoundException {
        // Fetch the current context of the running app
        cleanUser("Test Event----", Boolean.FALSE);
        // Step 1: Fetch the current context of the running app
        Activity currentActivity = getCurrentActivity(); // Custom method to retrieve the current activity
        assertTrue(currentActivity instanceof EventViewActivity);

        // Step 2: Fetch the QR hash
        String qrHash = fetchQrHash("7e4689b8-53c0-4ff9-8296-df1f63e44f17");

        if (qrHash != null) {
            // Step 3: Create an Intent to navigate to QrScanActivity
            Intent intent = new Intent(currentActivity, QrScanActivity.class);
            intent.putExtra("qrHash", qrHash);

            currentActivity.startActivity(intent);
            Thread.sleep(5000);
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            device.waitForIdle();
            UiObject allowButton = device.findObject(new UiSelector().text("While using the app"));
            if (allowButton.exists()) {
                allowButton.click();
            }
            // Check the Intent of the currently running activity (QrScanActivity)
            QrScanActivity qrScanActivity = (QrScanActivity)getCurrentActivity();
            Intent launchedIntent = qrScanActivity.getIntent();
            assertNotNull("Launched Intent is null", launchedIntent); // Ensure the intent is not null
            assertEquals("Expected activity is not launched", QrScanActivity.class.getName(), launchedIntent.getComponent().getClassName());
            assertEquals("QR hash mismatch", qrHash, launchedIntent.getStringExtra("qrHash"));
            // Step4: SimulateScan
            qrScanActivity.simulateScan(qrHash); // Pass qrHash to simulate the scan
            Thread.sleep(5000);
            onView(withText("Test Event----")).check(matches(isDisplayed()));
        } else {
            throw new RuntimeException("QR Code not found for this event");
        }
        cleanUser("Test Event----", Boolean.FALSE);
    }

    /**
     * US 01.06.02 As an entrant I want to be able to be sign up for an event by scanning the
     * QR code
     */
    @Test
    public void testEventSignUpQrCode() throws InterruptedException, UiObjectNotFoundException {
        // Step 1: Fetch the current context of the running app
        Activity currentActivity = getCurrentActivity(); // Custom method to retrieve the current activity
        assertTrue(currentActivity instanceof EventViewActivity);

        // Step 2: Fetch the QR hash
        String qrHash = fetchQrHash("7e4689b8-53c0-4ff9-8296-df1f63e44f17");

        if (qrHash != null) {
            // Step 3: Create an Intent to navigate to QrScanActivity
            Intent intent = new Intent(currentActivity, QrScanActivity.class);
            intent.putExtra("qrHash", qrHash);

            currentActivity.startActivity(intent);
            Thread.sleep(5000);
            UiDevice device = UiDevice.getInstance(getInstrumentation());
            device.waitForIdle();
            UiObject allowButton = device.findObject(new UiSelector().text("While using the app"));
            if (allowButton.exists()) {
                allowButton.click();
            }
            // Check the Intent of the currently running activity (QrScanActivity)
            QrScanActivity qrScanActivity = (QrScanActivity)getCurrentActivity();
            Intent launchedIntent = qrScanActivity.getIntent();
            assertNotNull("Launched Intent is null", launchedIntent); // Ensure the intent is not null
            assertEquals("Expected activity is not launched", QrScanActivity.class.getName(), launchedIntent.getComponent().getClassName());
            assertEquals("QR hash mismatch", qrHash, launchedIntent.getStringExtra("qrHash"));
            // Step4: SimulateScan
            qrScanActivity.simulateScan(qrHash); // Pass qrHash to simulate the scan
            Thread.sleep(5000);
            onView(withText(containsString("successfully signed up"))).check(matches(isDisplayed()));
        } else {
            throw new RuntimeException("QR Code not found for this event");
        }
    }

    /**
     * US 01.08.01 As an entrant, I want to be warned before joining a waiting list that it requires geolocation.

     * Test to verify that warning is shown before joining waiting list that has geolocation.
     * @throws InterruptedException
     */
    @Test
    public void testGeolocationWarning() throws InterruptedException {

        createTestEvent(permanentEvent);

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1500);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isNotChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1500);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(permanentEvent));
        onView(withText(permanentEvent)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.geoLocationNotification)).check(matches(isDisplayed()));
        onView(withId(R.id.geoLocationNotification)).check(matches(withText("Notice: Event Signup Requires Geolocation!")));
    }
}
