package eu.credential.app.patient.ui;

import android.support.test.rule.ActivityTestRule;

import com.example.administrator.credential_v020.R;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class LoginActivityTest {
    @Rule
    public ActivityTestRule<LoginActivity> activityRule = new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void onCreate() {
        onView(withId(R.id.login_form)).check(matches(isDisplayed()));
        onView(withId(R.id.imageViewLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.email_login)).perform(typeText("JohnDoe"));
        onView(withId(R.id.password)).perform(typeText("123456789"));
        onView(withId(R.id.account_sign_in_button)).check(matches(isDisplayed()));
        onView(withId(R.id.register_button)).perform(click());
    }

    @Test
    public void onBackPressed() {
    }
}