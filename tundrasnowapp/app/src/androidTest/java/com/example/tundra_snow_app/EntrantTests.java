package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.tundra_snow_app.Activities.ProfileViewActivity;
import com.example.tundra_snow_app.Activities.SettingsViewActivity;
import com.example.tundra_snow_app.EventActivities.MyEventViewActivity;
import com.example.tundra_snow_app.Activities.EntrantSignupActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Test class that has multiple test cases for Entrant User Story functionalities.
 */
public class EntrantTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    String testEventTitle = "TestEventTitle";
    String testUserFirst = "TestFirst";
    String testUserLast = "TestLast";

    // Event title used for testing purposes
    String permanentEvent = "event_title";

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
     * Set up method that initializes Intents.
     */
    @Before
    public void setUp() {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }


    /**
     * Release Intents after tests are complete.
     */
    @After
    public void tearDown() {
        // Log out after all tests
        auth.signOut();
        Intents.release();
    }


    /** US 01.08.01 As an entrant, I want to be warned before joining a waiting list that requires geolocation.

     * Test to verify that warning is shown before joining waiting list that has geolocation.
     * @throws InterruptedException
     */
    @Test
    public void testWarningBeforeJoiningWaitingListWithGeolocation() throws InterruptedException {

        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);

 
        onView(withText(permanentEvent)).check(matches(isDisplayed()));
        onView(withText(permanentEvent)).perform(scrollTo(), click());
        
        Thread.sleep(2000);


        onView(withId(R.id.buttonSignUpForEvent)).check(matches(isDisplayed()));
        onView(withId(R.id.geoLocationNotification)).check(matches(isDisplayed()));
        onView(withId(R.id.geoLocationNotification)).check(matches(withText("Notice: Event Signup Requires Geolocation!")));
        
        Thread.sleep(2000);
    }

    /** US 1.02.01
     * Test to verify sign-up and login for entrants.
     * @throws InterruptedException
     */
    @Test
    public void testSignUpAndLogin() throws InterruptedException {
 
        onView(withId(R.id.signUpText)).perform(clickClickableSpan("Sign up"));
        intended(hasComponent(EntrantSignupActivity.class.getName()));

        String testEmail = "newuser@example.com";
        String testPassword = "password123";
        String testFacility = "testSetFacility";
        String facilityLocation = "testLocation";
        
        onView(withId(R.id.editTextFirstName)).perform(replaceText("John"));
        onView(withId(R.id.editTextLastName)).perform(replaceText("Doe"));
        onView(withId(R.id.editTextEmail)).perform(replaceText(testEmail));
        onView(withId(R.id.editTextPassword)).perform(replaceText(testPassword));
        onView(withId(R.id.editTextDateOfBirth)).perform(replaceText("01/01/1990"));
        onView(withId(R.id.editTextPhoneNumber)).perform(replaceText("1234567890"));
        onView(withId(R.id.toggleButtonNotification)).perform(click());
        onView(withId(R.id.checkBoxOrganizer)).perform(click());
        onView(withId(R.id.editTextFacility)).perform(replaceText(testFacility));
        onView(withId(R.id.editTextFacilityLocation)).perform(replaceText(facilityLocation));
        
        onView(withId(R.id.signupButton)).perform(click());

        Thread.sleep(2000);


        onView(withId(R.id.usernameEditText)).perform(replaceText(testEmail));
        onView(withId(R.id.passwordEditText)).perform(replaceText(testPassword));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);
        
        onView(withId(R.id.modeToggle)).check(matches(isDisplayed()));
    }

    
    /** US 1.04.03
     * Test to verify the settings functionality for entrants.
     * @throws InterruptedException
     */
    @Test
    public void testSettingsNotification() throws InterruptedException {
        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());
        
        Thread.sleep(2000);
        
        onView(withId(R.id.nav_settings)).perform(click());

        Thread.sleep(2000);
        
        intended(hasComponent(SettingsViewActivity.class.getName()));
        onView(withId(R.id.notificationsCheckbox)).check(matches(isDisplayed()));
        
        boolean isChecked = isCheckedState();

        if (isChecked) {
            
            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isNotChecked()));
        } else {
            
            onView(withId(R.id.notificationsCheckbox)).perform(click());
            onView(withId(R.id.notificationsCheckbox)).check(matches(isChecked()));
        }
    }
    

    /**
     *  Method to determine the current state of the notifications checkbox.
     *
     * @return true if the checkbox is checked, false otherwise.
     */
    private boolean isCheckedState() {
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

    /** US 1.02.02
     * Test to verify the updating of the profile information of an entrant.
     * @throws InterruptedException
     */
    @Test
    public void testUpdateProfileInformation() throws InterruptedException {
        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());

 
        Thread.sleep(2000);
        
        onView(withId(R.id.nav_profile)).perform(click());

     
        Thread.sleep(2000);

   
        intended(hasComponent(ProfileViewActivity.class.getName()));

    
        onView(withId(R.id.editButton)).perform(click());

    
        onView(withId(R.id.profileEmail)).perform(replaceText("updated.email@example.com"));
        onView(withId(R.id.profilePhone)).perform(replaceText("123-456-7890"));


        onView(withId(R.id.saveButton)).perform(click());


        Thread.sleep(2000);


        onView(withId(R.id.profileEmail)).check(matches(withText("updated.email@example.com")));
        onView(withId(R.id.profilePhone)).check(matches(withText("123-456-7890")));


        onView(withId(R.id.editButton)).perform(click());
        onView(withId(R.id.profileEmail)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.profilePhone)).perform(replaceText("123-456-7890"));
        onView(withId(R.id.saveButton)).perform(click());


        Thread.sleep(2000);

        // Verify the reverted information is displayed correctly
        onView(withId(R.id.profileEmail)).check(matches(withText("newuser@example.com")));
        onView(withId(R.id.profilePhone)).check(matches(withText("123-456-7890")));
    }

    /** US 1.07.01
     * Test to verify the sign-in process with the device identifier.
     * @throws InterruptedException
     */
    @Test
    public void testSignInWithDevice() throws InterruptedException {
        onView(withId(R.id.signInWithDeviceButton)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.modeToggle)).check(matches(isDisplayed()));
    }

    /** US 1.01.01, 1.01.02
     * Test to verify process of joining and leaving waiting list for a specific event.
     */
    @Test
    public void testJoinAndLeaveWaitingList() throws InterruptedException {
        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);

        onView(withText(permanentEvent)).check(matches(isDisplayed()));
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        Thread.sleep(2000);

        onView(withId(R.id.nav_events)).perform(click());

        Thread.sleep(2000);

        intended(hasComponent(MyEventViewActivity.class.getName()));
        onView(withText(permanentEvent)).check(matches(isDisplayed()));
        onView(withText(permanentEvent)).perform(scrollTo(), click());

        Thread.sleep(2000);

        onView(withId(R.id.rightButton)).check(matches(isDisplayed()));
        onView(withId(R.id.rightButton)).perform(click());
    }

    /** US 01.05.02
     * Tests simulates a user logging in, navigating to an event, and accepting the invitation to confirm
     * After confirming, the system displays a success message.
     * @throws InterruptedException
     */
    @Test
    public void testAcceptChosen() throws InterruptedException {
        Map<String, Object> user = new HashMap<>();
        user.put("dateOfBirth", "Jan 1 2001");
        user.put("deviceID", "5a75fb942b3e0c50");
        user.put("email", "testingg@2^2^2.ca");
        user.put("facilityList", Arrays.asList()); // empty array
        user.put("firstName", testUserFirst);
        user.put("lastName", testUserLast);
        user.put("notificationsEnabled", false);
        user.put("organizerEventList", Arrays.asList()); // empty array
        user.put("password", "Testing123");

        user.put("permissions", Arrays.asList(
                "NOTIFY_ENTRANTS",
                "JOIN_WAITLIST",
                "MANAGE_ENTRANTS",
                "CREATE_EVENT",
                "VIEW_EVENT_DETAILS",
                "UPDATE_PROFILE",
                "VIEW_ENTRANT_LIST"
        ));

        user.put("phoneNumber", "7804737373");

        // Roles array with specified values
        user.put("roles", Arrays.asList("user", "organizer"));

        user.put("userEventList", Arrays.asList()); // empty array
        user.put("userID", "000testUserId");
        db.collection("users").document("000testUserId").set(user);

        Map<String, Object> event = new HashMap<>();
        event.put("cancelledList", Arrays.asList()); // empty array
        event.put("capacity", 50);
        event.put("chosenList", Arrays.asList("000testUserId")); // add as a chosen participant
        event.put("confirmedList", Arrays.asList()); // empty array
        event.put("declinedList", Arrays.asList()); // empty array
        event.put("description", "This is a description for the test event.");

        // Timestamps for dates
        Timestamp currentTimestamp = Timestamp.now();
        event.put("endDate", currentTimestamp);
        event.put("registrationEndDate", currentTimestamp);
        event.put("registrationStartDate", currentTimestamp);
        event.put("startDate", currentTimestamp);

        event.put("entrantList", Arrays.asList()); // empty array
        event.put("eventID", "000testEventId");
        event.put("facility", Arrays.asList()); // empty array
        event.put("geolocationRequirement", "Remote");
        event.put("location", "Test Location");
        event.put("organizer", "2af6bd2b-cf5d-452c-919c-520059e7670c");
        event.put("published", "yes");
        event.put("status", "open");
        event.put("title", testEventTitle);
        db.collection("events").document("000testEventId").set(event);

        Thread.sleep(2000);
        onView(withId(R.id.usernameEditText)).perform(replaceText("testingg@2^2^2.ca"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("Testing123"));
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(1000);
        onView(withText(testEventTitle)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.rightButton)).perform(click());
        Thread.sleep(1000);
        onView(withText(testEventTitle)).perform(click());
        Thread.sleep(1000);
        onView(withText("You have confirmed your attendance!")).check(matches(isDisplayed()));

        db.collection("users").document("000testUserId").delete();
        db.collection("events").document("000testEventId").delete();
    }

    /** US 1.05.03
     * Test simulates a user logging in, navigating to an event, and declining their invitation to attend the event.
     * If the user is not confirmed for the event, a message stating that they were not chosen is displayed.
     *
     * @throws InterruptedException
     */
    @Test
    public void testDeclineChosen() throws InterruptedException {
        Map<String, Object> user = new HashMap<>();
        user.put("dateOfBirth", "Jan 1 2001");
        user.put("deviceID", "5a75fb942b3e0c50");
        user.put("email", "testingg@2^2^2.ca");
        user.put("facilityList", Arrays.asList()); // empty array
        user.put("firstName", testUserFirst);
        user.put("lastName", testUserLast);
        user.put("notificationsEnabled", false);
        user.put("organizerEventList", Arrays.asList()); // empty array
        user.put("password", "Testing123");

        user.put("permissions", Arrays.asList(
                "NOTIFY_ENTRANTS",
                "JOIN_WAITLIST",
                "MANAGE_ENTRANTS",
                "CREATE_EVENT",
                "VIEW_EVENT_DETAILS",
                "UPDATE_PROFILE",
                "VIEW_ENTRANT_LIST"
        ));

        user.put("phoneNumber", "7804737373");

        // Roles array with specified values
        user.put("roles", Arrays.asList("user", "organizer"));

        user.put("userEventList", Arrays.asList()); // empty array
        user.put("userID", "000testUserId");
        db.collection("users").document("000testUserId").set(user);

        Map<String, Object> event = new HashMap<>();
        event.put("cancelledList", Arrays.asList()); // empty array
        event.put("capacity", 50);
        event.put("chosenList", Arrays.asList("000testUserId")); // add as a chosen participant
        event.put("confirmedList", Arrays.asList()); // empty array
        event.put("declinedList", Arrays.asList()); // empty array
        event.put("description", "This is a description for the test event.");

        // Timestamps for dates
        Timestamp currentTimestamp = Timestamp.now();
        event.put("endDate", currentTimestamp);
        event.put("registrationEndDate", currentTimestamp);
        event.put("registrationStartDate", currentTimestamp);
        event.put("startDate", currentTimestamp);

        event.put("entrantList", Arrays.asList()); // empty array
        event.put("eventID", "000testEventId");
        event.put("facility", Arrays.asList()); // empty array
        event.put("geolocationRequirement", "Remote");
        event.put("location", "Test Location");
        event.put("organizer", "2af6bd2b-cf5d-452c-919c-520059e7670c");
        event.put("published", "yes");
        event.put("status", "open");
        event.put("title", testEventTitle);
        db.collection("events").document("000testEventId").set(event);

        Thread.sleep(2000);
        onView(withId(R.id.usernameEditText)).perform(replaceText("testingg@2^2^2.ca"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("Testing123"));
        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.nav_events)).perform(click());
        Thread.sleep(1000);
        onView(withText(testEventTitle)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.leftButton)).perform(click());
        Thread.sleep(1000);
        onView(withText(testEventTitle)).perform(click());
        Thread.sleep(1000);
        onView(withText("You were not chosen for this event!")).check(matches(isDisplayed()));

        db.collection("users").document("000testUserId").delete();
        db.collection("events").document("000testEventId").delete();
    }
}
