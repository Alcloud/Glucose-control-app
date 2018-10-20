package eu.credential.app.patient.orchestration.http;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;

public class SsoTokenRefresh extends AsyncTask<Object, Object, Void> {

    private static final String TAG = SsoTokenRefresh.class.getSimpleName();

    @Override
    protected void onPreExecute() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(Object... params) {
        JSONObject responseJSONobject;
        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("wallet.protocol")
                .host("wallet.host")
                .port("wallet.port")
                .path("wallet.path").build();
        try {
            HttpURLConnection httpURLConnection = SetURLConnection.setConnection("POST",
                    "mobile/sessions/" + SavePreferences.getDefaultsString("ssoToken",
                            PatientApp.getContext()) + "?_action=validate", "");
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseJSONobject = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));

                // check if ssoTocken is valid
                if(responseJSONobject.getBoolean("valid")){
                    Log.d(TAG, "ssoToken is valid");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
