package eu.credential.app.patient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.administrator.credential_v020.BuildConfig;
import com.example.administrator.credential_v020.R;

import java.security.Provider;
import java.security.Security;

import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.ui.LoginActivity;
import eu.credential.app.patient.ui.MainActivity;
import eu.credential.app.patient.ui.SplashActivity;
import iaik.security.provider.IAIK;

/**
 * It's basic idea is to check if the application is started for the first time, then checks if
 * user are already locally registered and/or logged in. Based upon that a new screen is loaded.
 */
public class PatientApp extends AppCompatActivity {

    private static PatientApp instance;

    private SharedPreferences prefs = null;

    public static PatientApp getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        instance = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_app);

        IAIK.addAsProvider();
        Provider iaikProv = Security.getProvider("IAIK");
        iaikProv.put("CertificateFactory.X.509", "iaik.x509.X509CertificateFactory");

        checkFirstRun();
    }

    private void checkFirstRun() {

        Intent i;
        boolean login = SavePreferences.getDefaultsBoolean("login", PatientApp.getContext());
        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (login) {
            i = new Intent(PatientApp.this, MainActivity.class);
            startActivity(i);
            finish();

        } else if (savedVersionCode == DOESNT_EXIST) {
            i = new Intent(PatientApp.this, SplashActivity.class);
            startActivity(i);
            prefs.edit().putBoolean("firstrun", false).apply();
            finish();

        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
        } else if (!login) {
            i = new Intent(PatientApp.this, LoginActivity.class);
            startActivity(i);
            prefs.edit().putBoolean("firstrun", false).apply();
            finish();
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }
}
