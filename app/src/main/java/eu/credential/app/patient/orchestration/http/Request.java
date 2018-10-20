package eu.credential.app.patient.orchestration.http;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.app.patient.ui.MainActivity;

/**
 * Created by Aleksei Piatkin on 12.10.17.
 * <p>
 * This class helps to create, save and get relevant for notification service data.
 * Notification service data: notification, preference
 */
public class Request extends AsyncTask<String, String, ArrayList<eu.credential.app.patient.helper.Notification>> {

    private static final String TAG = "Performance";
    private long startTimeGetPref = 0;
    private AsyncTaskCompleteListener<ArrayList<eu.credential.app.patient.helper.Notification>> callback;
    private String addressURL;
    private String accountId;
    private String id;
    private String dataId;
    private String requestId;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog dialog;
    private boolean flag = true;

    private static final String fcm_app_token = "AAAADVVgij0:APA91bGTyoC1FZD3ZEpMR0-jdZuGwJ6Pdq0YAM" +
            "OL7rrf8y-oy2mjTd2Z6M88i3I6EHvTfiQ8APprR8CFj1FO5ruwizqK1Z6pDbkRZ7Isa6IU1Eku8cgHBHW5EK40oYoXcnHXtcsUpz7p";

    private Request(final RequestBuilder requestBuilder) {
        this.addressURL = requestBuilder.getAddressURL();
        this.accountId = requestBuilder.getAccountId();
        this.id = requestBuilder.getId();
        this.dataId = requestBuilder.getDataId();
        this.requestId = requestBuilder.getRequestId();
        this.context = requestBuilder.getContext();
        this.callback = requestBuilder.getCallback();
    }

