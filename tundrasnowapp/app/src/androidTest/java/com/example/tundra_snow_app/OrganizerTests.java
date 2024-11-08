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
import com.example.tundra_snow_app.ListActivities.EntrantSignupActivity;
import com.example.tundra_snow_app.ListActivities.ViewParticipantListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrganizerTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String testEventTitle = "";
    String permanentEvent = "Permanent Test Event";

    List<String> generatedTitles = new ArrayList<>();

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;

        generatedTitles.add(testEventTitle);

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
    public void testClickEventNavigatesToDetails() throws InterruptedException {
        // Navigate to events list
        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(3000); // Wait for change

        // Wait for data to load by checking if the permanentEvent title is displayed
        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(3000); // Wait for mode change

        // Optionally, verify that EventDetailActivity displays the event title
        onView(withId(R.id.organizerEventTitle)).check(matches(withText(permanentEvent)));
    }

    // US 02.02.01 As an organizer I want to view the list of
    // entrants who joined my event waiting lis
    @Test
    public void testViewWaitingList() throws InterruptedException {
        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(3000); // Wait for change

        // Wait for data to load by checking if the permanentEvent title is displayed
        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(3000); // Wait for mode change

        onView(withId(R.id.viewWaitingList)).perform(click());

        intended(hasComponent(ViewParticipantListActivity.class.getName()));
    }
}
