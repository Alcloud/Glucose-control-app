package eu.credential.app.patient.orchestration.http;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class UpdateParticipantData extends AsyncTask<String, Void, Void> {

    private String doctorId;
    private String doctorName;
    private String doctorSurname;
    private String doctorMainRole;
    private String doctorRole;
    private String doctorCity;
    private String accountId;
    private String operationId = "";
    JSONArray array;

    private static final String GET_PARTICIPANT_URL = "http://194.95.174.238:8081/dms/data/";

    public UpdateParticipantData(JSONArray array, String doctorId, String accountId, String doctorName,
                                 String doctorSurname, String doctorCity, String doctorMainRole, String doctorRole) {
        super();
        this.array = array;
        this.doctorId = doctorId;
        this.accountId = accountId;
        this.doctorName = doctorName;
        this.doctorSurname = doctorSurname;
        this.doctorMainRole = doctorMainRole;
        this.doctorCity = doctorCity;
        this.doctorRole = doctorRole;
    }

    public UpdateParticipantData(JSONArray array, String accountId, String operationId) {
        super();
        this.array = array;
        this.accountId = accountId;
        this.operationId = operationId;
    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject requestMessage;

        try {
            URL url2 = new URL(GET_PARTICIPANT_URL + accountId);
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) url2.openConnection();
            httpURLConnection2.setDoOutput(true);
            httpURLConnection2.setRequestMethod("DELETE");
            httpURLConnection2.setRequestProperty("Content-Type", "application/json");
            httpURLConnection2.connect();
            Log.d("Update_participant", String.valueOf(httpURLConnection2.getResponseCode()));

            URL url = new URL(GET_PARTICIPANT_URL + accountId);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("PUT");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.connect();

            if (operationId.equals("delete")) {
                operationId = "";
            } else {
                JSONObject object = new JSONObject("{\n" +
                        "                    \"id\": \"" + doctorId + "\",\n" +
                        "                    \"name\": \"" + doctorName + "\",\n" +
                        "                    \"surname\": \"" + doctorSurname + "\",\n" +
                        "                    \"city\": \"" + doctorCity + "\",\n" +
                        "                    \"mainrole\": \"" + doctorMainRole + "\",\n" +
                        "                    \"role\":\"" + doctorRole + "\"\n" +
                        "                }\n");
                array.put(object);
                Log.d("Update participant", "doctorArray: " + array.toString());
            }
            requestMessage = new JSONObject("{\n" +
                    "\t\"metadata\": {\n" +
                    "\t\t\"identifier\":{\n" +
                    "\t\t\t\"dataId\":\"" + accountId + "\",\n" +
                    "\t\t\t\"parentId\":\"-\",\n" +
                    "\t\t\t\"ownerId\":\"4321\",\n" +
                    "\t\t\t\"dataType\":\"file\"\n" +
                    "\t\t},\n" +
                    "\t\t\"generic\":{\n" +
                    "\t\t\t\"name\":\"testFile1\",\n" +
                    "\t\t\t\"creationDate\":\"Mon Sep 11 13:42:26 GMT 2017\",\n" +
                    "\t\t\t\"lastModificationDate\":\"Mon Sep 11 13:42:26 GMT 2017\"\n" +
                    "\t\t},\n" +
                    "\t\t\"appSpecific\":{\n" +
                    "\t\t\t\"addressbook\":" + array.toString() +
                    "\t\t},\n" +
                    "\t\t\"tags\":[],\n" +
                    "\t\t\n" +
                    "\t\t\"fileSpecific\":{\n" +
                    "\t\t\n" +
                    "\t\t},\n" +
                    "\t\t\"signature\":{\n" +
                    "\t\t\n" +
                    "\t\t}\n" +
                    "\t},\n" +
                    "\t\"fileContent\":[],\n" +
                    "\t\"replace\":true\n" +
                    "}");
            Log.d("Update_participant", "requestMessage: " + requestMessage.toString());

            OutputStreamWriter osw = new OutputStreamWriter(httpURLConnection.getOutputStream());
            osw.write(requestMessage.toString());
            osw.close();
            httpURLConnection.getInputStream();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

            } else {
                Log.d("Update_participant", "no connection");
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Convert JSON object to String format
    private String jsonToString(HttpURLConnection httpURLConnection) throws IOException {

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(httpURLConnection.getInputStream()));
        StringBuilder sb = new StringBuilder(1024);
        String tmp;
        while ((tmp = reader.readLine()) != null)
            sb.append(tmp).append("\n");
        reader.close();
        Log.d("Update participant", sb.toString());
        return sb.toString();
    }
}