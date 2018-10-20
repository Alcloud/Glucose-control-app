package eu.credential.app.patient.ui.configuration;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.Notification;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.app.patient.orchestration.http.Request;

public class NotificationList extends AppCompatActivity implements AsyncTaskCompleteListener<ArrayList<Notification>> {

    private static final String TAG = "Performance";
    private String accountId = SavePreferences.getDefaultsString("accountId", PatientApp.getContext());
    private static boolean flag = true;
    private Request request;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        Toolbar toolbar = findViewById(R.id.toolbar_notification_list);

        toolbar.setTitle(getString(R.string.notification_settings));
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("notifications.protocol")
                .host("notifications.host")
                .path("notifications.path").build();

        Switch notificationOne = findViewById(R.id.switch_notification_one);
        Switch notificationTwo = findViewById(R.id.switch_notification_two);

        boolean[] checkedItemsArray = {SavePreferences.getDefaultsBoolean("checkNewData", PatientApp.getContext()),
                SavePreferences.getDefaultsBoolean("checkDocumentAccess", PatientApp.getContext())};

        notificationOne.setChecked(checkedItemsArray[0]);
        notificationTwo.setChecked(checkedItemsArray[1]);

        notificationOne.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                long startTime = System.nanoTime();
                // Send a preference request (to save a new preference)
                request = new Request.RequestBuilder()
                        .addressURL("addPreferences")
                        .accountId(accountId)
                        .dataId("newdata")
                        .requestId("addPreference")
                        .context(NotificationList.this)
                        .callback(this).build();
                request.execute();
                long endTime = System.nanoTime();
                Log.i(TAG, "Subscribe|Subscribe to event|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");
            } else {
                long startTime = System.nanoTime();
                // Send a preference request (to delete a preference)
                request = new Request.RequestBuilder()
                        .addressURL("deletePreferences")
                        .accountId(accountId)
                        .id(SavePreferences.getDefaultsString("prefNewData", PatientApp.getContext()))
                        .dataId("newdata")
                        .requestId("deletePreference")
                        .context(NotificationList.this)
                        .callback(this).build();
                request.execute();
                long endTime = System.nanoTime();
                Log.i(TAG, "Unsubscribe|Unsubscribe from event|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");
            }
            if (flag) {
                SavePreferences.setDefaultsBoolean("checkNewData", isChecked, PatientApp.getContext());
            }
        });

        notificationTwo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                long startTime = System.nanoTime();
                // Send a preference request (to save a new preference)
                request = new Request.RequestBuilder()
                        .addressURL("addPreferences")
                        .accountId(accountId)
                        .dataId("documentaccess")
                        .requestId("addPreference")
                        .context(NotificationList.this)
                        .callback(this).build();
                request.execute();
                long endTime = System.nanoTime();
                Log.i(TAG, "Subscribe|Subscribe to event|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");
            } else {
                long startTime = System.nanoTime();
                // Send a preference request (to delete a preference)
                request = new Request.RequestBuilder()
                        .addressURL("deletePreferences")
                        .accountId(accountId)
                        .id(SavePreferences.getDefaultsString("prefDocumentAccess", PatientApp.getContext()))
                        .dataId("documentaccess")
                        .requestId("deletePreference")
                        .context(NotificationList.this)
                        .callback(this).build();
                request.execute();
                long endTime = System.nanoTime();
                Log.i(TAG, "Unsubscribe|Unsubscribe from event|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");
            }
            if (flag) {
                SavePreferences.setDefaultsBoolean("checkDocumentAccess", isChecked, PatientApp.getContext());
            }
        });
    }

    @Override
    public void onTaskComplete(ArrayList<Notification> result) {
        // check and inform if there is no connection
        if (result != null && !result.isEmpty() && result.get(0).getText().equals("error")) {
            flag = false;
            Toast.makeText(getApplicationContext(), getString(R.string.data_was_not_saved),
                    Toast.LENGTH_LONG).show();
        } else {
            flag = true;
        }
    }
}
