package eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.orchestration.http.RegisterPermission;

/**
 * Created by Aleksei Piatkin on 02.07.17.
 * <p>
 * This class shows a dialog fragment to show user doctors access option
 * Two buttons: Ok, Cancel.
 */
public class AskToAccessTypeDialog extends DialogFragment implements AsyncTaskCompleteListener<JSONArray> {

    // Get folder ids from DMS
    protected static String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    private boolean first = false;
    private static String doctorId;
    private static String name;
    private static String surname;
    private static JSONArray doctorArray;


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Data from AddressBookDetails
        if (this.getArguments() != null) {
            doctorId = this.getArguments().getString("id");
            name = this.getArguments().getString("name");
            surname = this.getArguments().getString("surname");
        }

        // get data for checkbox
        GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
        getParticipantData.execute();

        // dialog items
        final String[] accessTypes = {"read and edit my documents and vital data."};
        final boolean[] checkedItemsArray = {first};

        String title = "Allow doctor " + name + " " + surname;

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()));
        builder.setTitle(title)
                .setIcon(R.drawable.access)
                .setMultiChoiceItems(accessTypes, checkedItemsArray, (dialog, which, isChecked) ->
                        checkedItemsArray[which] = isChecked)
                .setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                    Confirm confirm = new Confirm(checkedItemsArray, accessTypes);
                    if (getFragmentManager() != null) {
                        confirm.show(getFragmentManager(), "ConfirmPermission");
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
        builder.setCancelable(true);
        return builder.create();
    }

    /**
     * Give to doctor specific access to private vital data
     */
    private static void setAccess(String field, String bool) {
        boolean b;
        b = bool.equals("true");
        SavePreferences.setDefaultsBoolean(field, b, PatientApp.getContext());
        for (int i = 0; i < doctorArray.length(); i++) {
            try {
                if (doctorArray.getJSONObject(i).getString("id").equals(doctorId)) {
                    doctorArray.getJSONObject(i).getJSONObject("access").put(field, bool);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ValidFragment")
    public static class Confirm extends DialogFragment {
        private boolean[] checkedItemsArray;
        private String[] accessTypes;

        public Confirm(boolean[] checkedItemsArray, String[] accessTypes) {
            this.checkedItemsArray = checkedItemsArray;
            this.accessTypes = accessTypes;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
            builder.setTitle("Permission settings")
                    .setMessage(getString(R.string.change_permission));
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                if (checkedItemsArray[0]) {
                    setAccess("editdocument", "true");
                    // Create Policy
                    RegisterPermission registerPermission = new RegisterPermission(doctorId,
                            SavePreferences.getDefaultsString("dataIdDMS2", PatientApp.getContext()), getContext());
                    registerPermission.execute();
                }
                if (!checkedItemsArray[0]) {
                    setAccess("editdocument", "false");
                    RegisterPermission registerPermission = new RegisterPermission(doctorId,
                            SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext()), getContext());
                    registerPermission.execute();
                }

                StringBuilder state = new StringBuilder();
                for (int i = 0; i < accessTypes.length; i++) {
                    state.append(accessTypes[i]);
                    if (checkedItemsArray[i])
                        state.append(" - accept\n");
                    else
                        state.append(" - not accept\n");
                }
                // update address book (delete old data and save new one)
                UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                        UpdateParticipantBuilder()
                        .array(doctorArray)
                        .dataId(dataId)
                        .operationId("delete")
                        .context(getContext()).build();
                updateParticipantData.execute();
            })
                    .setNegativeButton(getString(R.string.cancel), (dialog, id) -> dialog.cancel());
            builder.setCancelable(true);
            return builder.create();
        }
    }
    @Override
    public void onTaskComplete(JSONArray result) {
        try {
            doctorArray = result;
            // check and inform if there is no connection
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
                for (int i = 0; i < result.length(); i++) {
                    if (result.getJSONObject(i).getString("id").equals(doctorId)) {
                        JSONObject accessList = result.getJSONObject(i).getJSONObject("access");

                        if (accessList != null && accessList.getString("editdocument").equals("true")) {
                            first = true;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}