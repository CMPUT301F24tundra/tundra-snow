package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;

import android.content.res.AssetManager;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.tundra_snow_app.AdminActivities.AdminEventViewActivity;
import com.example.tundra_snow_app.ListActivities.EntrantSignupActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

@RunWith(AndroidJUnit4.class)
public class AdminTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    // Add test data to firebase
    String testEventTitle = "7^7^7";
    String testFacilityTitle = "8^8^8";
    String testUserFirst = "999";
    String testUserLast = "555";

    /**
     * Generates test data for a facility, event and user.
     */
    @Before
    public void setUp() throws InterruptedException {

        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        // filling the fields of the test facility
        Map<String, Object> facility = new HashMap<>();
        facility.put("facilityID", "833189b6-ec5b-489b-a450-0725b87f4a2e");
        facility.put("facilityLocation", "testfacloc");
        facility.put("facilityName", testFacilityTitle);
        // add to facilities collection with the id "testFacilityId"
        db.collection("facilities").document("00testFacilityId").set(facility);
        Map<String, Object> user = new HashMap<>();
        user.put("dateOfBirth", "Jan 1 2001");
        user.put("deviceID", "5a75fb942b3e0c48");
        user.put("email", "testing@2^2^2.ca");
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
        user.put("userID", "0848c905-872b-4d3e-a561-17539179bf2c");
        db.collection("users").document("00testUserId").set(user);

        Map<String, Object> event = new HashMap<>();
        event.put("cancelledList", Arrays.asList()); // empty array
        event.put("capacity", 50);
        event.put("chosenList", Arrays.asList()); // empty array
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
        event.put("eventID", "206f230b-a1b5-4a3d-a887-9d8c3869a009");
        event.put("facility", Arrays.asList()); // empty array
        event.put("geolocationRequirement", "Remote");
        event.put("location", "Test Location");
        event.put("organizer", "2af6bd2b-cf5d-452c-919c-520059e7670c");
        event.put("published", "yes");
        event.put("status", "open");
        event.put("title", testEventTitle);
        db.collection("events").document("00testEventId").set(event);

        // log in with admin credentials
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
        // Log out after all tests
        db.collection("users").document("00testUserId").delete();
        db.collection("events").document("00testEventId").delete();
        db.collection("facilities").document("00testFacilityId").delete();
        auth.signOut();
        Intents.release();
    }

    /**
     * Testing US 03.04.01 and US 03.01.01
     * As an administrator, I want to be able to browse/remove events
     * First an event is generated at the top of the view, then checked if it is displayed
     * Second the event is deleted from the view, then checked if it is gone
     * @throws InterruptedException
     */
    @Test
    public void testAdminEventViewDelete() throws InterruptedException {
        onView(withId(R.id.admin_nav_events)).perform(click());
        // check if event is displayed
        Thread.sleep(1000);
        onView(withText(testEventTitle)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        // click the button that has the right name next to it
        onView(allOf(withId(R.id.removeEventButton), hasSibling(hasDescendant(withText(testEventTitle))))).perform(click());
        Thread.sleep(1000);
        // check that it's now gone
        onView(withText(testEventTitle)).check(doesNotExist());


    }

    /**
     * Testing US 03.07.01
     *  As an administrator I want to remove facilities that violate app policy
     * First a facility is generated at the top of the view, then checked if it is displayed
     * Second the facility is deleted from the view, then checked if it is gone
     * @throws InterruptedException
     */
    @Test
    public void testAdminFacilityViewDelete() throws InterruptedException {
        // move to the facility view
        onView(withId(R.id.admin_nav_facilities)).perform(click());
        Thread.sleep(1000);
        // check if displayed
        onView(withText(testFacilityTitle)).check(matches(isDisplayed()));
        Thread.sleep(1000);
        // click the button that has the right name next to it
        onView(allOf(withId(R.id.removeEventButton), hasSibling(hasDescendant(withText(testFacilityTitle))))).perform(click());
        Thread.sleep(1000);
        onView(withText(testFacilityTitle)).check(doesNotExist());
    }

    /**
     * Testing US 03.02.01 and US 03.05.01
     * As an administrator, I want to be able to browse/remove users
     * First a user is generated at the top of the view, then checked if it is displayed
     * Second the user is deleted from the view, then checked if it is gone
     * @throws InterruptedException
     */
    @Test
    public void testAdminUserViewDelete() throws InterruptedException {
        onView(withId(R.id.admin_nav_profiles)).perform(click());
        Thread.sleep(1000);
        onView(withText(testUserFirst + " " + testUserLast)).check(matches(isDisplayed()));

        Thread.sleep(1000);
        onView(allOf(withId(R.id.removeUserButton), hasSibling(hasDescendant(withText(testUserFirst + " " + testUserLast))))).perform(click());
        Thread.sleep(1000);
        onView(withText(testUserFirst + " " + testUserLast)).check(doesNotExist());


    }

}