    @Override
    protected void onPreExecute() {
        if (!requestId.equals("getNotification") && !requestId.equals("getNotificationList") && !requestId.equals("addAppId")
                && !requestId.equals("getPreference")) {
            dialog = ProgressDialog.show(context, "Saving settings...", "Please wait");
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    protected ArrayList<eu.credential.app.patient.helper.Notification> doInBackground(String... params) {
        ArrayList<eu.credential.app.patient.helper.Notification> eventList = new ArrayList<>();
        JSONObject requestMessage = null;
        JSONObject responseJSON;
        HttpURLConnection httpURLConnection = null;
        long startTime;
        long endTime;

        // generate error message, if there is no connection
        ArrayList<eu.credential.app.patient.helper.Notification> error = new ArrayList<>();
        eu.credential.app.patient.helper.Notification errorNotification =
                new eu.credential.app.patient.helper.Notification();
        errorNotification.setText("error");
        error.add(errorNotification);
        try {
            startTimeGetPref = System.nanoTime();

            if (requestId.equals("getPreference")) {
                initConnection("notifications.path1");
                getUserPreferenceSettings(accountId);
            }

            if (requestId.equals("addAppId")) {
                initConnection("notifications.path1");

                requestMessage = new JSONObject(setAddPreferenceRequest(accountId, dataId, id,
                        "fcm-app-token", fcm_app_token));

                httpURLConnection = SetURLConnection.setConnection("POST",
                        addressURL, requestMessage.toString());
            }

            if (requestId.equals("addPreference")) {
                initConnection("notifications.path1");

                if (!checkPreferenceExistence(dataId, accountId)) {
                    requestMessage = new JSONObject(setAddPreferenceRequest(accountId, dataId,
                            null, "pull", "yes"));
                }
                if (requestMessage != null) {
                    httpURLConnection = SetURLConnection.setConnection("POST",
                            addressURL, requestMessage.toString());
                }
            }

            if (requestId.equals("getNotification")) {
                initConnection("notifications.path2");

                requestMessage = new JSONObject("{\n" +
                        "    \"notificationId\": {\n" +
                        "        \"namespace\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                        "        \"value\": \"" + id + "\",\n" +
                        "        \"type\": {\n" +
                        "            \"type\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                        "            \"version\": \"1.0\",\n" +
                        "            \"code\": \"notificationId\",\n" +
                        "            \"display\": \"ID of a notification.\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");
                httpURLConnection = SetURLConnection.setConnection("POST",
                        addressURL, requestMessage.toString());
            }

            if (requestId.equals("getNotificationList")) {
                initConnection("notifications.path2");

                requestMessage = new JSONObject("{\n" +
                        "    \"accountId\": {\n" +
                        "        \"namespace\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                        "        \"value\": \"" + accountId + "\",\n" +
                        "        \"type\": {\n" +
                        "            \"type\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                        "            \"version\": \"1.0\",\n" +
                        "            \"code\": \"accountId\",\n" +
                        "            \"display\": \"Account ID of a CREDENTIAL user.\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");
                httpURLConnection = SetURLConnection.setConnection("POST",
                        addressURL, requestMessage.toString());
            }

            if (requestId.equals("deletePreference")) {
                initConnection("notifications.path1");

                if (id != null && !id.equals("")) {
                    requestMessage = new JSONObject(deletePreferenceRequest(accountId, id));
                    httpURLConnection = SetURLConnection.setConnection("POST",
                            addressURL, requestMessage.toString());
                } else {
                    flag = false;
                }
            }

            if (addressURL != null && requestMessage != null) {
                startTime = System.nanoTime();

                endTime = System.nanoTime();
                Log.i(TAG, "-|Manage preferences or notification|" + SetURLConnection.setURL()
                        + "/" + addressURL + "|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());

                Log.i("EventsLog", "EventsLog: " + httpURLConnection.getResponseCode());

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    publishProgress("Connection successful");

                    responseJSON = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));

                    if (requestId.equals("addPreference")) {

                        for (int i = 0; i < responseJSON.getJSONArray("preferenceList")
                                .length(); i++) {
                            JSONObject object = responseJSON.getJSONArray("preferenceList")
                                    .getJSONObject(i);
                            if (object.getJSONObject("preferenceType").getString("code")
                                    .equals("newdata")) {
                                SavePreferences.setDefaultsString("prefNewData", object
                                        .getJSONObject("preferenceId")
                                        .getString("value"), PatientApp.getContext());
                            }
                            if (object.getJSONObject("preferenceType").getString("code")
                                    .equals("documentaccess")) {
                                SavePreferences.setDefaultsString("prefDocumentAccess",
                                        object.getJSONObject("preferenceId")
                                                .getString("value"), PatientApp.getContext());
                            }
                        }
                    }
                    if (requestId.equals("getNotification")) {
                        JSONArray array = responseJSON.getJSONObject("notification")
                                .getJSONArray("notificationDetails");

                        for (int i = 0; i < array.length(); i++) {
                            if (array.getJSONObject(i).getJSONObject("key").getString("code")
                                    .equals("message")) {
                                String message = responseJSON.getJSONObject("notification")
                                        .getJSONArray("notificationDetails").getJSONObject(i)
                                        .getString("value");
                                notifyMe(message);
                                Log.d("RequestLogs", "message: " + message);
                            }
                        }
                    }
                    if (requestId.equals("getNotificationList")) {
                        JSONArray notificationListArray = responseJSON.getJSONArray("notificationList");

                        for (int i = 0; i < notificationListArray.length(); i++) {
                            eu.credential.app.patient.helper.Notification notification =
                                    new eu.credential.app.patient.helper.Notification();

                            JSONObject object = notificationListArray.getJSONObject(i);
                            notification.setDate(object.getString("notificationCreationTime"));

                            JSONArray notificationDetailsArray = object.getJSONArray("notificationDetails");

                            for (int j = 0; j < notificationDetailsArray.length(); j++) {
                                if (notificationDetailsArray.getJSONObject(j).getJSONObject("key")
                                        .getString("code").equals("message")) {
                                    String message = notificationDetailsArray.getJSONObject(j)
                                            .getString("value");
                                    notification.setText(message);
                                    Log.d("getNotificationListRequestLogs", "message: " + message);
                                }
                            }
                            eventList.add(notification);
                        }
                    }
                } else {
                    flag = false;
                    Log.d("RequestLogs", "no connection");
                }
            }

        } catch (IOException | JSONException e) {
            flag = false;
            e.printStackTrace();
        }
        if (!flag) {
            return error;
        } else return eventList;
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
    protected void onPostExecute(ArrayList<eu.credential.app.patient.helper.Notification> result) {
        if (!requestId.equals("getNotification") && !requestId.equals("getNotificationList") &&
                !requestId.equals("addAppId") && !requestId.equals("getPreference")) {
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

    // Check if a specific preference exist for an specific User
    private boolean checkPreferenceExistence(String type, String account) throws IOException, JSONException {
        boolean exist = false;
        String code;

        if (account != null) {
            JSONObject object = getPreferenceAsJSONobject(account);
            if (!object.toString().equals("{\"preferenceList\":null}")) {

                for (int i = 0; i < object.getJSONArray("preferenceList").length(); i++) {
                    code = object.getJSONArray("preferenceList").getJSONObject(i)
                            .getJSONObject("preferenceType").getString("code");
                    if (code.equals(type)) {
                        exist = true;
                    }
                }
            } else {
                exist = false;
            }
        } else {
            exist = false;
        }
        return exist;
    }

    private void getUserPreferenceSettings(String account) throws IOException, JSONException {
        String code;
        String preferenceId;
        String appidPrevious = "";
        String appidNew = SavePreferences.getDefaultsString("appid", PatientApp.getContext());
        JSONObject object = getPreferenceAsJSONobject(account);
        if (!object.toString().equals("{\"preferenceList\":null}")) {
            for (int i = 0; i < object.getJSONArray("preferenceList").length(); i++) {
                code = object.getJSONArray("preferenceList").getJSONObject(i)
                        .getJSONObject("preferenceType").getString("code");
                preferenceId = object.getJSONArray("preferenceList").getJSONObject(i)
                        .getJSONObject("preferenceId").getString("value");
                if (code.equals("ids")) {
                    SavePreferences.setDefaultsString("prefIds", preferenceId, PatientApp.getContext());
                    JSONArray array = object.getJSONArray("preferenceList").getJSONObject(i)
                            .getJSONArray("preferenceDetails");
                    for (int j = 0; j < array.length(); j++) {
                        if (array.getJSONObject(j).getJSONObject("key").getString("code").equals("appid")) {
                            appidPrevious = array.getJSONObject(j).getString("value");
                        }
                    }
                    if (!appidNew.equals(appidPrevious)) {
                        JSONObject requestDel = new JSONObject(deletePreferenceRequest(accountId,
                                SavePreferences.getDefaultsString("prefIds", PatientApp.getContext())));

                        long startTime = System.nanoTime();
                        HttpURLConnection httpURLConnectionDel = SetURLConnection.setConnection("POST",
                                "deletePreferences", requestDel.toString());

                        long endTime = System.nanoTime();
                        Log.i(TAG, "-|Delete preference|" + SetURLConnection.setURL()
                                + "/deletePreferences" + "|-|" + startTime / 1000000 + "|" +
                                (endTime - startTimeGetPref) / 1000000
                                + "|" + httpURLConnectionDel.getResponseCode());

                        if (httpURLConnectionDel.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            JSONObject requestAdd = new JSONObject(setAddPreferenceRequest
                                    (accountId, "ids", appidNew, "fcm-app-token", fcm_app_token));

                            long startTimeAdd = System.nanoTime();
                            HttpURLConnection httpURLConnectionAdd = SetURLConnection.setConnection("POST",
                                    "addPreferences", requestAdd.toString());

                            endTime = System.nanoTime();
                            Log.i(TAG, "-|Add preference|" + SetURLConnection.setURL()
                                    + "/addPreferences" + "|-|" + startTimeAdd / 1000000 + "|"
                                    + (endTime - startTimeGetPref) / 1000000
                                    + "|" + httpURLConnectionAdd.getResponseCode());
                        } else {
                            flag = false;
                        }
                    }
                }
                if (code.equals("newdata")) {
                    SavePreferences.setDefaultsBoolean("checkNewData", true, PatientApp.getContext());
                    SavePreferences.setDefaultsString("prefNewData", preferenceId, PatientApp.getContext());
                }
                if (code.equals("documentaccess")) {
                    SavePreferences.setDefaultsBoolean("checkDocumentAccess", true, PatientApp.getContext());
                    SavePreferences.setDefaultsString("prefDocumentAccess", preferenceId, PatientApp.getContext());
                }
            }
        }
    }

    private JSONObject getPreferenceAsJSONobject(String account) throws JSONException, IOException {
        JSONObject requestMessage = new JSONObject("{\n" +
                "  \"accountId\": {\n" +
                "    \"value\":" + account +
                "  }\n" +
                "}");
        JSONObject error = new JSONObject("{\"error\": \"true\"}");

        HttpURLConnection httpURLConnectionGet = SetURLConnection.setConnection("POST",
                "getPreferences", requestMessage.toString());
        flag = httpURLConnectionGet.getResponseCode() == HttpURLConnection.HTTP_OK;
        long endTime = System.nanoTime();
        Log.i(TAG, "-|Get preference|" + SetURLConnection.setURL() + "/getPreferences" + "|-|" +
                startTimeGetPref / 1000000 + "|" + (endTime - startTimeGetPref) / 1000000 + "|"
                + httpURLConnectionGet.getResponseCode());
        if (!flag) {
            return error;
        } else return new JSONObject(SetURLConnection.jsonToString(httpURLConnectionGet));
    }

    // Create a pop-up notification with message from notification DB
    private void notifyMe(String text) {

        String channelId = "default_channel_id";
        String channelDescription = "CREDENTIAL Channel";

        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestID, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context)
                    .setContentIntent(contentIntent)
                    .setContentTitle("Credential")
                    .setTicker("Credential message")
                    .setContentText(text)
                    .setSmallIcon(R.drawable.doctor)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .setChannelId(channelId)
                    .build();
        } else {
            builder = new Notification.Builder(context)
                    .setContentIntent(contentIntent)
                    .setContentTitle("Credential")
                    .setTicker("Credential message")
                    .setContentText(text)
                    .setSmallIcon(R.drawable.doctor)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_SOUND)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setStyle(new Notification.BigTextStyle().bigText(text))
                    .build();
        }

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        //Check if notification channel exists and if not create one
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(channelId);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(channelId, channelDescription, importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(NotificationID.getID(), builder);
        }
    }

