package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;

import androidx.recyclerview.widget.RecyclerView;
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
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.allOf;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.espresso.util.HumanReadables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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

@RunWith(AndroidJUnit4.class)
public class AdminTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // Test data for database
    String testEventTitle = "Important Admin Test Event";
    String testFacilityTitle = "Test Facility Title";
    String testUserFirst = "Test";
    String testUserLast = "User";

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
        onView(withId(R.id.editTextLocation)).perform(replaceText("Test Location"));

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

        // Toggle geolocation requirement (default is Enabled, clicking makes it Disabled)
        onView(withId(R.id.toggleGeolocationRequirement)).perform(scrollTo(), click());

        // Create the event
        onView(withId(R.id.buttonCreateEvent)).perform(click());
        Thread.sleep(1000); // Allow time for event creation and database update

        generatedTitles.add(title);
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
     * TODO US 03.03.01 As an administrator, I want to be able to remove images.
     * @throws InterruptedException
     */
    @Test
    public void testAdminImageRemoval() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 03.03.02 As an administrator, I want to be able to remove hashed
     * QR code data
     * @throws InterruptedException
     */
    @Test
    public void testAdminHashDataRemoval() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 03.04.01 As an administrator, I want to be able to browse events.
     * @throws InterruptedException
     */
    @Test
    public void testAdminEventBrowsing() throws InterruptedException {
        addEvent(testEventTitle);

        Thread.sleep(1000); // Wait for the event to be added

        // TODO currently we have to toggle back to user if we want to be able to
        //  browse events and view additional details about an event. we should be able to
        //  click the event while browsing as an admin so we can view more details
        toggleToUserMode();

        Thread.sleep(1000);

        // Browse events and find added event
        onView(withId(R.id.eventsRecyclerView))
                .perform(scrollToItemWithText(testEventTitle));

        // Scroll to the item with the specific title and click it
        onView(withText(testEventTitle)).perform(click());

        Thread.sleep(1000);

        // Make sure the details activity is shown
        intended(hasComponent(EventDetailActivity.class.getName()));
    }


    /**
     * TODO US 03.05.01 As an administrator, I want to be able to browse profiles.
     * @throws InterruptedException
     */
    @Test
    public void testAdminProfileBrowsing() throws InterruptedException {
        // TODO currently we can browse few details but we should be able to click and
        //  view additional details like phone number and email address ( similar to an event details screen)
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * TODO US 03.06.01 As an administrator, I want to be able to browse images.
     * @throws InterruptedException
     */
    @Test
    public void testAdminImageBrowsing() throws InterruptedException {
        throw new UnsupportedOperationException("Not yet implemented");
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