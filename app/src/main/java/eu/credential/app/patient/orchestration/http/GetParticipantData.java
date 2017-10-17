package eu.credential.app.patient.orchestration.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetParticipantData extends AsyncTask<Object, Object, JSONArray> {
    private String addressURL = "http://194.95.174.238:8081/dms/data/";
    private String accountId;

    public GetParticipantData(String accountId) {
        super();
        this.accountId = accountId;
    }

    @Override
    protected JSONArray doInBackground(Object... params) {
        JSONObject responseJSON;
        JSONArray parameter = new JSONArray();

        try {
            URL url = new URL(addressURL + accountId);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                responseJSON = new JSONObject(jsonToString(httpURLConnection));
                parameter = responseJSON.getJSONObject("metadata").getJSONObject("appSpecific")
                        .getJSONArray("addressbook");
            } else {
                Log.d("myLogs", "no connection");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return parameter;
    }

    private String jsonToString(HttpURLConnection httpURLConnection) throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);
        String tmp;
        while ((tmp = reader.readLine()) != null)
            sb.append(tmp).append("\n");
        reader.close();
        Log.d("myLogs", sb.toString());
        return sb.toString();
    }
}