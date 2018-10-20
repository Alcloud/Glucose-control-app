package eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Objects;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.RegisterPermission;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.AddressBookMainFragment;
import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.ui.my_doctors.doctors_list.DoctorsFromAddressBookMainFragment;

/**
 * Created by Aleksei Piatkin on 02.07.17.
 * <p>
 * This class shows a dialog fragment to ask user if he really wants to change doctor's role.
 * Two buttons: Yes, No.
 */
public class AskToChangeFragment extends DialogFragment implements AsyncTaskCompleteListener<JSONArray> {

    private static final String TAG = "Performance";

    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());
    private ArrayList<Doctor> doctorList;
    private static boolean permission = false;
    private String doctorId;
    private String previousDoctorId;
    private String role;
    private FragmentActivity context;

    int activity;
    private long startTime;
    AlertDialog.Builder builder;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        startTime = System.nanoTime();
        context = getActivity();
        GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);

        //Data from DoctorsFromAddressBookMainFragment for new person
        doctorId = this.getArguments().getString("id");
        previousDoctorId = this.getArguments().getString("previousid");
        role = this.getArguments().getString("role");
        String name = this.getArguments().getString("name");
        String surname = this.getArguments().getString("surname");
        activity = this.getArguments().getInt("activity");

        //Data from DoctorDetailsActivity for actual person
        String previousName = this.getArguments().getString("previousname");
        String previousSurname = this.getArguments().getString("previoussurname");

        String title = "Set doctor " + name + " " + surname + " as " + role + "?";
        String message = "Doctor " + previousName + " " + previousSurname + " is already set as "
                + role + ". Do you want to change him/her to " + name + " " + surname +
                "?\nAll existing permissions for this doctor " + previousName + " " + previousSurname + " will be deleted.";

        if (activity == 2) {
            doctorList = AddressBookMainFragment.listDoctor;
        } else {
            doctorList = DoctorsFromAddressBookMainFragment.listDoctor;
        }
        FragmentActivity fragmentActivity = Objects.requireNonNull(getActivity());
        AlertDialog.Builder builderChange = new AlertDialog.Builder(fragmentActivity);
        builderChange.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.doctor)
                // Yes button
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {

                    builder = new AlertDialog.Builder(fragmentActivity);
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("Permission settings");
                    builder.setMessage(getString(R.string.change_permission_auto));
                    builder.setPositiveButton(getString(R.string.ok),
                            (arg0, arg1) -> {
                                getParticipantData.execute();

                                permission = true;
                                setAccess("editdocument", "true");

                                RegisterPermission registerPermission = new RegisterPermission(doctorId,
                                        SavePreferences.getDefaultsString("dataIdDMS2",
                                                PatientApp.getContext()), getContext());
                                registerPermission.execute();
                            });
                    builder.setNegativeButton(getString(R.string.cancel),
                            (arg0, arg1) -> {
                                getParticipantData.execute();

                                permission = false;
                                setAccess("editdocument", "false");
                            });
                    builder.setCancelable(true);
                    builder.show();
                });
        // No button
        builderChange.setNegativeButton(getString(R.string.no), (dialog, id) -> {
        });
        builderChange.setCancelable(true);
        return builderChange.create();
    }

    /**
     * Update doctors role (diabetologist or family doctor)
     */
    private void changeDoctorRole(String id, String previousId, String role, int i, int position,
                                  JSONArray doctorArray) {
        if ((id != null && id.equals(doctorList.get(i).getId()))) {
            try {
                // check and inform if there is no connection
                if (doctorArray.getJSONObject(0).has("error")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("Warning!");
                    builder.setMessage(getString(R.string.server_not_respond));
                    builder.setPositiveButton(getString(R.string.ok),
                            (arg0, arg1) -> {
                            });
                    builder.show();
                } else {
                    for (int j = 0; j < doctorArray.length(); j++) {
                        if (doctorArray.getJSONObject(j).getString("id").equals(previousId)) {
                            doctorArray.getJSONObject(j).getJSONArray("role").put(position, "null");
                            doctorArray.getJSONObject(j).getJSONObject("access").put("editdocument", false);

                        }
                        if (doctorArray.getJSONObject(j).getString("id").equals(id) && permission) {
                            doctorArray.getJSONObject(j).getJSONArray("role").put(position, role);
                            doctorArray.getJSONObject(j).getJSONObject("access").put("editdocument", true);
                        } else if (doctorArray.getJSONObject(j).getString("id").equals(id) && !permission) {
                            doctorArray.getJSONObject(j).getJSONArray("role").put(position, role);
                            doctorArray.getJSONObject(j).getJSONObject("access").put("editdocument", false);
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                    UpdateParticipantBuilder()
                    .array(doctorArray)
                    .dataId(dataId)
                    .operationId("delete").build();
            updateParticipantData.execute();
            long endTime = System.nanoTime();
            Log.i(TAG, "Change doctor from address book to <role>|Change doctor from " +
                    "address book to <role>|mix|-|" + startTime / 1000000 + "|" +
                    (endTime - startTime) / 1000000 + "|" + "-");
            Toast.makeText(context, "Doctor is changed.", Toast.LENGTH_SHORT).show();
            if (activity != 1 && activity != 2) {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .fragment(new MyDoctorsFragment()).fragmentActivity(context).build();
                transmitData.changeFragment(R.id.container);
            }
        }
    }

    private static void setAccess(String field, String bool) {
        boolean b;
        b = bool.equals("true");
        SavePreferences.setDefaultsBoolean(field, b, PatientApp.getContext());
    }

    @Override
    public void onTaskComplete(JSONArray result) {
        for (int i = 0; i < doctorList.size(); i++) {
            if (role != null && role.equals("diabetologist")) {
                changeDoctorRole(doctorId, previousDoctorId, role, i, 0, result);
            }
            if (role != null && role.equals("family doctor")) {
                changeDoctorRole(doctorId, previousDoctorId, role, i, 1, result);
            }
        }

    }
}