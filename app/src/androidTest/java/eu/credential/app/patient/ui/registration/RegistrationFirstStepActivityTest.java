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

public class RegistrationFirstStepActivityTest {

    @Rule
    public ActivityTestRule<RegistrationFirstStepActivity> activityRule = new ActivityTestRule<>(
            RegistrationFirstStepActivity.class);

    @Before
    public void setUp() {
    }

    @Test
    public void onCreate() {
        onView(withId(R.id.imageViewFirstRegistration)).check(matches(isDisplayed()));
        onView(withId(R.id.first_name)).perform(typeText("John"));
        onView(withId(R.id.last_name)).perform(typeText("Doe"));
        onView(withId(R.id.email_registration)).perform(scrollTo(), typeText("Bar@foo.ba"));
        onView(withId(R.id.city_registration)).perform(scrollTo(), typeText("Foo"));
        onView(withId(R.id.next_imageButton)).perform(scrollTo(), click());
    }
}