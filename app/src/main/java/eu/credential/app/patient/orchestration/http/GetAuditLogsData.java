package eu.credential.app.patient.orchestration.http;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;

/**
 * Created by Aleksei Piatkin on 4.12.17.
 * <p>
 * This class helps to get doctor's data as JSON array from LDAP.
 */
public class GetAuditLogsData extends AsyncTask<Object, Object, JSONArray> {

    private String accountId = SavePreferences.getDefaultsString("accountId", PatientApp.getContext());
    private String ssoToken = SavePreferences.getDefaultsString("ssoToken", PatientApp.getContext());

    private ProgressDialog dialog;
    private boolean flag = true;

    public GetAuditLogsData(Context context) {
        super();
        dialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Loading data, please wait.");
        dialog.show();
    }

    @Override
    protected JSONArray doInBackground(Object... params) {
        JSONArray parameter = new JSONArray();
        JSONArray error = new JSONArray();

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("auditlogs.protocol")
                .host("auditlogs.host")
                .path("auditlogs.path")
                .ssoToken(ssoToken)
                .build();
        try {
            error.put(new JSONObject("{\"error\": \"true\"}"));

            HttpURLConnection httpURLConnection = SetURLConnection.setConnection("GET",
                    "event?userid=" + accountId + "&from=" + String.valueOf(System.currentTimeMillis() - 360000), "");

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                parameter = new JSONArray(SetURLConnection.jsonToString(httpURLConnection));
            } else {
                flag = false;
            }
        } catch (IOException | JSONException e) {
            flag = false;
            e.printStackTrace();
        }
        if (!flag) {
            return error;
        } else return parameter;
    }

    protected void onPostExecute(JSONArray result) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}