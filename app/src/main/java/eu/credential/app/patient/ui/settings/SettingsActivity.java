package eu.credential.app.patient.ui.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * View for the settings fragment
 */
public class SettingsActivity extends Activity {

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display settings fragment as main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment()).commit();
    }

}
