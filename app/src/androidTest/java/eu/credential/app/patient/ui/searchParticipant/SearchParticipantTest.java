package eu.credential.app.patient.ui.searchParticipant;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import eu.credential.app.patient.ui.MainActivity;
import eu.credential.app.patient.ui.rules.FragmentTestRule;

@RunWith(AndroidJUnit4.class)
public class SearchParticipantTest {

    @Rule
    public final RuleChain rules = RuleChain
            .outerRule(new ActivityTestRule<>(MainActivity.class))
            .around(new FragmentTestRule<>(MainActivity.class, new SearchParticipant()));

    /*@Before
    public void before() {
        mock(SavePreferences.class);
    }

    @Test
    public void onCreate() {
        mock(SavePreferences.class);
        when(SavePreferences.getDefaultsBoolean(anyString(), any())).thenReturn(true);
        when(SavePreferences.getDefaultsString(anyString(), any())).thenReturn("foo");
        onView(withId(R.id.spinner_profession)).check(matches(isDisplayed()));
    }*/
}