package eu.credential.app.patient.ui.myHealthRecords.event;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.Notification;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.Request;

/**
 * Created by Aleksei Piatkin on 28.03.18.
 * <p>
 * This fragment shows user a event list (mostly notifications).
 */
public class EventListMain extends Fragment {
    public static ArrayList<Notification> eventList = new ArrayList<>();
    private String accountId = SavePreferences.getDefaultsString("accountId", PatientApp.getContext());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list_main, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshProtocolList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Update a list of events.
     */
    public void refreshProtocolList() {
        eventList.clear();
        Request getNotificationList = new Request.RequestBuilder()
                .accountId(accountId)
                .addressURL("getNotificationList")
                .requestId("getNotificationList").build();
        try {
            eventList = getNotificationList.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}