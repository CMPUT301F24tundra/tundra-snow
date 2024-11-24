package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;
import android.view.View;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String permanentEvent = "Important Testing Event";
    String permanentEventID = "ada10f4e-014f-4ee6-a76d-5d5b36a096c6";
    String permanentEntrant = "Main Organizer";
    String permanentEntrantID = "ef375549-e7b5-4078-8d22-959be14937f0";

    String testEventTitle = "";

    Set<String> generatedTitles  = new HashSet<>();

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // log in with admin credentials
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

    private void toggleToOrganizerModeIfNeeded() {
        // Try to get the text on the toggle button and click it only if it reads "Toggle Mode: User"
        ViewInteraction toggleButton = onView(withId(R.id.modeToggle));

        try {
            toggleButton.perform(new ViewAction() {
                @Override
                public Matcher<View> getConstraints() {
                    return ViewMatchers.isDisplayed(); // Ensures the view is displayed before performing the action
                }

                @Override
                public String getDescription() {
                    return "Toggle to organizer mode if currently in user mode";
                }

                @Override
                public void perform(UiController uiController, View view) {
                    // Check the current text on the button
                    String currentText = ((android.widget.ToggleButton) view).getText().toString();
                    if (currentText.equals("Toggle Mode: User")) {
                        view.performClick(); // Click only if the text is "Toggle Mode: User"
                    }
                }
            });
        } catch (NoMatchingViewException | PerformException e) {
            // Do nothing if the toggle button is not found or click can't be performed
        }
    }

    private void addEntrantToWaitingList() {
        db.collection("events")
                .document(permanentEventID)
                .update("entrantList", FieldValue.arrayUnion(permanentEntrantID))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Entrant added to waiting list"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding entrant to waiting list", e));

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

        // Toggle to organizer mode if needed
        toggleToOrganizerModeIfNeeded();
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
     * Testing US 02.06.04
     * As an organizer I want to cancel entrants that did not sign up for the event
     * @throws InterruptedException
     */
    @Test
    public void testDeletingEntrantFromWaitlist() throws InterruptedException {

        addEntrantToWaitingList();

        toggleToOrganizerModeIfNeeded();

        Thread.sleep(1000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(click());

        Thread.sleep(1000);

        onView(withId(R.id.rejectUser)).perform(click());

        // Verify rejection
        Thread.sleep(1000);

        // check that the entrant was removed
        onView(withText(permanentEntrant)).check(doesNotExist());
    }

    /**
     * Testing US 02.02.03
     * As an organizer I want to enable or disable the geolocation requirement for my event.
     * @throws InterruptedException
     */
    @Test
    public void testCreateEvent() throws InterruptedException {

        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;

        generatedTitles.add(testEventTitle);

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

        // Enable geolocation requirement
        onView(withId(R.id.toggleGeolocationRequirement)).perform(click());

        // Submit the event creation form
        onView(withId(R.id.buttonCreateEvent)).perform(scrollTo(), click());

        Thread.sleep(1000); // Wait for submission to complete

        // Switch back to user mode to view published events
        onView(withId(R.id.modeToggle)).perform(click());

        Thread.sleep(1000); // Wait for mode change

        // Check that the event title is displayed in the events list
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
    }

    /**
     * CURRENTLY FAILING
     * Testing US 02.03.01
     * As an organizer I want to OPTIONALLY limit the number of entrants who
     * can join my waiting list
     * @throws InterruptedException
     */
    @Test
    public void testCreateEventNoCapacity() throws InterruptedException {

        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Organizer Test Event " + randomNumber;

        generatedTitles.add(testEventTitle);

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

        // Toggle geolocation requirement
        onView(withId(R.id.toggleGeolocationRequirement)).perform(click());

        // Submit the event creation form
        onView(withId(R.id.buttonCreateEvent)).perform(scrollTo(), click());

        Thread.sleep(1000); // Wait for submission to complete

        // Switch back to user mode to view published events
        onView(withId(R.id.modeToggle)).perform(click());

        Thread.sleep(1000); // Wait for mode change

        // Check that the event title is displayed in the events list
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
    }

    /**
     * Testing US 02.02.01.
     * As an organizer I want to view the list of entrants who joined my event waiting list
     * @throws InterruptedException
     */
    @Test
    public void testViewWaitingList() throws InterruptedException {

        toggleToOrganizerModeIfNeeded();

        Thread.sleep(1000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(ViewParticipantListActivity.class.getName()));
    }

    /**
     * Testing US 02.06.01
     * As an organizer I want to view a list of all chosen entrants who are
     * invited to apply
     * @throws InterruptedException
     */
    @Test
    public void testViewChosenList() throws InterruptedException {

        toggleToOrganizerModeIfNeeded();

        Thread.sleep(1000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        // Wait for data to load by checking if the permanentEvent title is displayed
        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewChosenList)).perform(click());

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

        toggleToOrganizerModeIfNeeded();

        Thread.sleep(1000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        // Wait for data to load by checking if the permanentEvent title is displayed
        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewCancelledList)).perform(click());

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

        toggleToOrganizerModeIfNeeded();

        Thread.sleep(1000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(1000);

        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewEnrolledList)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(ViewConfirmedParticipantListActivity.class.getName()));

    }
}
