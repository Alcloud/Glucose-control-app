package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToChangeFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteParticipantDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteRoleDialog;

/**
 * Created by Aleksei Piatkin on 26.06.17.
 * <p>
 * This fragment set each line (item) in user address book.
 * The user can set or delete the doctor and his role from address book.
 */

public class AddressBookFragment extends ListFragment {

    ArrayList<Doctor> listDoctorAdapter = new ArrayList<>();

    public static String name;
    public static String surname;
    public static String role;
    public static String city;
    private String accountId = "HansAugust";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));

        MyDoctorsAdapter myListAdapter = new MyDoctorsAdapter(getActivity(),
                R.layout.fragment_address_book_list, AddressBookMainFragment.listDoctor);
        setListAdapter(myListAdapter);
        myListAdapter.notifyDataSetChanged();
    }

    @Override

    // get doctor data by click on item
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent doctorDetailsIntent = new Intent(getActivity(), BookDoctorDetailsActivity.class);
        startActivity(doctorDetailsIntent);
        TextView textViewName = (TextView) v.findViewById(R.id.textViewDoctorNameBook);
        TextView textViewSurname = (TextView) v.findViewById(R.id.textViewDoctorSurnameBook);
        TextView textViewRole = (TextView) v.findViewById(R.id.textViewDoctorRoleBook);
        TextView textViewCity = (TextView) v.findViewById(R.id.textViewDoctorCityBook);
        name = textViewName.getText().toString();
        surname = textViewSurname.getText().toString();
        role = textViewRole.getText().toString();
        city = textViewCity.getText().toString();
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
            View v = inflater.inflate(R.layout.fragment_address_book_list, parent,
                    false);

            TextView doctorName = (TextView) v.findViewById(R.id.textViewDoctorNameBook);
            TextView doctorSurname = (TextView) v.findViewById(R.id.textViewDoctorSurnameBook);
            TextView doctorRole = (TextView) v.findViewById(R.id.textViewDoctorRoleBook);
            TextView doctorCity = (TextView) v.findViewById(R.id.textViewDoctorCityBook);
            ImageButton diabetologistRole = (ImageButton) v.findViewById(R.id.button_diabetologist_role_sign);
            ImageButton familyRole = (ImageButton) v.findViewById(R.id.button_family_role_sign);
            ImageButton doctorDelete = (ImageButton) v.findViewById(R.id.button_delete);
            ImageButton addToRole = (ImageButton) v.findViewById(R.id.button_add_to_role);
            ImageView iconDoctor = (ImageView) v.findViewById(R.id.iconDoctor);

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
            if (listDoctorAdapter.get(position).getRole() != null &&
                    !listDoctorAdapter.get(position).getRole()[0].equals("null")) {
                diabetologistRole.setImageResource(R.drawable.diabetologist_aktiv);
                diabetologistRole.setVisibility(View.VISIBLE);
            }
            if (listDoctorAdapter.get(position).getRole() != null &&
                    !listDoctorAdapter.get(position).getRole()[1].equals("null")) {
                familyRole.setImageResource(R.drawable.family_doctor_aktiv);
                familyRole.setVisibility(View.VISIBLE);
            }

            iconDoctor.setImageResource(R.drawable.doctor);
            doctorDelete.setOnClickListener(v12 -> {

                //Send data to AskToChangeFragment
                sendDataToDialogFragment(new AskToDeleteParticipantDialog(), null,
                        doctorName.getText().toString(), doctorSurname.getText().toString(),
                        doctorRole.getText().toString(), doctorCity.getText().toString());
            });
            addToRole.setOnClickListener(v1 -> {
                if (doctorName != null && doctorSurname != null && doctorRole != null && doctorCity != null) {
                    addRole(getString(R.string.diabetologist), doctorName.getText().toString(), doctorSurname.getText().toString(),
                            doctorRole.getText().toString(), doctorCity.getText().toString());
                    addRole(getString(R.string.family_doctor), doctorName.getText().toString(), doctorSurname.getText().toString(),
                            doctorRole.getText().toString(), doctorCity.getText().toString());
                }
            });
            diabetologistRole.setOnClickListener(v13 -> {
                sendDataToDialogFragment(new AskToDeleteRoleDialog(), "diabetologist", doctorName.getText().toString(),
                        doctorSurname.getText().toString(), doctorRole.getText().toString(), doctorCity.getText().toString());
            });
            familyRole.setOnClickListener(v14 -> {
                sendDataToDialogFragment(new AskToDeleteRoleDialog(), "family doctor", doctorName.getText().toString(),
                        doctorSurname.getText().toString(), doctorRole.getText().toString(), doctorCity.getText().toString());
            });
            return v;
        }
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    // method to add a new role to doctor and check if he has this role already
    private void addRole(String role, String name, String surname, String mainRole, String city) {
        if (AddressBookMainFragment.role.equals(role)) {
            if (AddressBookMainFragment.name.equals(name) && AddressBookMainFragment.surname.equals(surname)) {
                Toast.makeText(getActivity().getApplicationContext(), "This doctor is already set as " +
                        role, Toast.LENGTH_SHORT).show();
            } else {
                if (AddressBookMainFragment.name.equals("") || AddressBookMainFragment.name.equals("not registered")) {
                    for (int i = 0; i < AddressBookMainFragment.listDoctor.size(); i++) {
                        if (role.equals("diabetologist")) {
                            saveDoctorRole(name, surname, i, 0, role);
                        }
                        if (role.equals("family doctor")) {
                            saveDoctorRole(name, surname, i, 1, role);
                        }
                    }
                } else {
                    //Send data to AskToChangeFragment
                    sendDataToDialogFragment(new AskToChangeFragment(), role, name, surname, mainRole, city);
                }
            }
        }
    }

    private void saveDoctorRole(String name, String surname, int i, int position, String roleName) {
        if (name.equals(AddressBookMainFragment.listDoctor.get(i).getName()) &&
                surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname()) &&
                AddressBookMainFragment.listDoctor.get(i).getRole()[position].equals("null")) {
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
            Toast.makeText(getContext(), "Doctor " + name + " " + surname +
                    " was set as " + roleName + ".", Toast.LENGTH_SHORT).show();
        }
        if (name.equals(AddressBookMainFragment.listDoctor.get(i).getName()) &&
                surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname()) &&
                AddressBookMainFragment.listDoctor.get(i).getRole()[position].equals(roleName)) {
            Toast.makeText(getActivity().getApplicationContext(), "This doctor is already set as " +
                    roleName, Toast.LENGTH_SHORT).show();
        }
    }

    // send some data to another fragment or activity
    private void sendDataToDialogFragment(DialogFragment myDialogFragment, String role, String name,
                                          String surname, String mainRole, String city) {

        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();
        // new person
        bundle.putString("mainrole", mainRole);
        bundle.putString("name", name);
        bundle.putString("surname", surname);
        bundle.putString("role", role);
        bundle.putString("city", city);
        // previous person
        bundle.putString("previousname", AddressBookMainFragment.name);
        bundle.putString("previoussurname", AddressBookMainFragment.surname);
        bundle.putString("previouscity", AddressBookMainFragment.city);

        myDialogFragment.setArguments(bundle);
        myDialogFragment.show(transaction, "dialog");
    }
}
