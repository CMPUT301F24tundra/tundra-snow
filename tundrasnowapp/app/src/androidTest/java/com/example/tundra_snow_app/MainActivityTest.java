package com.example.tundra_snow_app;

import static androidx.test.espresso.Espresso.onView;
import androidx.test.espresso.intent.Intents;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.allOf;

import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.tundra_snow_app.Activities.EntrantSignupActivity;
import com.example.tundra_snow_app.EventActivities.EventViewActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private FirebaseFirestore db;

    String testEmail = "newuser@example.com";

    // Custom matcher to check if TextView has clickable spans
    private static Matcher<View> hasClickableSpan() {
        return new BoundedMatcher<View, TextView>(TextView.class) {
            @Override
            protected boolean matchesSafely(TextView textView) {
                return textView.getMovementMethod() != null;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("has clickable span");
            }
        };
    }

    // Custom ViewAction to click on the specified ClickableSpan text
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

    @Before
    public void setUp() {
        Intents.init();
        db = FirebaseFirestore.getInstance();
    }

    @After
    public void tearDown() {
        Intents.release();

        db.collection("users")
                .whereEqualTo("email", testEmail)  // Use the current title in the loop
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("TearDown", "User with email '" + testEmail + "' deleted successfully"))
                                    .addOnFailureListener(e ->
                                            Log.e("TearDown", "Error deleting user with email '" + testEmail + "'", e));
                        }
                    } else {
                        Log.e("TearDown", "Error finding users with email '" + testEmail + "'", task.getException());
                    }
                });
    }

    @Test
    public void testFailedLogin() {
        // Enter wrong credentials
        onView(withId(R.id.usernameEditText)).perform(replaceText("wrong@email.com"));
        onView(withId(R.id.passwordEditText)).perform(replaceText("wrongpassword"));

        onView(withId(R.id.loginButton)).perform(click());

        // Ensure MainActivity is still displayed (no navigation occurred)
        onView(withId(R.id.loginButton)).check(matches(isDisplayed()));
    }

    @Test
    public void testSignUpTextContent() {
        // Verify the text displayed is correct
        onView(withId(R.id.signUpText)).check(matches(withText("Don't have an account? Sign up")));
    }

    @Test
    public void testSignUpTextIsClickable() {
        // Verify the TextView has clickable spans
        onView(withId(R.id.signUpText)).check(matches(hasClickableSpan()));
    }

    @Test
    public void testSignUpClickNavigation() {
        // Click on the specific ClickableSpan within the TextView
        onView(withId(R.id.signUpText)).perform(clickClickableSpan("Sign up"));

        // Verify navigation to EntrantSignupActivity
        intended(hasComponent(EntrantSignupActivity.class.getName()));
    }

    /** US 01.02.01 As an entrant, I want to provide my personal information such
     * as name, email and optional phone number in the app
     * @throws InterruptedException
     */
    @Test
    public void testSignUpAndLogin() throws InterruptedException {
        // Navigate to the sign-up screen
        onView(withId(R.id.signUpText)).perform(clickClickableSpan("Sign up"));

        // Verify navigation to EntrantSignupActivity
        intended(hasComponent(EntrantSignupActivity.class.getName()));

        String testPassword = "password123";
        String testFacility = "testSetFacility";
        String facilityLocation = "testLocation";
        
        // Fill out the sign-up form
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


        // Submit the sign-up form
        onView(withId(R.id.signupButton)).perform(click());

        Thread.sleep(2000);

        // Log in with the newly created account
        onView(withId(R.id.usernameEditText)).perform(replaceText(testEmail));
        onView(withId(R.id.passwordEditText)).perform(replaceText(testPassword));
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);

        // Verify successful login by checking for an element in the target activity
        intended(hasComponent(EventViewActivity.class.getName()));
    }

    /** US 1.07.01
     * Test to verify the sign-in process with the device identifier.
     * @throws InterruptedException
     */
    @Test
    public void testSignInWithDevice() throws InterruptedException {

        onView(withId(R.id.signInWithDeviceButton)).perform(click());

        Thread.sleep(1000);

        intended(hasComponent(EventViewActivity.class.getName()));
    }
}
