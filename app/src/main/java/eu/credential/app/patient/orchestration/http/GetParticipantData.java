package eu.credential.app.patient.orchestration.http;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Arrays;

import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SetURLConnection;

/**
 * Created by Aleksei Piatkin on 15.11.17.
 * <p>
 * This class helps to get participant data.
 * Participant data: address book, PHR key, user details, glucose or weight measurement PHR-document id
 */
public class GetParticipantData extends AsyncTask<String, String, JSONArray> {

    private String dataId;
    private boolean flag = true;
    private ProgressDialog dialog;
    private AsyncTaskCompleteListener<JSONArray> callback;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    private static final String TAG = "Performance";

    public GetParticipantData(String dataId, Context context, AsyncTaskCompleteListener<JSONArray> callback) {
        super();
        this.dataId = dataId;
        this.context = context;
        this.callback = callback;
    }

    public GetParticipantData(String dataId) {
        super();
        this.dataId = dataId;
    }

    @Override
    protected void onPreExecute() {
        if (context != null) {
            dialog = ProgressDialog.show(context, "Saving settings...", "Please wait");
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    protected JSONArray doInBackground(String... params) {
        JSONObject responseJSONobject;
        JSONArray responseJSONarray;
        JSONArray parameter = new JSONArray();
        JSONArray error = new JSONArray();
        String[] test = new String[5];

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("dms.protocol")
                .host("dms.host")
                .path("dms.path").build();
        try {
            long startTime = System.nanoTime();

            error.put(new JSONObject("{\"error\": \"true\"}"));
            HttpURLConnection httpURLConnection = SetURLConnection.setConnection("GET",
                    "data/" + dataId, null);

            long endTime = System.nanoTime();
            Log.i(TAG, "-|Get user data from DMS|" + SetURLConnection.setURL() + "/data/" +
                    "|" + dataId + "|" + startTime / 1000000 + "|" +
                    (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                publishProgress("Connection successful");

                responseJSONobject = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));

                // get participant data and save into JSON array
                responseJSONarray = responseJSONobject.getJSONArray("fileContent");
                if (responseJSONarray != null && responseJSONarray.length() > 0) {
                    if (!responseJSONarray.getString(0).equals("null")) {
                        for (int i = 0; i < responseJSONarray.length(); i++) {
                            if (responseJSONarray.getString(i).startsWith("{")) {
                                parameter.put(new JSONObject(responseJSONarray.getString(i)));
                            } else {
                                test[i] = responseJSONarray.getString(i);
                            }
                        }
                        if (test[0] != null) {
                            parameter = new JSONArray(Arrays.asList(test));
                        }
                    }
                }
            } else {
                Log.d("myLogs", "no connection");
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

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (dialog != null) {
            if (values[0] != null) {
                dialog.setMessage(values[0]);
            }
        }
    }

    @Override
    protected void onPostExecute(JSONArray result) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
        if (callback != null) {
            callback.onTaskComplete(result);
        }
    }
}