    private String setAddPreferenceRequest(String accountId, String dataId, String appId, String code, String value) {
        return "{\n" +
                "  \"preferenceList\": [\n" +
                "    {\n" +
                "      \"accountId\": {\n" +
                "        \"value\":\"" + accountId + "\"" +
                "      },\n" +
                "      \"preferenceType\": {\n" +
                "        \"system\": \"https://credential.eu/config/codesystems/preferencetypes\",\n" +
                "        \"code\":\"" + dataId + "\"" +
                "      },\n" +
                "      \"preferenceDetails\": [\n" +
                "        {\n" +
                "          \"key\": {\n" +
                "            \"system\": \"https://credential.eu/config/codesystems/preferenceitemkey\",\n" +
                "            \"code\": \"appid\"\n" +
                "          },\n" +
                "          \"value\":\"" + appId + "\"" +
                "        },\n" +
                "        {\n" +
                "          \"key\": {\n" +
                "            \"system\": \"https://credential.eu/config/codesystems/preferenceitemkey\",\n" +
                "            \"code\": \"" + code + "\"\n" +
                "          },\n" +
                "          \"value\": \"" + value + "\"\n" +
                "          \n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String deletePreferenceRequest(String accountId, String id) {
        return "{\n" +
                "  \"preferenceList\": [\n" +
                "    {\n" +
                "      \"accountId\": {\n" +
                "        \"value\":\"" + accountId + "\"" +
                "      },\n" +
                "      \"preferenceId\": {\n" +
                "                \"type\": {\n" +
                "                    \"system\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                "                    \"version\": null,\n" +
                "                    \"code\": \"preferenceId\",\n" +
                "                    \"display\": null\n" +
                "                },\n" +
                "                \"namespace\": \"https://credential.eu/config/codesystems/identifiers\",\n" +
                "                \"value\":\"" + id + "\"" +
                "            },\n" +
                "      \"preferenceType\": {\n" +
                "        \"system\": \"https://credential.eu/config/codesystems/preferencetypes\",\n" +
                "        \"code\": null" +
                "      },\n" +
                "      \"preferenceDetails\": [\n" +
                "        {\n" +
                "          \n" +
                "          \"key\": {\n" +
                "            \"system\": \"https://credential.eu/config/codesystems/preferenceitemkey\",\n" +
                "            \"code\": \"pull\"\n" +
                "          },\n" +
                "          \"value\": \"yes\"\n" +
                "          \n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private void initConnection(String path) {
        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("notifications.protocol")
                .host("notifications.host")
                .port("notifications.port")
                .path(path)
                .ssoToken(SavePreferences.getDefaultsString("ssoToken", PatientApp.getContext()))
                .build();
    }

