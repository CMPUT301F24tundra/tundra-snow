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
import static androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import android.app.UiAutomation;
import android.util.Log;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
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
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


/**
 * Test class that has multiple test cases for Entrant User Story functionalities.
 */
public class EntrantTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String projectBasePath = System.getProperty("user.dir");
    private UiDevice mDevice;
    String permanentEvent = "Important Test Event";
    String permanentEventID = "ef375549-e7b5-4078-8d22-959be14937f0";
    String entrantUserId = "da0343cf-1173-41a7-a06e-dee3269f02a6";
    String testEntrantID = "2bbfb1db-d2d7-4941-a8c0-5e4a5ca30b8c";

    /**
     * Custom ViewAction to click on a specified ClickableSpan text inside a TextView.
     *
     * @param spanText The text of the ClickableSpan to click on.
     * @return A ViewAction that acts like a click on the specified ClickableSpan.
     */
    private static ViewAction clickClickableSpan(final String spanText) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(withId(R.id.signUpText));
            }

            @Override
            public String getDescription() {
                return "click on a specific ClickableSpan inside TextView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                TextView textView = (TextView) view;
                CharSequence text = textView.getText();
                if (text instanceof Spannable) {
                    Spannable spannable = (Spannable) text;
                    ClickableSpan[] spans = spannable.getSpans(0, text.length(), ClickableSpan.class);
                    for (ClickableSpan span : spans) {
                        int start = spannable.getSpanStart(span);
                        int end = spannable.getSpanEnd(span);
                        String spanString = text.subSequence(start, end).toString();
                        if (spanString.equals(spanText)) {
                            span.onClick(view);
                            break;
                        }
                    }
                }
            }
        };
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
                .whereEqualTo("title", permanentEvent)
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
                            if (entrantList.contains(testEntrantID)) {
                                entrantList.remove(testEntrantID);
                                updates.put("entrantList", entrantList);
                                needsUpdate = true;
                            }

                            // Add user to Chosen list if not present
                            if (!chosenList.contains(testEntrantID)) {
                                chosenList.add(testEntrantID);
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

    private void addUserToCancelledList(String userID, String EventName) {
        db.collection("events")
                .whereEqualTo("title", EventName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            List<String> cancelledList = (List<String>) document.get("cancelledList");

                            // Initialize list if it's null
                            if (cancelledList == null) cancelledList = new ArrayList<>();

                            // Check if user is already in the cancelled list
                            if (!cancelledList.contains(userID)) {
                                cancelledList.add(userID);
                                document.getReference().update("cancelledList", cancelledList)
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("UserManagement", "User added to cancelled list: " + userID))
                                        .addOnFailureListener(e ->
                                                Log.e("UserManagement", "Error adding user to cancelled list", e));
                            } else {
                                Log.d("UserManagement", "User already in cancelled list: " + userID);
                            }
                        }

                    } else {
                        Log.e("UserManagement", "Error finding events for user update", task.getException());
                    }
                });
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
        // This account is an only an Entrant
        onView(withId(R.id.usernameEditText)).perform(replaceText("111@gmail.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("111"));
        onView(withId(R.id.loginButton)).perform(click());
//        pushImageToDeviceAndScan(); Probems with ADB. have to do it manually for now
        clearUserNotifications(testEntrantID);
        Thread.sleep(1000);
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

    private void pushImageToDeviceAndScan() throws IOException, InterruptedException {
        String adbPath = "/Users/stro/Library/Android/sdk/platform-tools/adb";  // Path to adb
        String imagePath = "/Users/stro/AndroidStudioProjects/tundra-snow/tundrasnowapp/app/src/main/res/drawable/test_image.png";  // Path to the image
        String targetPath = "/sdcard/Pictures/";  // Target path on the device/emulator
        String adbPushCommand = adbPath + " push \"" + imagePath + "\" " + targetPath;
        Log.d("AdbTest", adbPushCommand);
        String mediaScanCommand = adbPath + " shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://" + targetPath + "test_image.png";
        executeCommand(adbPushCommand);
        executeCommand(mediaScanCommand);

    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        String[] commandArgs = command.split(" ");
        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
        processBuilder.inheritIO();  // Inherit the I/O streams for debugging/logging
        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Command executed successfully: " + command);
        } else {
            System.err.println("Command failed with exit code " + exitCode + ": " + command);
        }
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

    /**
     * Release Intents after tests are complete.
     */
    @After
    public void tearDown(){
        auth.signOut();
        Intents.release();

        cleanUser(permanentEvent, Boolean.FALSE);

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

    /**
     * US 01.01.01 As an entrant, I want to join the waiting list for a specific event
     * @throws InterruptedException
     */
    @Test
    public void testJoinWaitingList() throws InterruptedException {
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

        Thread.sleep(1000);

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
        onView(withId(R.id.profileEmail)).perform(replaceText("111@gmail.com"));
        onView(withId(R.id.profilePhone)).perform(replaceText("780-777-7777"));
        onView(withId(R.id.saveButton)).perform(click());


        Thread.sleep(1000);

        // Verify the reverted information is displayed correctly
        onView(withId(R.id.profileEmail)).check(matches(withText("111@gmail.com")));
        onView(withId(R.id.profilePhone)).check(matches(withText("780-777-7777")));
    }

    /**
     * TODO US 01.03.01 As an entrant I want to upload a profile picture for a more
     * personalized experience
     */
    @Test
    public void testProfilePictureUpload() throws InterruptedException, IOException {
        onView(withId(R.id.nav_profile)).perform(click());
/*
        // Step 2: Perform the click action on the "Change Picture" button
//        onView(withId(R.id.changePictureButton)).perform(click());

        // Step 3: Mock the result of the photo picker
        String imagePath = "content://media/picker/0/com.android.providers.media.photopicker/media/1000000023"; // change as needed
//        Uri imageUri = Uri.parse("file://" + imagePath);       // Convert path to a file URI
        Uri imageUri = Uri.parse(imagePath);  // Convert path to a file URI
//        // Create a mock result for the photo picker
//        Intent resultData = new Intent();
//        resultData.setData(imageUri);  // Simulate the selected image
//        Instrumentation.ActivityResult mockResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
//
//        // Mock the response for the photo picker intent
//        Intents.intending(hasAction(Intent.ACTION_PICK)).respondWith(mockResult);*/
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

    /**
     * TODO US 01.03.02 As an entrant I want remove profile picture if need be
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
     * TODO US 01.03.03 As an entrant I want my profile picture to be deterministically
     * generated from my profile name if I haven't uploaded a profile image yet.
     * @throws InterruptedException
     */
    @Test
    public void testAutomaticProfilePicture() throws InterruptedException {
        // Step 1: Navigate to the profile screen
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(1000);
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

        //Step 4: Ensure the profile picture has changed
        onView(withId(R.id.profileImageView))
                .check(matches(not(withDrawable(R.drawable.default_profile_picture))));
    }

    /**
     * TODO US 01.04.01 As an entrant I want to receive notification when chosen from the
     * waiting list (when I "win" the lottery)
     * @throws InterruptedException
     */
    @Test
    public void testLotteryWinNotification() throws InterruptedException {
        // Disable window and transition animations
        String testCase = "Test Event----";
        cleanUser(testCase, Boolean.TRUE);
        Thread.sleep(2000);
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 0.0");

        registerforTestEvent(testCase);
        logOutUser(Boolean.TRUE);


        onView(withId(R.id.menuButton)).perform(click());
        onView(withText("Organizer")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);
        onView(withText(testCase)).perform(click());
        onView(withId(R.id.viewWaitingList)).perform(scrollTo()).check(matches(isDisplayed())).perform(click());
            // Select User
        onView(withId(R.id.selectRegSampleButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.backButton)).perform(click());
        onView(withId(R.id.backButton)).perform(click());
        logOutUser(Boolean.FALSE);
        onView(withId(R.id.nav_events)).perform(click());
        // Step 3: Check Notifications
        Thread.sleep(3000);
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);

        onView(childAtPosition(withId(R.id.contentLayout), 0))
                .check(matches(hasDescendant(withText(containsString("Congratulations")))));

        cleanUser(testCase, Boolean.TRUE);
        Thread.sleep(2000);
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 1.0");
    }

    private void registerforTestEvent(String testCase) throws InterruptedException {
        // Step 1: Register For Test Event
        onView(withId(R.id.nav_settings)).perform(click());
        Thread.sleep(1000);
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
        onView(withId(R.id.nav_settings)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.logoutButton)).perform(click());
        onView(withText("Logout"))
                .check(matches(isDisplayed())); // Verify dialog is displayed
        onView(withText("Yes"))
                .perform(click()); // Click the positive button
        Thread.sleep(1000);
        if (isEntrant){
            onView(withId(R.id.usernameEditText)).perform(replaceText("admin@gmail.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("admin123"));
            onView(withId(R.id.loginButton)).perform(click());
        }else{

            onView(withId(R.id.usernameEditText)).perform(replaceText("111@gmail.com"));
            onView(withId(R.id.passwordEditText)).perform(replaceText("111"));
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

    /**
     * TODO US 01.04.02 As an entrant I want to receive notification of not chosen on the
     * app (when I "lose" the lottery)
     * @throws InterruptedException
     */
    @Test
    public void testLotteryLoseNotification() throws InterruptedException {
        // Disable window and transition animations
        String testCase = "Test Event----";
        resetCapacity(testCase);
        cleanUser(testCase, Boolean.TRUE);
        Thread.sleep(2000);
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 0.0");

        registerforTestEvent(testCase);
        logOutUser(Boolean.TRUE);
        // Login

        onView(withId(R.id.menuButton)).perform(click());
        onView(withText("Organizer")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(1000);
        onView(withText(testCase)).perform(click());
        onView(withId(R.id.viewWaitingList)).perform(scrollTo()).check(matches(isDisplayed())).perform(click());
        // Force User to loose
        onView(withId(R.id.editButton)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.maxParticipantEdit))
                .perform(scrollTo(), replaceText("-1"));
        onView(withId(R.id.saveButton)).perform(click());
        Thread.sleep(3000);
        onView(withId(R.id.selectRegSampleButton)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.backButton)).perform(click());
        onView(withId(R.id.backButton)).perform(click());
        logOutUser(Boolean.FALSE);
        onView(withId(R.id.nav_events)).perform(click());
        // Step 3: Check Notifications
        Thread.sleep(3000);
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);

        onView(childAtPosition(withId(R.id.contentLayout), 0))
                .check(matches(hasDescendant(withText(containsString("Unfortunately")))));

        cleanUser(testCase, Boolean.TRUE);
        resetCapacity(testCase);
        Thread.sleep(2000);
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 1.0");
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
     * TODO US 01.05.01 As an entrant I want another chance to be chosen from the waiting list
     * if a selected user declines an invitation to sign up
     * @throws InterruptedException
     */
    @Test
    public void testSigningUpAfterDeclined() throws InterruptedException {
        UiAutomation uiAutomation = getInstrumentation().getUiAutomation();
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 0.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 0.0");

        // Test Set up
        String testCase = "Test Event----";
        String adminUserId = "03e53a60-0e1e-4ea9-ad6f-38a3fea3cd6b";
        cleanUser(testCase, Boolean.TRUE);
        addUserToCancelledList(adminUserId, testCase);
        Thread.sleep(2000);
        // Step 1: Initial conditions - "Entrant User Registers, switch to organizer to simulate notif
        registerforTestEvent(testCase);
        logOutUser(Boolean.TRUE);

        onView(withId(R.id.menuButton)).perform(click());
        onView(withText("Organizer")).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.nav_my_events)).perform(click());
        Thread.sleep(2000);
        onView(withText(testCase)).perform(click());
        onView(withId(R.id.viewWaitingList)).perform(scrollTo()).check(matches(isDisplayed())).perform(click());
        // Select User
        Thread.sleep(3000);
        onView(withId(R.id.selectReplaceButton)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.backButton)).perform(click());
        onView(withId(R.id.backButton)).perform(click());
        logOutUser(Boolean.FALSE);
        onView(withId(R.id.nav_events)).perform(click());

        // Step 3: Check Notifications
        Thread.sleep(3000);
        onView(withId(R.id.notificationButton)).perform(click());
        Thread.sleep(2000);

        onView(childAtPosition(withId(R.id.contentLayout), 0))
                .check(matches(hasDescendant(withText(containsString("replacement")))));

        cleanUser(testCase, Boolean.TRUE);
        Thread.sleep(2000);
        uiAutomation.executeShellCommand("settings put global transition_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global window_animation_scale 1.0");
        uiAutomation.executeShellCommand("settings put global animator_duration_scale 1.0");
    }

    /**
     * US 01.05.02 As an entrant I want to be able to accept the invitation to register/sign
     * up when chosen to participate in an event
      * @throws InterruptedException
      */
    @Test
    public void testAcceptEventInvitation() throws InterruptedException {
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

        moveFromWaitlistToChosenList();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        onView(allOf(
                withId(R.id.eventName),
                withText(permanentEvent)
        )).perform(click());

        Thread.sleep(1000);

        // Accept Invitation (end up in confirmedList)
        onView(withId(R.id.rightButton)).perform(click());

        Thread.sleep(1000);

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

        moveFromWaitlistToChosenList();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        onView(allOf(
                withId(R.id.eventName),
                withText(permanentEvent)
        )).perform(click());

        Thread.sleep(1000);

        // Decline Invitation (end up in declinedList)
        onView(withId(R.id.leftButton)).perform(click());

        Thread.sleep(1000);

        onView(allOf(
                withId(R.id.eventStatus),
                withText("Declined.")
        )).check(matches(isDisplayed()));
    }

    /**
     * TODO US 01.06.01 As an entrant I want to view event details within the app by scanning
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
    /**
     * TODO US 01.06.02 As an entrant I want to be able to be sign up for an event by scanning the
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

        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.geolocationCheckbox)).check(matches(isDisplayed()));

        boolean isChecked = isGeolocationCheckedState();

        if (isChecked) {

            onView(withId(R.id.geolocationCheckbox)).perform(click());
            onView(withId(R.id.geolocationCheckbox)).check(matches(isNotChecked()));
        }

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(permanentEvent));
        onView(withText(permanentEvent)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        onView(withId(R.id.geoLocationNotification)).check(matches(isDisplayed()));
        onView(withId(R.id.geoLocationNotification)).check(matches(withText("Notice: Event Signup Requires Geolocation!")));
    }
}
