package eu.credential.app.patient.orchestration.firebase;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import eu.credential.app.patient.orchestration.http.Request;

/**
 * Created by Aleksei Piatkin on 27.05.17.
 * <p>
 * This class get app id (token) only by a first install (or reinstall on a new device)
 * and send it to server.
 */

public class MyAndroidFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";
    private static final String ADD_PREFERENCE_URL =
            "http://194.95.174.238:8083/v1/notificationManagementService/addPreferences";
    private String accountId = "HansAugust";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        Request request = new Request(ADD_PREFERENCE_URL, accountId, token,
                "appid", "addAppId", getApplicationContext());
        request.execute();
    }
}