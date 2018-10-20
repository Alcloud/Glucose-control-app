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
 * Created by Aleksei Piatkin on 02.07.17.
 * <p>
 * This class shows a dialog fragment to ask user if he really wants to delete doctor from address book.
 * Three buttons: Yes, No.
 */
public class AskToDeleteParticipantDialog extends DialogFragment implements AsyncTaskCompleteListener<JSONArray> {

    private static final String TAG = "Performance";
    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());
    private String userId;
    private int activity;
    private long startTime;
    private FragmentActivity context;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        startTime = System.nanoTime();

        //Data from AddressBookFragment for new person
        userId = this.getArguments().getString("id");
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        activity = this.getArguments().getInt("activity");
        context = getActivity();

        String title = "Remove doctor " + name + " " + surname + ".";
        String message = "Do you want to remove doctor " + name + " " + surname +
                " from address book?\nAll existing permissions will be deleted.";

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor);
        builder.setPositiveButton(getString(R.string.yes), (dialog, id) -> {
            GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
            getParticipantData.execute();

            // Delete Policy
            RegisterPermission registerPermission = new RegisterPermission(userId,
                    SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext()), getContext());
            registerPermission.execute();
        });
        builder.setNegativeButton(getString(R.string.no), (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }
    @Override
    public void onTaskComplete(JSONArray result) {
        try {
            // check and inform if there is no connection
            if (result.getJSONObject(0).has("error")) {
                AlertDialog.Builder builderAlert = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builderAlert.setIcon(android.R.drawable.ic_dialog_info);
                builderAlert.setTitle("Warning!");
                builderAlert.setMessage(getString(R.string.server_not_respond));
                builderAlert.setPositiveButton(getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builderAlert.show();
            } else {
                for (int i = 0; i < result.length(); i++) {
                    if (result.getJSONObject(i).getString("id").equals(userId)) {
                        result.remove(i);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // delete doctor's role and save to address book
        UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                UpdateParticipantBuilder()
                .array(result)
                .dataId(dataId)
                .operationId("delete").build();
        updateParticipantData.execute();

        long endTime = System.nanoTime();
        Log.i(TAG, "Delete doctor from address book|Delete doctor from address book|" +
                "mix|-|" + startTime / 1000000 + "|" +
                (endTime - startTime) / 1000000 + "|" + "-");

        if (activity != 1 && activity != 2) {
            final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                    .fragment(new MyDoctorsFragment())
                    .fragmentActivity(context).build();
            transmitData.changeFragment(R.id.container);
        }
    }
}