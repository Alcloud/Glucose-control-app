package eu.credential.app.patient.orchestration.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;

/**
 * Created by Aleksei Piatkin on 27.05.17.
 * <p>
 * This class get app id (token) only by a first install (or reinstall on a new device)
 * and send it to server.
 */

public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        saveAppId(refreshedToken);
    }

    private void saveAppId(String token) {
        SavePreferences.setDefaultsString("appid", token, PatientApp.getContext());
    }
}