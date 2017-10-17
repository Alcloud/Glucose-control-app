package eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;

/**
 * Created by Aleksei Piatkin on 02.07.17.
 * <p>
 * This class shows a dialog fragment to ask user if he really wants to delete doctor from address book.
 * Three buttons: Yes, No.
 */

public class AskToDeleteParticipantDialog extends DialogFragment {

    private String accountId = "HansAugust";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Data from AddressBookFragment for new person
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        int activity = this.getArguments().getInt("activity");

        String title = "Delete " + name + " " + surname + ".";
        String message = "Do you want to delete doctor " + name + " " + surname + " from address book?";

        String button1String = "Yes";
        String button2String = "No";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor);
        builder.setPositiveButton(button1String, (dialog, id) -> {
            GetParticipantData getParticipantData = new GetParticipantData(accountId);
            JSONArray doctorArray = null;
            try {
                doctorArray = getParticipantData.execute().get();
                for (int i = 0; i < doctorArray.length(); i++) {
                    // TODO: Change "surname" to "id", when doctors will be get from LDAP
                    if (doctorArray.getJSONObject(i).getString("surname").equals(surname) &&
                            doctorArray.getJSONObject(i).getString("name").equals(name)) {
                        doctorArray.remove(i);
                    }
                }
            } catch (InterruptedException | ExecutionException | JSONException e) {
                e.printStackTrace();
            }
            // delete doctor's role and save to address book
            UpdateParticipantData updateParticipantData = new UpdateParticipantData
                    (doctorArray, accountId, "delete");
            updateParticipantData.execute();
            Toast.makeText(getActivity().getApplicationContext(), "Doctor " +
                    name + " " + surname + " was removed from address book", Toast.LENGTH_SHORT).show();
            if (activity != 1) {
                changeFragment(new MyDoctorsFragment());
            }
        });
        builder.setNegativeButton(button2String, (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}