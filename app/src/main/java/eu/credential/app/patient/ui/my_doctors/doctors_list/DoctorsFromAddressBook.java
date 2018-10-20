package eu.credential.app.patient.ui.my_doctors.doctors_list;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToChangeFragment;

/**
 * Created by Aleksei Piatkin on 18.10.17.
 * <p>
 * This fragment set each line (item) in user address book.
 * The user can set the doctor's role. Available are only doctors from address book.
 */
public class DoctorsFromAddressBook extends ListFragment implements AsyncTaskCompleteListener<JSONArray> {

    private static final String TAG = "Performance";

    public static String name;
    public static String surname;
    public static String role;

    private static boolean permission = false;
    private ArrayList<Doctor> listDoctorAdapter = new ArrayList<>();
    private String previousIdDiabetologist;
    private String previousIdFamily;
    private String nameDiabetologist;
    private String nameFamily;
    private String surnameDiabetologist;
    private String surnameFamily;
    private String newId;
    private int newPosition;
    private String newRoleName;
    private static String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());
    AlertDialog.Builder builder;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        DoctorsFromAddressBook.MyDoctorsAdapter myListAdapter =
                new DoctorsFromAddressBook.MyDoctorsAdapter(getActivity(),
                        R.layout.fragment_doctors_from_address_book, DoctorsFromAddressBookMainFragment.listDoctor);
        setListAdapter(myListAdapter);
        myListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTaskComplete(JSONArray result) {
        long startTime = System.nanoTime();
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
                for (int i = 0; i < result.length(); i++) {
                    if (result.getJSONObject(i).getString("id").equals(newId)) {
                        result.getJSONObject(i).getJSONArray("role").put(newPosition, newRoleName);
                        if (permission) {
                            result.getJSONObject(i).getJSONObject("access").put("editdocument", true);
                        } else {
                            result.getJSONObject(i).getJSONObject("access").put("editdocument", false);
                        }
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
                .context(getContext())
                .operationId("delete").build();
        updateParticipantData.execute();

        final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                .fragment(new MyDoctorsFragment()).fragmentActivity(getActivity()).build();
        transmitData.changeFragment(R.id.container);

        long endTime = System.nanoTime();
        Log.i(TAG, "Set doctor from address book as <role>|Set doctor from address " +
                "book as <role>|mix|-|" + startTime / 1000000 + "|" +
                (endTime - startTime) / 1000000 + "|" + "-");

        Toast.makeText(getContext(), "Doctor " + name + " " + surname +
                " was set as " + newRoleName + ".", Toast.LENGTH_SHORT).show();
    }

    private class MyDoctorsAdapter extends ArrayAdapter<Doctor> {
        private Context myContext;

        MyDoctorsAdapter(Context context, int textViewResourceId, ArrayList<Doctor> listDoctor) {
            super(context, textViewResourceId, listDoctor);
            myContext = context;
            listDoctorAdapter = listDoctor;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = null;
            if (inflater != null) {
                v = inflater.inflate(R.layout.fragment_doctors_from_address_book, parent, false);
            }
            // Get text
            TextView doctorId = v.findViewById(R.id.textViewDoctorIdBook);
            TextView doctorName = v.findViewById(R.id.textViewDoctorNameBook);
            TextView doctorSurname = v.findViewById(R.id.textViewDoctorSurnameBook);
            TextView doctorRole = v.findViewById(R.id.textViewDoctorRoleBook);
            TextView doctorCity = v.findViewById(R.id.textViewDoctorCityBook);
            // Get button and image
            ImageButton addToRole = v.findViewById(R.id.button_add_to_doctors_role);
            ImageView iconDoctor = v.findViewById(R.id.iconDoctor);
            iconDoctor.setImageResource(R.mipmap.doctor);

            if (listDoctorAdapter.get(position).getId() != null) {
                doctorId.setText(listDoctorAdapter.get(position).getId());
            }

            if (listDoctorAdapter.get(position).getName() != null) {
                doctorName.setText(listDoctorAdapter.get(position).getName());
            }

            if (listDoctorAdapter.get(position).getSurname() != null) {
                doctorSurname.setText(listDoctorAdapter.get(position).getSurname());
            }

            if (listDoctorAdapter.get(position).getMainRole() != null) {
                doctorRole.setText(listDoctorAdapter.get(position).getMainRole());
            }

            if (listDoctorAdapter.get(position).getCity() != null) {
                doctorCity.setText(listDoctorAdapter.get(position).getCity());
            }
            for (int i = 0; i < listDoctorAdapter.size(); i++) {
                String[] s = listDoctorAdapter.get(i).getRole();
                if (!s[0].equals("null")) {
                    surnameDiabetologist = listDoctorAdapter.get(i).getSurname();
                    nameDiabetologist = listDoctorAdapter.get(i).getName();
                    previousIdDiabetologist = listDoctorAdapter.get(i).getId();
                }
                if (!s[1].equals("null")) {
                    surnameFamily = listDoctorAdapter.get(i).getSurname();
                    nameFamily = listDoctorAdapter.get(i).getName();
                    previousIdFamily = listDoctorAdapter.get(i).getId();
                }
            }

            addToRole.setOnClickListener(v1 -> {
                if (doctorName != null && doctorSurname != null && doctorRole != null && doctorCity != null) {
                    addRole(doctorId.getText().toString(), DoctorsFromAddressBookMainFragment.role,
                            doctorName.getText().toString(), doctorSurname.getText().toString());
                }
                getActivity().getSupportFragmentManager().beginTransaction().remove(getParentFragment()).commit();
            });
            return v;
        }
    }

    // method to add a new role to doctor and check if he has this role already
    private void addRole(String id, String role, String name, String surname) {

        newId = id;
        newRoleName = role;
        if (DoctorsFromAddressBookMainFragment.doctorId != null &&
                DoctorsFromAddressBookMainFragment.doctorId.equals(id)) {
            Toast.makeText(getActivity().getApplicationContext(), "This doctor is already set as " +
                    role, Toast.LENGTH_SHORT).show();
        } else {
            if (DoctorsFromAddressBookMainFragment.name.equals("") ||
                    DoctorsFromAddressBookMainFragment.name.equals("not registered")) {

                builder = new AlertDialog.Builder(getContext());
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Permission settings");
                builder.setMessage(getString(R.string.change_permission_auto));
                builder.setPositiveButton(getString(R.string.ok),
                        (arg0, arg1) -> {
                            permission = true;
                            if (role.equals("diabetologist")) {
                                newPosition = 0;
                                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                                getParticipantData.execute();
                            }
                            if (role.equals("family doctor")) {
                                newPosition = 1;
                                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                                getParticipantData.execute();
                            }
                            setAccess("editdocument", "true");
                            // Create Policy
                            RegisterPermission registerPermission = new RegisterPermission(id,
                                    SavePreferences.getDefaultsString("dataIdDMS2", PatientApp.getContext()), getContext());
                            registerPermission.execute();
                        });
                builder.setNegativeButton(getString(R.string.cancel),
                        (arg0, arg1) -> {
                            permission = false;
                            if (role.equals("diabetologist")) {
                                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                                getParticipantData.execute();
                            }
                            if (role.equals("family doctor")) {
                                GetParticipantData getParticipantData = new GetParticipantData(dataId, getContext(), this);
                                getParticipantData.execute();
                            }
                            setAccess("editdocument", "false");
                            getActivity().getSupportFragmentManager().beginTransaction().remove(getParentFragment()).commit();
                        });
                builder.setCancelable(true);
                builder.show();
            } else {
                AskToChangeFragment askToChangeFragment = new AskToChangeFragment();
                //Send data to AskToChangeFragment
                if ((surnameDiabetologist != null && !surnameDiabetologist.equals("") && role.equals("diabetologist"))) {
                    final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                            .id(id)
                            .previousId(previousIdDiabetologist)
                            .name(name).surname(surname).role(role)
                            .previousname(nameDiabetologist)
                            .previoussurname(surnameDiabetologist)
                            .dialogFragment(askToChangeFragment)
                            .fragmentActivity(getActivity()).build();
                    transmitData.changeFragmentToDialog();
                }
                if ((surnameFamily != null && !surnameFamily.equals("") && role.equals("family doctor"))) {
                    final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                            .id(id)
                            .previousId(previousIdFamily)
                            .name(name).surname(surname).role(role)
                            .previousname(nameFamily)
                            .previoussurname(surnameFamily)
                            .dialogFragment(askToChangeFragment)
                            .fragmentActivity(getActivity()).build();
                    transmitData.changeFragmentToDialog();
                }
            }
        }
    }

    private static void setAccess(String field, String bool) {
        boolean b;
        b = bool.equals("true");
        SavePreferences.setDefaultsBoolean(field, b, PatientApp.getContext());
    }
}
