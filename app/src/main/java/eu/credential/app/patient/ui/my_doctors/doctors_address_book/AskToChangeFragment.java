package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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

public class AskToChangeFragment extends DialogFragment {
    private String accountId = "HansAugust";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        GetParticipantData getParticipantData = new GetParticipantData(accountId);
        //Data from AddressBookFragment for new person
        String role = this.getArguments().getString("role");
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");

        //Data from AddressBookFragment for actual person
        String previousName = this.getArguments().getString("previousname");
        String previousSurname = this.getArguments().getString("previoussurname");
        String previousRole = this.getArguments().getString("previousrole");

        String title = "Set " + name + " " + surname + " as " + role + "?";
        String message = previousName + " " + previousSurname + " is already set as " + role +
                ". Do you want to change it to " + name + " " + surname + "?";

        String button1String = "Yes";
        String button2String = "No";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor)
                .setPositiveButton(button1String, (dialog, id) -> {

                    for (int i = 0; i < AddressBookMainFragment.listDoctor.size(); i++) {
                        if ((previousName != null && previousName.equals(name)) &&
                                (previousSurname != null && previousSurname.equals(surname)) &&
                                !AddressBookMainFragment.listDoctor.get(i).getRole().equals("null")) {
                            Toast.makeText(getActivity(), "Doctor " + name + " " + surname +
                                    " is already set as " + previousRole + ".", Toast.LENGTH_LONG).show();
                        } else if ((name != null && name.equals(AddressBookMainFragment.listDoctor.get(i).getName())) &&
                                (surname != null && surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname())) &&
                                AddressBookMainFragment.listDoctor.get(i).getRole().equals("null")) {
                            JSONArray doctorArray = new JSONArray();
                            try {
                                doctorArray = getParticipantData.execute().get();
                                for (int j = 0; j < doctorArray.length(); j++) {
                                    // TODO: Change "surname" to "id", when doctors will be get from LDAP
                                    if (doctorArray.getJSONObject(j).getString("surname").equals(previousSurname) &&
                                            doctorArray.getJSONObject(j).getString("name").equals(previousName)) {
                                        doctorArray.getJSONObject(j).put("role", "null");
                                    }
                                    if (doctorArray.getJSONObject(j).getString("surname").equals(surname) &&
                                            doctorArray.getJSONObject(j).getString("name").equals(name)) {
                                        doctorArray.getJSONObject(j).put("role", role);
                                    }
                                }
                            } catch (InterruptedException | ExecutionException | JSONException e) {
                                e.printStackTrace();
                            }
                            UpdateParticipantData updateParticipantData = new UpdateParticipantData
                                    (doctorArray, accountId, "delete");
                            updateParticipantData.execute();

                            Toast.makeText(getActivity(), "Doctor is changed.", Toast.LENGTH_SHORT).show();
                            MyDoctorsFragment fragment = new MyDoctorsFragment();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                            fragmentTransaction.replace(R.id.container, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                        } else if ((name != null && name.equals(AddressBookMainFragment.listDoctor.get(i).getName())) &&
                                (surname != null && surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname())) &&
                                !AddressBookMainFragment.listDoctor.get(i).getRole().equals("null")) {
                            Toast.makeText(getActivity(), "Doctor is already set as " +
                                    AddressBookMainFragment.listDoctor.get(i).getRole() +
                                    ". Firstly delete his role and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        builder.setNegativeButton(button2String, (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }
}