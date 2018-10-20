package eu.credential.app.patient.orchestration.firebase;

/*
 * Created by Aleksei Piatkin on 27.05.17.
 * This class get a notification data from firebase cloud, manage it and send to notification
 * service to get a specific text message for an user.
 */

import android.annotation.SuppressLint;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import eu.credential.app.patient.helper.Notification;
import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.app.patient.orchestration.http.Request;

import com.example.administrator.credential_v020.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @SuppressLint("WrongThread")
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String accountId = "";
        String notificationId = "";

        Object obj1 = remoteMessage.getData().get("accountId");
        Object obj2 = remoteMessage.getData().get("notificationId");

        if (obj1 != null) {
            accountId = obj1.toString();
        }
        if (obj2 != null) {
            notificationId = obj2.toString();
        }

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("notifications.protocol")
                .host("notifications.host")
                .path("notifications.path").build();

        // Send a notification request (to get a specific notification by notificationId)
        Request request = new Request.RequestBuilder()
                .addressURL("getNotification")
                .id(notificationId)
                .requestId("getNotification")
                .context(getApplicationContext()).build();
        ArrayList<Notification> errorResponse = null;
        try {
            errorResponse = request.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        // check and inform if there is any connection error
        if (!errorResponse.isEmpty() && errorResponse.get(0).getText().equals("error")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getApplicationContext()));
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle("Warning!");
            builder.setMessage(getString(R.string.server_not_respond));
            builder.setPositiveButton(getString(R.string.ok),
                    (arg0, arg1) -> {
                    });
            builder.show();
        }
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Log.d(TAG, "accountId: " + accountId);
            Log.d(TAG, "notificationId: " + notificationId);
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "accountId: " + remoteMessage.getNotification().getBody());
        }
    }
}