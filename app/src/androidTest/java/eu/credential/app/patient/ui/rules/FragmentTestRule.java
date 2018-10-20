package eu.credential.app.patient.ui.rules;

import android.support.test.rule.ActivityTestRule;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class FragmentTestRule <A extends AppCompatActivity, F extends Fragment>
        implements TestRule {

    private ActivityTestRule<A> activityRule;
    private F fragment;
    private RuleChain ruleChain;

    public FragmentTestRule(Class<A> activityClass, F fragment) {
        this.fragment = fragment;
        this.activityRule = new ActivityTestRule<>(activityClass);
        ruleChain = RuleChain
                .outerRule(activityRule)
                .around(new OpenFragmentRule<>(activityRule, fragment));
    }

    public ActivityTestRule<A> getActivityRule() {
        return activityRule;
    }

    public F getFragment() {
        return fragment;
    }

    public void runOnUiThread(Runnable runnable) throws Throwable {
        activityRule.runOnUiThread(runnable);
    }

    public A getActivity() {
        return activityRule.getActivity();
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return ruleChain.apply(statement, description);
    }
}
