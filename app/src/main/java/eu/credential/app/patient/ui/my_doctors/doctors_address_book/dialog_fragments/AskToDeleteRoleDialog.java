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
 * Created by Aleksei Piatkin on 13.10.17.
 * <p>
 * This class shows a dialog fragment to ask user if he really wants to delete doctor's role.
 * Three buttons: Yes, No.
 */

public class AskToDeleteRoleDialog extends DialogFragment {

    private String accountId = "HansAugust";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Data from AddressBookFragment for new person
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        String role = this.getArguments().getString("role");
        int activity = this.getArguments().getInt("activity");

        String title = "Delete doctor's " + name + " " + surname  + role + " role.";
        String message = "Do you want to delete doctor's " + name + " " + surname + role + " role?";

        String button1String = "Yes";
        String button2String = "No";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor);
        builder.setPositiveButton(button1String, (dialog, id) -> {
            if(role != null && role.equals("diabetologist")){
                changeDoctorRole(name, surname, 0, activity, "null");
            }
            if(role != null && role.equals("family doctor")){
                changeDoctorRole(name, surname, 1, activity, "null");
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
    private void changeDoctorRole(String name, String surname, int position, int activity, String roleName) {
        GetParticipantData getParticipantData = new GetParticipantData(accountId);
        try {
            JSONArray doctorArray = getParticipantData.execute().get();
            for (int j = 0; j < doctorArray.length(); j++) {
                // TODO: Change "surname" to "id", when doctors will be get from LDAP
                if (doctorArray.getJSONObject(j).getString("surname").equals(surname) &&
                        doctorArray.getJSONObject(j).getString("name").equals(name)) {
                    doctorArray.getJSONObject(j).getJSONArray("role").put(position, roleName);
                }
            }
            UpdateParticipantData updateParticipantData = new UpdateParticipantData
                    (doctorArray, accountId, "delete");
            updateParticipantData.execute();
            changeFragment(new MyDoctorsFragment());
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        Toast.makeText(getActivity().getApplicationContext(), "Doctor's " +
                name + " " + surname + " role was deleted.", Toast.LENGTH_SHORT).show();
        if (activity != 1) {
            changeFragment(new MyDoctorsFragment());
        }
    }
}