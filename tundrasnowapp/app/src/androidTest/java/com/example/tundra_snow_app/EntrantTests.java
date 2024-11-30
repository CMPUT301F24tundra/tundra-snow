package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import android.util.Log;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;

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
import android.util.Log;
import android.view.View;
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
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.matcher.IntentMatchers;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.example.tundra_snow_app.Activities.ProfileViewActivity;
import com.example.tundra_snow_app.Activities.SettingsViewActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

                            // Check if user is in any of the lists
                            boolean needsUpdate = false;
                            Map<String, Object> updates = new HashMap<>();

                            // Remove user from Entrant list (Waiting list) if present
                            if (entrantList != null && entrantList.contains(testEntrantID)) {
                                entrantList.remove(testEntrantID);
                                updates.put("entrantList", entrantList);
                                needsUpdate = true;
                            }

                            // Add user to Chosen list if not present
                            if (chosenList != null && !chosenList.contains(testEntrantID)) {
                                chosenList.add(testEntrantID);
                                updates.put("chosenList", chosenList);
                                needsUpdate = true;
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
     * Set up method that initializes Intents.
     */
    @Before
    public void setUp() throws InterruptedException,IOException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // This account is an only an Entrant
        onView(withId(R.id.usernameEditText)).perform(replaceText("111@gmail.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("111"));
        onView(withId(R.id.loginButton)).perform(click());
//        pushImageToDeviceAndScan(); Probems with ADB. have to do it manually for now

        Thread.sleep(1000);
    }

    public void pushImageToDeviceAndScan() throws IOException, InterruptedException {
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
    private void cleanUser() {
        // Query events collection to find events where this user is registered
        db.collection("events")
                .whereEqualTo("title", permanentEvent)
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

                            // Remove user from each list if present
                            if (entrantList != null && entrantList.contains(testEntrantID)) {
                                entrantList.remove(testEntrantID);
                                updates.put("entrantList", entrantList);
                                needsUpdate = true;
                            }
                            if (confirmedList != null && confirmedList.contains(testEntrantID)) {
                                confirmedList.remove(testEntrantID);
                                updates.put("confirmedList", confirmedList);
                                needsUpdate = true;
                            }
                            if (declinedList != null && declinedList.contains(testEntrantID)) {
                                declinedList.remove(testEntrantID);
                                updates.put("declinedList", declinedList);
                                needsUpdate = true;
                            }
                            if (cancelledList != null && cancelledList.contains(testEntrantID)) {
                                cancelledList.remove(testEntrantID);
                                updates.put("cancelledList", cancelledList);
                                needsUpdate = true;
                            }
                            if (chosenList != null && chosenList.contains(testEntrantID)) {
                                chosenList.remove(testEntrantID);
                                updates.put("chosenList", chosenList);
                                needsUpdate = true;
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

        cleanUser();
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
    public void testProfilePictureUpload() throws InterruptedException {
        onView(withId(R.id.nav_profile)).perform(click());

        // Step 2: Perform the click action on the "Change Picture" button
        onView(withId(R.id.changePictureButton)).perform(click());

        // Step 3: Mock the result of the photo picker
        String imagePath = "/sdcard/Download/test_image.png";  // Path to the image on the emulator
        Uri imageUri = Uri.parse("file://" + imagePath);       // Convert path to a file URI

        // Create a mock result for the photo picker
        Intent resultData = new Intent();
        resultData.setData(imageUri);  // Simulate the selected image
        Instrumentation.ActivityResult mockResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        // Mock the response for the photo picker intent
        Intents.intending(IntentMatchers.hasAction(Intent.ACTION_PICK)).respondWith(mockResult);

        // Step 4: Verify the result
        onView(withId(R.id.profileImageView))  // Assuming the image is displayed in this ImageView
                .check(matches(withImageUri(imageUri)));
    }

    private Matcher<? super View> withImageUri(Uri imageUri) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {

            @Override
            protected boolean matchesSafely(ImageView imageView) {
                // Get the drawable from the ImageView
                Drawable drawable = imageView.getDrawable();

                if (drawable == null) {
                    return false; // No image set in the ImageView
                }

                // Convert the URI to a bitmap for comparison
                Context context = imageView.getContext();
                Bitmap expectedBitmap = getBitmapFromUri(context, imageUri);

                if (expectedBitmap == null) {
                    return false; // Could not load the bitmap from the URI
                }

                // Convert the drawable in the ImageView to a bitmap
                Bitmap actualBitmap = getBitmapFromDrawable(drawable);

                // Compare the bitmaps
                return actualBitmap.sameAs(expectedBitmap);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with image URI: " + imageUri);
            }

            private Bitmap getBitmapFromUri(Context context, Uri uri) {
                try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
                    return BitmapFactory.decodeStream(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private Bitmap getBitmapFromDrawable(Drawable drawable) {
                if (drawable instanceof BitmapDrawable) {
                    return ((BitmapDrawable) drawable).getBitmap();
                }

                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );

                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);

                return bitmap;
            }
        };
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
    public void testProfilePictureRemoval() throws InterruptedException, UiObjectNotFoundException {
        // Step 1: Navigate to the profile screen
        onView(withId(R.id.nav_profile)).perform(click());
        Thread.sleep(1000); // Optional: Wait for the profile screen to load
        // Step 2: Set the profile picture
        onView(withId(R.id.generatePictureButton)).perform(click());
        Thread.sleep(3000);

        onView(withId(R.id.profileImageView))
                .check(matches(not(withDrawable(R.drawable.default_profile_picture))));

        // Step 3: Remove the profile picture
        onView(withId(R.id.removePictureButton)).perform(click())



        // Step 4: Verify that the profile picture is removed
        onView(withId(R.id.profileImageView))
                .check(matches(withDrawable(R.drawable.default_profile_picture)));
    }

    /**
     * TODO US 01.03.03 As an entrant I want my profile picture to be deterministically
     * generated from my profile name if I haven't uploaded a profile image yet.
     * @throws InterruptedException
     */
    @Test
    public void testAutomaticProfilePicture(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 01.04.01 As an entrant I want to receive notification when chosen from the
     * waiting list (when I "win" the lottery)
     * @throws InterruptedException
     */
    @Test
    public void testLotteryWinNotification(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 01.04.02 As an entrant I want to receive notification of not chosen on the
     * app (when I "lose" the lottery)
     * @throws InterruptedException
     */
    @Test
    public void testLotteryLoseNotification(){
        throw new UnsupportedOperationException("Not yet implemented");
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
    public void testSigningUpAfterDeclined(){
        throw new UnsupportedOperationException("Not yet implemented");
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
    public void testEventDetailsQrCode(){
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 01.06.02 As an entrant I want to be able to be sign up for an event by scanning the
     * QR code
     */
    @Test
    public void testEventSignUpQrCode(){
        throw new UnsupportedOperationException("Not yet implemented");
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
