    package com.example.tundra_snow_app;

    import static androidx.activity.result.ActivityResultCallerKt.registerForActivityResult;
    import static androidx.core.app.ActivityCompat.startActivityForResult;
    import static androidx.test.espresso.Espresso.onView;

    import androidx.activity.result.ActivityResultLauncher;
    import androidx.activity.result.PickVisualMediaRequest;
    import androidx.cardview.widget.CardView;
    import androidx.recyclerview.widget.RecyclerView;
    import androidx.test.core.app.ActivityScenario;
    import androidx.test.espresso.Espresso;
    import androidx.test.espresso.NoMatchingViewException;
    import androidx.test.espresso.PerformException;
    import androidx.test.espresso.UiController;
    import androidx.test.espresso.ViewAction;
    import androidx.test.espresso.ViewInteraction;
    import androidx.test.espresso.action.ViewActions;
    import androidx.test.espresso.intent.Intents;
    import static androidx.test.espresso.action.ViewActions.click;
    import static androidx.test.espresso.action.ViewActions.replaceText;
    import static androidx.test.espresso.action.ViewActions.scrollTo;
    import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
    import static androidx.test.espresso.assertion.ViewAssertions.matches;
    import static androidx.test.espresso.intent.Intents.intended;
    import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
    import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
    import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
    import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
    import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
    import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
    import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
    import static androidx.test.espresso.matcher.ViewMatchers.withId;
    import static androidx.test.espresso.matcher.ViewMatchers.withParent;
    import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
    import static androidx.test.espresso.matcher.ViewMatchers.withText;

    import androidx.test.espresso.matcher.ViewMatchers;
    import androidx.test.platform.app.InstrumentationRegistry;
    import androidx.activity.result.contract.ActivityResultContracts;
    import androidx.test.core.app.ActivityScenario;
    import static org.hamcrest.CoreMatchers.allOf;
    import static org.hamcrest.CoreMatchers.equalTo;
    import static org.junit.Assert.assertNull;
    import static org.junit.Assert.fail;

    import android.app.Activity;
    import android.app.Instrumentation;
    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.BitmapFactory;
    import android.net.Uri;
    import android.os.Environment;
    import android.os.SystemClock;
    import android.provider.MediaStore;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.test.espresso.intent.matcher.IntentMatchers;
    import androidx.test.espresso.util.HumanReadables;
    import androidx.test.ext.junit.rules.ActivityScenarioRule;
    import androidx.test.ext.junit.runners.AndroidJUnit4;

    import java.io.IOException;
    import java.util.List;
    import java.util.Random;
    import com.bumptech.glide.Glide;

    import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
    import com.google.firebase.Timestamp;
    import com.google.firebase.auth.FirebaseAuth;
    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.QuerySnapshot;
    import com.google.firebase.firestore.Source;
    import com.google.firebase.storage.FirebaseStorage;
    import com.google.firebase.storage.StorageReference;
    import com.google.firebase.storage.UploadTask;

    import org.hamcrest.Matcher;
    import org.junit.After;
    import org.junit.Before;
    import org.junit.Rule;
    import org.junit.Test;
    import org.junit.runner.RunWith;
    import org.junit.Assert.*;
    import static org.hamcrest.Matchers.is;
    import androidx.activity.result.ActivityResultLauncher;
    import androidx.test.core.app.ActivityScenario;
    import android.net.Uri;
    import org.junit.Test;

    import java.io.ByteArrayInputStream;
    import java.io.ByteArrayOutputStream;
    import java.io.InputStream;
    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.time.LocalDate;
    import java.util.Arrays;
    import java.util.HashMap;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.UUID;

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
            db.collection("users")
                    .whereEqualTo("firstName", testUserFirst)
                    .whereEqualTo("lastName", testUserLast)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot doc : task.getResult()) {
                                doc.getReference().delete()
                                        .addOnSuccessListener(aVoid -> Log.d("TearDown", "Test user deleted"))
                                        .addOnFailureListener(e -> Log.e("TearDown", "Error deleting user", e));
                            }
                        }
                    });

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


