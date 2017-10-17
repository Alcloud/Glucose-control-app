package eu.credential.app.patient;

import android.app.Application;
import android.content.Context;

/**
 * Created by tfl on 27.07.17.
 */

public class PatientApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        PatientApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return PatientApp.context;
    }
}
