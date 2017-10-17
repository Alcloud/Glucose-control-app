package eu.credential.app.patient.orchestration.firebase;

/*
  Created by Aleksei Piatkin on 24.03.2017.
 */

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.orchestration.http.Request;
import eu.credential.app.patient.ui.clinical.ClinicalMainFragment;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final String URL =
            "http://194.95.174.238:8081/v1/notificationRetrievalService/getNotification";

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, ClinicalMainFragment.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_menu_gallery)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_menu_gallery))
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //sendNotification(remoteMessage.getNotification().getBody());
        String accountId = "";
        String notificationId = "";

        Object obj1 = remoteMessage.getData().get("accountId");
        Object obj2 = remoteMessage.getData().get("notificationId");

        if (obj1 != null) {
            accountId = obj1.toString();
            Log.d(TAG, "accountId: " + accountId);
        }
        if (obj2 != null) {
            notificationId = obj2.toString();
            Log.d(TAG, "notificationId: " + notificationId);
        }

        // Send a notification request (to get a specific notification by notificationId)
        Request request = new Request(URL, notificationId, "getNotification", getApplicationContext());
        request.execute();

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