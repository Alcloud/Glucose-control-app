package eu.credential.app.patient.orchestration.http;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import eu.credential.app.patient.ui.MainActivity;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.JSONFile;

public class Request extends AsyncTask<String, Void, Void> {

    private String addressURL;
    private String accountId;
    private String id;
    private String dataId;
    private String requestId;
    private String appId;

    public String getAppId() {
        return appId;
    }

    private Context context;

    private static final String GET_PREFERENCE_URL =
            "http://194.95.174.238:8083/v1/notificationManagementService/getPreferences";

    public Request(String addressURL, String accountId, String id,
                   String dataId, String requestId, Context context) {
        super();
        this.addressURL = addressURL;
        this.accountId = accountId;
        this.id = id;
        this.dataId = dataId;
        this.requestId = requestId;
        this.context = context;
    }

    public Request(String addressURL, String id, String requestId, Context context) {
        super();
        this.addressURL = addressURL;
        this.id = id;
        this.requestId = requestId;
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject requestMessage = null;
        JSONObject responseJSON;

        try {
            URL url = new URL(addressURL);
            URL url2 = new URL(GET_PREFERENCE_URL);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            HttpURLConnection httpURLConnection2 = (HttpURLConnection) url2.openConnection();

            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");

            httpURLConnection2.setDoInput(true);
            httpURLConnection2.setDoOutput(true);
            httpURLConnection2.setRequestMethod("POST");
            httpURLConnection2.setRequestProperty("Content-Type", "application/json");

            httpURLConnection.connect();
            httpURLConnection2.connect();

            if (!checkPreferenceExistence(httpURLConnection2, dataId, accountId) && requestId.equals("addPreference")) {
                requestMessage = new JSONObject(setAddPreferenceString(accountId, dataId, appId));
            }

            if (requestId.equals("addAppId")) {
                requestMessage = new JSONObject(setAddPreferenceString(accountId, dataId, id));
            }

            if (requestId.equals("getNotification")) {
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
            }

            if (requestId.equals("deletePreference")) {
                requestMessage = new JSONObject("{\n" +
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
                        "        \"code\":\"" + dataId + "\"" +
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
                        "}");
            }

            BufferedOutputStream bos = new BufferedOutputStream(httpURLConnection.getOutputStream());
            if (requestMessage != null) {
                bos.write(requestMessage.toString().getBytes());
                bos.flush();
            }
            bos.close();

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                responseJSON = new JSONObject(jsonToString(httpURLConnection));

                if (requestId.equals("addPreference")) {

                    for (int i = 0; i < responseJSON.getJSONArray("preferenceList").length(); i++) {
                        JSONObject object = responseJSON.getJSONArray("preferenceList").getJSONObject(i);
                        if (object.getJSONObject("preferenceType").getString("code").equals("newdata")) {
                            JSONFile.saveJSONpreference(context, object.getJSONObject("preferenceId")
                                    .getString("value"), "newdata");
                        }
                        if (object.getJSONObject("preferenceType").getString("code").equals("documentaccess")) {
                            JSONFile.saveJSONpreference(context, object.getJSONObject("preferenceId")
                                    .getString("value"), "documentaccess");
                        }
                    }
                }
                if (requestId.equals("getNotification")) {
                    JSONArray array = responseJSON.getJSONObject("notification").getJSONArray("notificationDetails");
                    for (int i = 0; i < array.length(); i++) {
                        if (array.getJSONObject(i).getJSONObject("key").getString("code").equals("message")) {
                            String message = responseJSON.getJSONObject("notification")
                                    .getJSONArray("notificationDetails").getJSONObject(i).getString("value");
                            notifyMe(message);
                            Log.d("RequestLogs", "message: " + message);
                        }
                    }
                }
            } else {
                Log.d("RequestLogs", "no connection");
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
        Log.d("myLogs", sb.toString());
        return sb.toString();
    }

    // Check if a specific preference exist for an specific User
    private boolean checkPreferenceExistence(HttpURLConnection httpURLConnection,
                                             String type, String account) throws IOException, JSONException {
        boolean exist = false;
        if (account != null) {
            JSONObject requestMessage = new JSONObject("{\n" +
                    "  \"accountId\": {\n" +
                    "    \"value\":" + account +
                    "  }\n" +
                    "}");
            BufferedOutputStream bos2 = new BufferedOutputStream(httpURLConnection.getOutputStream());
            bos2.write(requestMessage.toString().getBytes());
            bos2.flush();
            bos2.close();
            JSONObject object = new JSONObject(jsonToString(httpURLConnection));
            if (!object.toString().equals("{\"preferenceList\":null}")) {
                for (int i = 0; i < object.getJSONArray("preferenceList").length(); i++) {
                    String code = object.getJSONArray("preferenceList").getJSONObject(i)
                            .getJSONObject("preferenceType").getString("code");
                    if (code.equals(type)) {
                        exist = true;
                    }
                    if (code.equals("appid")) {
                        JSONArray array = object.getJSONArray("preferenceList").getJSONObject(i)
                                .getJSONArray("preferenceDetails");
                        for (int j = 0; j < array.length(); j++) {
                            if (array.getJSONObject(j).getJSONObject("key").getString("code").equals("appid")) {
                                appId = array.getJSONObject(j).getString("value");
                            }
                        }
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

    // Create a pop-up notification with message from notification DB
    private void notifyMe(String text) {
        int requestID = (int) System.currentTimeMillis();
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                requestID, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification builder = new Notification.Builder(context)
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
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotificationID.getID(), builder);
    }

    private String setAddPreferenceString(String accountId, String dataId, String appId) {
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

    private static class NotificationID {
        private final static AtomicInteger c = new AtomicInteger(0);

        static int getID() {
            return c.incrementAndGet();
        }
    }
}