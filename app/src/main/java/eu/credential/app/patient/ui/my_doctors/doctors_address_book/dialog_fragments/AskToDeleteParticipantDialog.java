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

public class AskToDeleteParticipantDialog extends DialogFragment {

    private String accountId = "HansAugust";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Data from AddressBookFragment for new person
        String role = this.getArguments().getString("role");
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        int activity = this.getArguments().getInt("activity");

        String title = "Remove " + name + ".";
        String message = "Do you want to remove doctor " + name + " from address book or just delete his/her " + role + " role?";

        String button1String = "Remove";
        String button2String = "Delete role";
        String button3String = "No";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor)
                .setPositiveButton(button2String, (dialog, id) -> {
                    try {
                        GetParticipantData getParticipantData = new GetParticipantData(accountId);
                        JSONArray doctorArray = getParticipantData.execute().get();
                        // delete doctor's role and save to address book
                        for (int i = 0; i < doctorArray.length(); i++) {
                            // TODO: Change "surname" to "id", when doctors will be get from LDAP
                            if (doctorArray.getJSONObject(i).getString("surname").equals(surname) &&
                                    doctorArray.getJSONObject(i).getString("name").equals(name)) {
                                doctorArray.getJSONObject(i).put("role", "null");
                            }
                        }
                        UpdateParticipantData updateParticipantData = new UpdateParticipantData
                                (doctorArray, accountId, "delete");
                        updateParticipantData.execute();
                        Toast.makeText(getActivity().getApplicationContext(), "Doctor's " +
                                name + " " + surname + " role was deleted.", Toast.LENGTH_SHORT).show();
                        if (activity != 1) {
                            changeFragment(new MyDoctorsFragment());
                        }
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                    }
                });
        builder.setNeutralButton(button1String, (dialog, id) -> {
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
        builder.setNegativeButton(button3String, (dialog, id) -> {
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