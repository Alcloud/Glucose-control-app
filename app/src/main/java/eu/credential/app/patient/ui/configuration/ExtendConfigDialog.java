package eu.credential.app.patient.ui.configuration;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

import com.example.administrator.credential_v020.R;

import java.io.IOException;

import eu.credential.app.patient.orchestration.http.Request;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.JSONFile;

public class ExtendConfigDialog extends DialogFragment {

    private static final String ADD_PREFERENCE_URL =
            "http://194.95.174.238:8083/v1/notificationManagementService/addPreferences";
    private static final String DELETE_PREFERENCE_URL =
            "http://194.95.174.238:8083/v1/notificationManagementService/deletePreferences";

    private static String preferenceIdNewData = "";
    private static String preferenceIdDocumentAccess = "";

    // A temporary solution to get accountId
    private String accountId = "HansAugust";

    public static ExtendConfigDialog newInstance() {
        ExtendConfigDialog f = new ExtendConfigDialog();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Retrieve saved configuration for checkBox
        SharedPreferences sharedPreference = getContext().getSharedPreferences("PREFERENCE_NAME", Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefEditor = sharedPreference.edit();

        // get a preferenceId
        try {
            if (!JSONFile.readJSONpreference(getContext(), "newdata").equals("")) {
                preferenceIdNewData = JSONFile.readJSONpreference(getContext(), "newdata");
            }
            if (!JSONFile.readJSONpreference(getContext(), "newdata").equals("")) {
                preferenceIdDocumentAccess = JSONFile.readJSONpreference(getContext(), "documentaccess");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String[] confArray = {getString(R.string.checkbox_new_data), getString(R.string.checkbox_contacts)};
        boolean[] checkedItemsArray = {sharedPreference.getBoolean("0", false), sharedPreference.getBoolean("1", false)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.extend_conf)
                .setMultiChoiceItems(confArray, checkedItemsArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItemsArray[which] = isChecked;
                        sharedPrefEditor.putBoolean(String.valueOf(which), isChecked);
                        sharedPrefEditor.commit();
                    }
                })
                .setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (checkedItemsArray[0]) {
                            // Send a preference request (to save a new preference)
                            Request request = new Request(ADD_PREFERENCE_URL, accountId, null,
                                    "newdata", "addPreference", getContext());
                            request.execute();
                        }
                        if (checkedItemsArray[1]) {

                            Request request = new Request(ADD_PREFERENCE_URL, accountId, null,
                                    "documentaccess", "addPreference", getContext());
                            request.execute();
                        }
                        if (!checkedItemsArray[0]) {
                            // Send a preference request (to delete a preference)
                            Request request = new Request(DELETE_PREFERENCE_URL, accountId,
                                    preferenceIdNewData, "newdata", "deletePreference", getContext());
                            request.execute();
                        }
                        if (!checkedItemsArray[1]) {

                            Request request = new Request(DELETE_PREFERENCE_URL, accountId,
                                    preferenceIdDocumentAccess, "documentaccess", "deletePreference", getContext());
                            request.execute();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.reject),
                        (dialog, id) -> dialog.cancel());

        return builder.create();
    }
}
