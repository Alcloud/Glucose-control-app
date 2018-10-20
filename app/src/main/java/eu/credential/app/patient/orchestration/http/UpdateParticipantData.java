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
import java.util.ArrayList;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;

/**
 * Created by Aleksei Piatkin on 13.11.17.
 * <p>
 * This class helps to update participant data.
 * Participant data: address book, user details
 */
public class UpdateParticipantData extends AsyncTask<String, String, Void> {

    private static final String TAG = "Performance";

    private String doctorId;
    private String doctorName;
    private String doctorSurname;
    private String doctorMainRole;
    private String doctorRole;
    private String doctorCity;
    private String dataId;
    private String operationId;
    private String[] fileContent;
    private JSONArray array;
    private ProgressDialog dialog;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ArrayList<String> arrayList = new ArrayList<>();

    private UpdateParticipantData(final UpdateParticipantBuilder updateParticipantBuilder) {
        this.doctorId = updateParticipantBuilder.getDoctorId();
        this.doctorName = updateParticipantBuilder.getDoctorName();
        this.doctorSurname = updateParticipantBuilder.getDoctorSurname();
        this.doctorMainRole = updateParticipantBuilder.getDoctorMainRole();
        this.doctorRole = updateParticipantBuilder.getDoctorRole();
        this.doctorCity = updateParticipantBuilder.getDoctorCity();
        this.dataId = updateParticipantBuilder.getDataId();
        this.operationId = updateParticipantBuilder.getOperationId();
        this.fileContent = updateParticipantBuilder.getFileContent();
        this.array = updateParticipantBuilder.getArray();
        this.context = updateParticipantBuilder.getContext();
    }

