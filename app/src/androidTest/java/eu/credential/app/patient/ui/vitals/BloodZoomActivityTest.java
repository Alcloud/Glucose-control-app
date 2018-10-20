package eu.credential.app.patient.ui.vitals;

import android.support.test.rule.ActivityTestRule;

import com.example.administrator.credential_v020.R;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class BloodZoomActivityTest {

    @Rule
    public ActivityTestRule<BloodZoomActivity> activityRule = new ActivityTestRule<>(
            BloodZoomActivity.class);

    @Test
    public void onCreate() {
        onView(withId(R.id.spinner_period_blood)).perform(click());
    }
}