//             TODO
//            onView(withId(R.id.selectImageButton)).perform(scrollTo(), click());
//            String imagePath = "/0/Download/test_image.png";  // Path to the image on the emulator
//            Uri imageUri = Uri.parse("file://" + imagePath);       // Convert path to a file URI
//
//            // Create a mock result for the photo picker
//            Intent resultData = new Intent();
//            resultData.setData(imageUri);  // Simulate the selected image
//            Instrumentation.ActivityResult mockResult = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);


            // Toggle geolocation requirement (default is Enabled, clicking makes it Disabled)
            onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());
            // create QR hash info
            onView(withId(R.id.generateHashInformation)).perform(scrollTo(), click());
            // Create the event
            onView(withId(R.id.buttonCreateEvent)).perform(click());
            Thread.sleep(1000); // Allow time for event creation and database update
            generatedTitles.add(title);

    //        Map<String, Object> event = new HashMap<>();
    //        event.put("cancelledList", Arrays.asList()); // empty array
    //        event.put("capacity", 50);
    //        event.put("chosenList", Arrays.asList()); // empty array
    //        event.put("confirmedList", Arrays.asList()); // empty array
    //        event.put("declinedList", Arrays.asList()); // empty array
    //        event.put("description", "This is a description for the test event.");
    //        // Timestamps for dates
    //        Timestamp currentTimestamp = Timestamp.now();
    //        event.put("endDate", currentTimestamp);
    //        event.put("registrationEndDate", currentTimestamp);
    //        event.put("registrationStartDate", currentTimestamp);
    //        event.put("startDate", currentTimestamp);
    //        event.put("entrantList", Arrays.asList()); // empty array
    //        event.put("eventID", "00testEventId");
    //        FirebaseStorage storage = FirebaseStorage.getInstance();
    //
    //        String imagePath = "event_images/" + UUID.randomUUID() + ".jpg";
    //        StorageReference storageRef = storage.getReference(imagePath);
    //        String imageUrl = "https://a.espncdn.com/combiner/i?img=/i/headshots/nba/players/full/1966.png";
    //        StorageReference imageRef = storageRef.child(imagePath);
    //
    //
    //        storage.getReference(imagePath)
    //                .putFile(Uri.parse("https://a.espncdn.com/combiner/i?img=/i/headshots/nba/players/full/1966.png"))
    //                .addOnSuccessListener(taskSnapshot -> {
    //                    // Get the image URL and save it in the event details
    //                    storage.getReference(imagePath).getDownloadUrl().addOnSuccessListener(uri -> {
    //                        event.put("imageUrl", uri.toString());
    //                    });
    //                });
    //        event.put("facility", testFacilityTitle); // empty array
    //        event.put("geolocationRequirement", "Remote");
    //        event.put("location", "Test Location");
    //        event.put("organizer", "2af6bd2b-cf5d-452c-919c-520059e7670c");
    //        event.put("published", "yes");
    //        event.put("qrHash", "b096bb7a1c051fe080bb11012d6009cc9df2b7062c55eca3349b4642b3a5a8ad");
    //        event.put("status", "open");
    //        event.put("title", title);
    //        FirebaseFirestore.getInstance()
    //                .collection("events")
    //                .document("00testEventId")
    //                .set(event)
    //                .addOnSuccessListener(aVoid -> Log.d("AddEvent", "Event added: " + "206f230b-a1b5-4a3d-a887-9d8c3869a009"))
    //                .addOnFailureListener(e -> Log.e("AddEvent", "Error adding event", e));
        }
        public void deleteEvent() {
            db.collection("events").document("00testEventId").delete();
        }


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
         * @throws InterruptedException
         */
        @Test
        public void testAdminEventRemoval() throws InterruptedException {
            addEvent(testEventTitle);

            Thread.sleep(1000); // Wait for the event to be added

            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            // Scroll to the item with the specified event title
            onView(withId(R.id.adminEventsRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(testEventTitle, R.id.eventName, R.id.removeEventButton));


            Thread.sleep(1000);

            // Verify the event no longer exists
            Thread.sleep(1000); // Wait for the deletion to propagate
            onView(withText(testEventTitle)).check(doesNotExist());
        }

        /**
         * US 03.02.01 As an administrator, I want to be able to remove profiles.
         * @throws InterruptedException
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

            // Scroll to the item with the specified event title
            onView(withId(R.id.adminUsersRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(fullname, R.id.userFullName, R.id.removeUserButton));

            Thread.sleep(1000);

            // Verify the event no longer exists
            Thread.sleep(1000); // Wait for the deletion to propagate
            onView(withText(fullname)).check(doesNotExist());
        }

        /**
         *
         * @throws InterruptedException
         */
        @Test
        public void testAdminImageRemoval() throws InterruptedException {
            addEvent(testEventTitle);
            Thread.sleep(1000);
            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_images)).perform(click());
            Thread.sleep(1000);



        }

        /**
         * US 03.03.02 As an administrator, I want to be able to remove hashed
         * QR code data
         * @throws InterruptedException
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


            Thread.sleep(2000);
        }

        /**
         *
         * @throws InterruptedException
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
         *
         * @throws InterruptedException
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
         *
         * @throws InterruptedException
         */
        @Test
        public void testAdminImageBrowsing() throws InterruptedException {
            addEvent(testEventTitle);
            Thread.sleep(1000);
            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_images)).perform(click());
            Thread.sleep(1000);

        }

        /**
         * US 03.07.01 As an administrator I want to remove facilities that
         * violate app policy
         * @throws InterruptedException
         */
        @Test
        public void testAdminFacilityRemoval() throws InterruptedException {
            addFacility(testFacilityTitle);

            Thread.sleep(1000); // Wait for the event to be added

            toggleToAdminMode();

            Thread.sleep(2000); // Wait for the Admin mode to load

            onView(withId(R.id.admin_nav_facilities)).perform(click());

            Thread.sleep(1000);

            // Scroll to the item with the specified event title
            onView(withId(R.id.adminFacilitiesRecyclerView))
                    .perform(scrollToItemWithTextAndClickDelete(testFacilityTitle, R.id.facilityName, R.id.removeFacilityButton));

            Thread.sleep(1000);

            // Verify the event no longer exists
            Thread.sleep(1000); // Wait for the deletion to propagate
            onView(withText(testFacilityTitle)).check(doesNotExist());

        }

    }