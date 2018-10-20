package eu.credential.app.patient.ui.registration;

import android.support.test.rule.ActivityTestRule;

import com.example.administrator.credential_v020.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class RegistrationSecondStepActivityTest {

    @Rule
    public ActivityTestRule<RegistrationSecondStepActivity> activityRule = new ActivityTestRule<>(
            RegistrationSecondStepActivity.class);

    @Before
    public void setUp() {
    }

    @Test
    public void onCreate() {
        onView(withId(R.id.imageViewSecondRegistration)).check(matches(isDisplayed()));
        onView(withId(R.id.account_name)).perform(typeText("JohnDoe"));
        onView(withId(R.id.password_first)).perform(typeText("123456789"));
        onView(withId(R.id.password_second)).perform(scrollTo(), typeText("123456789"));
        onView(withId(R.id.register_imageButton)).check(matches(isDisplayed()));
        onView(withId(R.id.previous_imageButton)).perform(scrollTo(), click());
    }
}