    private static class NotificationID {
        private final static AtomicInteger c = new AtomicInteger(0);

        static int getID() {
            return c.incrementAndGet();
        }
    }

    public static class RequestBuilder {
        private String addressURL;
        private String accountId;
        private String id;
        private String dataId;
        private String requestId;
        private Context context;
        private AsyncTaskCompleteListener<ArrayList<eu.credential.app.patient.helper.Notification>> callback;

        public RequestBuilder id(final String id) {
            this.id = id;
            return this;
        }

        public RequestBuilder addressURL(final String addressURL) {
            this.addressURL = addressURL;
            return this;
        }

        public RequestBuilder accountId(final String accountId) {
            this.accountId = accountId;
            return this;
        }

        public RequestBuilder dataId(final String dataId) {
            this.dataId = dataId;
            return this;
        }

        public RequestBuilder requestId(final String requestId) {
            this.requestId = requestId;
            return this;
        }

        public RequestBuilder context(final Context context) {
            this.context = context;
            return this;
        }

        public RequestBuilder callback(final AsyncTaskCompleteListener<ArrayList<eu.credential.app.patient.helper.Notification>> callback) {
            this.callback = callback;
            return this;
        }

        public String getId() {
            return id;
        }

        String getAddressURL() {
            return addressURL;
        }

        public String getAccountId() {
            return accountId;
        }

        public String getDataId() {
            return dataId;
        }

        String getRequestId() {
            return requestId;
        }

        public Context getContext() {
            return context;
        }

        AsyncTaskCompleteListener<ArrayList<eu.credential.app.patient.helper.Notification>> getCallback() {
            return callback;
        }

        public Request build() {
            return new Request(this);
        }
    }
}