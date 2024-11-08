package com.example.tundra_snow_app;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tundra_snow_app.EventActivities.CreateEventActivity;
import com.example.tundra_snow_app.EventActivities.EventDetailActivity;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@RunWith(AndroidJUnit4.class)
public class EventActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    String testEventTitle = "";
    String eventTitle = "Testing Event!";
    String eventID = "61da5ec7-3b7d-4439-8245-c38294bf6511";
    String entrantID = "9d64eabe-10fe-428d-8cdd-ce00abe2ea88";

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        int randomNumber = new Random().nextInt(1000);
        testEventTitle = "Test Event Title " + randomNumber;

        onView(withId(R.id.usernameEditText)).perform(replaceText("newuser@example.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("password123"));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(3000);
    }

    @After
    public void tearDown() {
        // Log out after all tests
        auth.signOut();
        Intents.release();
    }

    @Test
    public void testClickEventNavigatesToDetails() {
        // Find the event item in the RecyclerView by title and click it
        onView(withText(eventTitle)).perform(scrollTo(), click());

        // Verify that clicking the event navigates to the EventDetailActivity
        intended(hasComponent(EventDetailActivity.class.getName()));

        // Optionally, check that EventDetailActivity displays the event title
        onView(withId(R.id.detailEventTitle)).check(matches(withText(eventTitle)));
    }

    @Test
    public void testSignUpForEvent() throws InterruptedException, ExecutionException {

        DocumentSnapshot initialSnapshot = Tasks.await(db.collection("events").document(eventID).get());
        if (initialSnapshot.exists()) {
            List<String> entrantList = (List<String>) initialSnapshot.get("entrantList");
            assertFalse(entrantList == null);
        }

        // Find the event item in the RecyclerView by title and click it
        onView(withText(eventTitle)).perform(scrollTo(), click());

        // Click the sign-up button
        onView(withId(R.id.buttonSignUpForEvent)).perform(click());

        Thread.sleep(2000);

        DocumentSnapshot finalSnapshot = Tasks.await(db.collection("events").document(eventID).get());
        if (finalSnapshot.exists()) {
            List<String> entrantList = (List<String>) finalSnapshot.get("entrantList");
            assertTrue(entrantList != null && entrantList.contains(entrantID));

            // Cleanup: Remove the user from entrantList
            Tasks.await(db.collection("events").document(eventID)
                    .update("entrantList", FieldValue.arrayRemove(entrantID)));
        }
    }
}
