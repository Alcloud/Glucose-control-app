package eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.RegisterPermission;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;

/**
 * Created by Aleksei Piatkin on 13.10.17.
 * <p>
 * This class shows a dialog fragment to ask user if he really wants to delete doctor's role.
 * Two buttons: Yes, No.
 */
public class AskToDeleteRoleDialog extends DialogFragment implements AsyncTaskCompleteListener<JSONArray> {

    private static final String TAG = "Performance";
    private long startTime;
    private String userId;
    private int position;
    private int activity;
    private String role;
    private FragmentActivity fragmentActivity;
    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        startTime = System.nanoTime();

        fragmentActivity = getActivity();
        //Data from AddressBookFragment for new person
        userId = this.getArguments().getString("id");
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        role = this.getArguments().getString("role");
        activity = this.getArguments().getInt("activity");

        String title = "Delete doctor's " + name + " " + surname + role + " role.";
        String message = "Do you want to delete doctor's " + name + " " + surname + " " + role +
                " role?\nAll existing permissions will be deleted.";

        AlertDialog.Builder builder = new AlertDialog.Builder(fragmentActivity);
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor);
        builder.setPositiveButton(getString(R.string.yes), (dialog, id) -> {
            if (role != null && role.equals("diabetologist")) {
                position = 0;
                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                getParticipantData.execute();

                RegisterPermission registerPermission = new RegisterPermission(userId,
                        SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext()), getContext());
                registerPermission.execute();
            }
            if (role != null && role.equals("family doctor")) {
                position = 1;
                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                getParticipantData.execute();

                RegisterPermission registerPermission = new RegisterPermission(userId,
                        SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext()), getContext());
                registerPermission.execute();
            }
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }

    @Override
    public void onTaskComplete(JSONArray result) {
        // check and inform if there is no connection
        try {
            if (result.getJSONObject(0).has("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(getString(R.string.server_not_respond));
                builder.setPositiveButton(getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                for (int j = 0; j < result.length(); j++) {
                    if (result.getJSONObject(j).getString("id").equals(userId)) {
                        result.getJSONObject(j).getJSONArray("role").put(position, "null");
                        result.getJSONObject(j).getJSONObject("access").put("editdocument", false);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                UpdateParticipantBuilder()
                .array(result)
                .dataId(dataId)
                .operationId("delete")
                .context(getContext())
                .build();
        updateParticipantData.execute();

        if (activity != 2) {
            final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                    .fragment(new MyDoctorsFragment()).fragmentActivity(fragmentActivity).build();
            transmitData.changeFragment(R.id.container);
        }
        long endTime = System.nanoTime();
        Log.i(TAG, "Delete doctors <role>|Change doctor from " +
                "address book to <role>|mix|-|" + startTime / 1000000 + "|" +
                (endTime - startTime) / 1000000 + "|" + "-");
    }
}