    @Override
    protected void onPreExecute() {
        if (context != null) {
            dialog = ProgressDialog.show(context, "Saving settings...", "Please wait");
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject requestMessage;
        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("dms.protocol")
                .host("dms.host")
                .path("dms.path").build();
        try {
            long startTime = System.nanoTime();

            if (operationId.equals("addressBook") || operationId.equals("delete")) {
                for (int i = 0; i < array.length(); i++) {
                    String object = "\"{" + "\\" + "\"id" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getString("id") + "\\" + "\"," +
                            "\\" + "\"name" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getString("name") + "\\" + "\"," +
                            "\\" + "\"surname" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getString("surname") + "\\" + "\"," +
                            "\\" + "\"city" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getString("city") + "\\" + "\"," +
                            "\\" + "\"mainrole" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getString("mainrole") + "\\" + "\"," +
                            "\\" + "\"role" + "\\" + "\": [" + "\\" + "\"" + array.getJSONObject(i).getJSONArray("role").getString(0) +
                            "\\" + "\"," + "\\" + "\"" + array.getJSONObject(i).getJSONArray("role").getString(1) + "\\" + "\"" + "]," +
                            "\\" + "\"access" + "\\" + "\":{" +
                            "\\" + "\"editdocument" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getJSONObject("access").getString("editdocument") + "\\" + "\"," +
                            "\\" + "\"sendmessage" + "\\" + "\":" + "\\" + "\"" + array.getJSONObject(i).getJSONObject("access").getString("sendmessage") + "\\" + "\"}}\"";
                    arrayList.add(object);
                }
                if (operationId.equals("addressBook")) {
                    String newObject = "\"{" + "\\" + "\"id" + "\\" + "\":" + "\\" + "\"" + doctorId + "\\" + "\"," +
                            "\\" + "\"name" + "\\" + "\":" + "\\" + "\"" + doctorName + "\\" + "\"," +
                            "\\" + "\"surname" + "\\" + "\":" + "\\" + "\"" + doctorSurname + "\\" + "\"," +
                            "\\" + "\"city" + "\\" + "\":" + "\\" + "\"" + doctorCity + "\\" + "\"," +
                            "\\" + "\"mainrole" + "\\" + "\":" + "\\" + "\"" + doctorMainRole + "\\" + "\"," +
                            "\\" + "\"role" + "\\" + "\": [" + "\\" + "\"" + doctorRole + "\\" + "\"," + "\\" + "\"" + doctorRole + "\\" + "\"" + "]," +
                            "\\" + "\"access" + "\\" + "\":{" +
                            "\\" + "\"editdocument" + "\\" + "\":" + "\\" + "\"false" + "\\" + "\"," +
                            "\\" + "\"sendmessage" + "\\" + "\":" + "\\" + "\"false" + "\\" + "\"}}\"";
                    arrayList.add(newObject);
                    Log.d("Update_participant", "doctorArray: " + arrayList.toString());
                }
            }
            if (operationId.equals("phrKey")) {
                arrayList.clear();
                for (String aFileContent : fileContent) {
                    arrayList.add("\"" + aFileContent + "\"");
                }
            }
            if (operationId.equals("userDetails")) {
                String userDetails = "\"{" + "\\" + "\"name" + "\\" + "\":" + "\\" + "\"" + doctorName +
                        "\\" + "\"," + "\\" + "\"surname" + "\\" + "\":" + "\\" + "\"" + doctorSurname +
                        "\\" + "\"," + "\\" + "\"city" + "\\" + "\":" + "\\" + "\"" + doctorCity +
                        "\\" + "\"," + "\\" + "\"email" + "\\" + "\":" + "\\" + "\"" + doctorMainRole + "\\" + "\"}\"";
                arrayList.clear();
                arrayList.add(userDetails);
            }

            requestMessage = new JSONObject("{\"fileContent\":" + arrayList.toString() + "}");

            HttpURLConnection httpURLConnection = SetURLConnection.setConnection("POST",
                    "data/" + dataId, requestMessage.toString());

            Log.d("Update_participant", "requestMessage: " + requestMessage.toString());
            long endTime = System.nanoTime();
            Log.i(TAG, "-|Update participant|" + SetURLConnection.setURL() + "/data/" + "|"
                    + dataId + "|" + startTime / 1000000 + "|" + (endTime - startTime) / 1000000 +
                    "|" + httpURLConnection.getResponseCode());

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                publishProgress("Connection successful");
                if (operationId.equals("userDetails")) {
                    SavePreferences.setDefaultsString("userName", doctorName, PatientApp.getContext());
                    SavePreferences.setDefaultsString("userSurname", doctorSurname, PatientApp.getContext());
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (values[0] != null && dialog != null) {
            dialog.setMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    public static class UpdateParticipantBuilder {
        private String doctorId;
        private String doctorName;
        private String doctorSurname;
        private String doctorMainRole;
        private String doctorRole;
        private String doctorCity;
        private String dataId;
        private String operationId;
        private Context context;
        private String[] fileContent;
        private JSONArray array;

        public UpdateParticipantBuilder doctorId(final String doctorId) {
            this.doctorId = doctorId;
            return this;
        }

        public UpdateParticipantBuilder doctorName(final String doctorName) {
            this.doctorName = doctorName;
            return this;
        }

        public UpdateParticipantBuilder doctorSurname(final String doctorSurname) {
            this.doctorSurname = doctorSurname;
            return this;
        }

        public UpdateParticipantBuilder doctorMainRole(final String doctorMainRole) {
            this.doctorMainRole = doctorMainRole;
            return this;
        }

        public UpdateParticipantBuilder doctorRole(final String doctorRole) {
            this.doctorRole = doctorRole;
            return this;
        }

        public UpdateParticipantBuilder doctorCity(final String doctorCity) {
            this.doctorCity = doctorCity;
            return this;
        }

        public UpdateParticipantBuilder dataId(final String dataId) {
            this.dataId = dataId;
            return this;
        }

        public UpdateParticipantBuilder operationId(final String operationId) {
            this.operationId = operationId;
            return this;
        }

        UpdateParticipantBuilder fileContent(final String[] fileContent) {
            this.fileContent = fileContent;
            return this;
        }

        public UpdateParticipantBuilder array(final JSONArray array) {
            this.array = array;
            return this;
        }

        public UpdateParticipantBuilder context(final Context context) {
            this.context = context;
            return this;
        }

        String getDoctorId() {
            return doctorId;
        }

        String getDoctorName() {
            return doctorName;
        }

        String getDoctorSurname() {
            return doctorSurname;
        }

        public String getDataId() {
            return dataId;
        }

        String getDoctorMainRole() {
            return doctorMainRole;
        }

        String getDoctorRole() {
            return doctorRole;
        }

        String getDoctorCity() {
            return doctorCity;
        }

        public String getOperationId() {
            return operationId;
        }

        String[] getFileContent() {
            return fileContent;
        }

        public JSONArray getArray() {
            return array;
        }

        public Context getContext() {
            return context;
        }

        public UpdateParticipantData build() {
            return new UpdateParticipantData(this);
        }
    }
}