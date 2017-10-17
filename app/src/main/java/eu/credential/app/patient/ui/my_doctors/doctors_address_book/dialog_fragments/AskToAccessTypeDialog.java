package eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.administrator.credential_v020.R;

import java.io.IOException;

import eu.credential.app.patient.ui.my_doctors.doctors_address_book.BookDoctorDetailsActivity;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.JSONFile;

/**
 * Created by Aleksei Piatkin on 02.07.17.
 * <p>
 * This class shows a dialog fragment to show user doctors access option
 * Three buttons: Ok, Cancel.
 */

public class AskToAccessTypeDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Data from AddressBookFragment for new person
        String name = this.getArguments().getString("name") + this.getArguments().getString("surname");

        // dialog items
        final String[] accessTypes = {"to receive my vital data", "to read my documents", "to edit my documents", "to send me messages"};
        final boolean[] checkedItemsArray = {false, false, false, true};

        String title = "Allow doctor " + name;

        String button1String = "OK";
        String button2String = "Cancel";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setIcon(R.drawable.access)
                .setMultiChoiceItems(accessTypes, checkedItemsArray, (dialog, which, isChecked) -> checkedItemsArray[which] = isChecked)
                .setPositiveButton(button1String, (dialog, id) -> {

                    StringBuilder state = new StringBuilder();
                    for (int i = 0; i < accessTypes.length; i++) {
                        state.append(accessTypes[i]);
                        if (checkedItemsArray[i])
                            state.append(" - accept\n");
                        else
                            state.append(" - not accept\n");
                    }
                    try {
                        JSONFile.saveJSONAccess(getContext(), state.toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ((BookDoctorDetailsActivity) getActivity()).refresh();
                });
        builder.setNegativeButton(button2String, (dialog, id) -> {

        });
        builder.setCancelable(true);
        return builder.create();
    }
}