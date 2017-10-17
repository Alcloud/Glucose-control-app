package eu.credential.app.patient.ui.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.administrator.credential_v020.R;

/**
 * Fragment for manipulating and showing the current app preferences.
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load preferences from the xml preferences file
        addPreferencesFromResource(R.xml.preferences);
    }
}
