package eu.credential.app.patient.ui.myHealthRecords.protocol;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.helper.Protocol;
import eu.credential.app.patient.orchestration.http.GetAuditLogsData;

/**
 * Created by Aleksei Piatkin on 28.03.18.
 * <p>
 * This fragment shows user a protocol list of medical events.
 */
public class ProtocolListMain extends Fragment {
    public static ArrayList<Protocol> listProtocol = new ArrayList<>();
    private JSONArray protocolArray;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_protocol_list_main, container, false);
        ScrollView scrollView = v.findViewById(R.id.scroll_view_protocol);
        scrollView.setScrollbarFadingEnabled(false);
        scrollView.setScrollBarFadeDuration(0);
        return v;
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

    private void setProtocolList(int i) throws JSONException {
        Protocol protocol = new Protocol();
        protocol.setEventId(protocolArray.getJSONObject(i).getString("eventId"));
        protocol.setEventType(protocolArray.getJSONObject(i).getString("eventType"));
        protocol.setEventCreationTime(timeFormat(protocolArray.getJSONObject(i).getString("eventCreationTime")));
        protocol.setTrackingId(protocolArray.getJSONObject(i).getString("trackingId"));

        for (int j = 0; j < protocolArray.getJSONObject(i).getJSONArray("eventInformation").length(); j++) {
            switch (protocolArray.getJSONObject(i).getJSONArray("eventInformation").getJSONObject(j)
                    .getJSONObject("key").getString("code")) {
                case "requesting_user":
                    protocol.setRequestingUser(protocolArray.getJSONObject(i).getJSONArray(
                            "eventInformation").getJSONObject(j).getString("value"));
                    break;
                case "event_outcome_indicator":
                    protocol.setEventIndicator(protocolArray.getJSONObject(i).getJSONArray(
                            "eventInformation").getJSONObject(j).getString("value"));
                    break;
                case "requested_event_type":
                    protocol.setRequestedEventType(protocolArray.getJSONObject(i).getJSONArray(
                            "eventInformation").getJSONObject(j).getString("value"));
                    break;
                case "response_code":
                    protocol.setResponseCode(protocolArray.getJSONObject(i).getJSONArray(
                            "eventInformation").getJSONObject(j).getString("value"));
                    break;
            }
        }

        // TODO not nice how we do filtering here
        if (protocolArray.getJSONObject(i).getString("eventType").startsWith("PHR")) listProtocol.add(protocol);
    }


    private JSONArray sortProtocolArrayByTime(JSONArray protocolArray) {


        List<JSONObject> sortedList = new ArrayList<JSONObject>();
        JSONArray sortedArray = new JSONArray();

        try {

            for (int i = 0; i < protocolArray.length(); i++) {
                sortedList.add(protocolArray.getJSONObject(i));
            }

            Collections.sort(sortedList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    try {
                        if (Long.valueOf(o1.getString("eventCreationTime")) >= Long.valueOf(o2.getString("eventCreationTime"))) {

                            return -1;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return 0;
                }
            });

            for (int i = 0; i < sortedList.size(); i++) {
                sortedArray.put(sortedList.get(i));
            }

        } catch(Exception e) {
            e.printStackTrace();
        }
        return sortedArray;
    }


    /**
     * Update a list of protocols.
     */
    public void refreshProtocolList() {
        listProtocol.clear();
        GetAuditLogsData getAuditLogsData = new GetAuditLogsData(getContext());
        try {
            protocolArray = getAuditLogsData.execute().get();

            protocolArray = sortProtocolArrayByTime(protocolArray);

            for (int i = 0; i < protocolArray.length(); i++) {
                try {
                    if (protocolArray.getJSONObject(i).has("error")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                        builder.setIcon(android.R.drawable.ic_dialog_info);
                        builder.setTitle("Warning!");
                        builder.setMessage(getString(R.string.server_not_respond));
                        builder.setPositiveButton(getString(R.string.ok),
                                (arg0, arg1) -> {
                                });
                        builder.show();
                    } else {
                        setProtocolList(i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private String timeFormat(String time) {
        long unix_seconds = Long.parseLong(time);
        Date date = new Date(unix_seconds);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat jdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH);
        jdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return jdf.format(date);
    }
}
