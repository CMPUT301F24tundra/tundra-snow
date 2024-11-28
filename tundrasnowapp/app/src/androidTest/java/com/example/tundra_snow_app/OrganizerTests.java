package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
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

import java.time.LocalDate;
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
    String permanentEvent = "Important Test Event";
    String permanentEventID = "ef375549-e7b5-4078-8d22-959be14937f0";
    String permanentEntrant = "Main Organizer";
    String permanentEntrantID = "ef375549-e7b5-4078-8d22-959be14937f0";

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

    private void addEntrantToWaitingList() {
        db.collection("events")
                .document(permanentEventID)
                .update("entrantList", FieldValue.arrayUnion(permanentEntrantID))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Entrant added to waiting list"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error adding entrant to waiting list", e));

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
     * Testing US 02.06.04
     * As an organizer I want to cancel entrants that did not sign up for the event
     * @throws InterruptedException
     */
    @Test
    public void testDeletingEntrantFromWaitlist() throws InterruptedException {

        addEntrantToWaitingList();
        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        onView(withText(permanentEvent)).check(matches(isDisplayed()));

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());

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
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
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
        onView(withId(R.id.editTextEventTitle)).perform(replaceText(testEventTitle));
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
     * Testing US 02.02.01.
     * As an organizer I want to view the list of entrants who joined my event waiting list
     * @throws InterruptedException
     */
    @Test
    public void testViewWaitingList() throws InterruptedException {

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewWaitingList)).perform(scrollTo(), click());

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

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

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

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

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

        toggleToOrganizerMode();

        Thread.sleep(1000);

        onView(withId(R.id.nav_my_events)).perform(click());

        Thread.sleep(1000);

        // Scroll to the item with the specific title and click it
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(OrganizerEventDetailActivity.class.getName()));

        onView(withId(R.id.viewEnrolledList)).perform(scrollTo(), click());

        Thread.sleep(1000);

        intended(hasComponent(ViewConfirmedParticipantListActivity.class.getName()));

    }
}
