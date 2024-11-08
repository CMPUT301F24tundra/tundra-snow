package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

public class OrganizerTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String testEventTitle = "";
    String permanentEvent = "Permanent Test Event";
    static int randomNumber = new Random().nextInt(1000);

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        testEventTitle = "Organizer Test Event " + randomNumber;

        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(3000);

        // Switch to organizer mode
        onView(withId(R.id.modeToggle)).perform(click());

        Thread.sleep(3000); // Wait for mode change
    }

    @After
    public void tearDown() {
        // Log out after all tests
        auth.signOut();
        Intents.release();

        // Query and delete test events with the specified title
        db.collection("events")
                .whereEqualTo("title", testEventTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("TearDown", "Test event deleted successfully"))
                                    .addOnFailureListener(e ->
                                            Log.e("TearDown", "Error deleting test event", e));
                        }
                    } else {
                        Log.e("TearDown", "Error finding test events", task.getException());
                    }
                });

    }


    @Test
    public void testCreateEvent() throws InterruptedException {

        // Click on the add event button to open CreateEventActivity
        onView(withId(R.id.addEventButton)).perform(click());

        Thread.sleep(1000); // Allow time for the Create Event screen to load

        // Fill out the event form
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
        onView(withId(R.id.editTextEventDescription)).perform(replaceText("This is a description for the test event."));
        onView(withId(R.id.editTextLocation)).perform(replaceText("Test Location"));
        onView(withId(R.id.editTextStartDate)).perform(click()); // Open date picker for Start Date
        onView(withText("OK")).perform(click()); // Confirm the default date (or set a specific date if needed)
        onView(withId(R.id.editTextEndDate)).perform(click()); // Open date picker for End Date
        onView(withText("OK")).perform(click()); // Confirm the default date

        // Enter registration dates if applicable
        onView(withId(R.id.editRegistrationStartDate)).perform(click());
        onView(withText("OK")).perform(click()); // Confirm start registration date
        onView(withId(R.id.editRegistrationEndDate)).perform(click());
        onView(withText("OK")).perform(click()); // Confirm end registration date

        // Set event capacity
        onView(withId(R.id.editTextCapacity)).perform(replaceText("50"));

        // Toggle geolocation requirement
        onView(withId(R.id.toggleGeolocationRequirement)).perform(click());

        // Submit the event creation form
        onView(withId(R.id.buttonCreateEvent)).perform(scrollTo(), click());

        Thread.sleep(2000); // Wait for submission to complete

        // Switch back to user mode to view published events
        onView(withId(R.id.modeToggle)).perform(click());

        Thread.sleep(3000); // Wait for mode change

        // Check that the event title is displayed in the events list
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void testClickEventNavigatesToDetails() {

        onView(withId(R.id.nav_events)).perform(click());

        // Check that the event title is displayed in the events list
        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        // Find the event item in the RecyclerView by title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        // Verify that clicking the event navigates to the EventDetailActivity
        intended(hasComponent(EventDetailActivity.class.getName()));
    }
}
