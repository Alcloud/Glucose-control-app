package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;

import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToAccessTypeDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteParticipantDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteRoleDialog;

/**
 * Created by Aleksei Piatkin on 26.06.17.
 * <p>
 * This fragment set each line (item) in user address book.
 * The user can delete the doctor and his role from address book and find some info about doctor.
 */

public class AddressBookFragment extends ListFragment {

    private ArrayList<Doctor> listDoctorAdapter = new ArrayList<>();

    public static String ids;
    public static String name;
    public static String surname;
    public static String role;
    public static String city;
    public static boolean diabetologist;
    public static boolean familyDoctor;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setEmptyText(getResources().getString(R.string.error_list));

        MyDoctorsAdapter myListAdapter = new MyDoctorsAdapter(getActivity(),
                R.layout.fragment_address_book_list, AddressBookMainFragment.listDoctor);
        setListAdapter(myListAdapter);
        myListAdapter.notifyDataSetChanged();
    }

    // get doctor data by click on item
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent doctorDetailsIntent = new Intent(getActivity(), BookDoctorDetailsActivity.class);
        startActivity(doctorDetailsIntent);
        TextView textViewId = v.findViewById(R.id.textViewDoctorIdBook);
        TextView textViewName = v.findViewById(R.id.textViewDoctorNameBook);
        TextView textViewSurname = v.findViewById(R.id.textViewDoctorSurnameBook);
        TextView textViewRole = v.findViewById(R.id.textViewDoctorRoleBook);
        TextView textViewCity = v.findViewById(R.id.textViewDoctorCityBook);
        ids = textViewId.getText().toString();
        name = textViewName.getText().toString();
        surname = textViewSurname.getText().toString();
        role = textViewRole.getText().toString();
        city = textViewCity.getText().toString();
        diabetologist = listDoctorAdapter.get(position).getRole() != null &&
                !listDoctorAdapter.get(position).getRole()[0].equals("null");
        familyDoctor = listDoctorAdapter.get(position).getRole() != null &&
                !listDoctorAdapter.get(position).getRole()[1].equals("null");
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
                v = inflater.inflate(R.layout.fragment_address_book_list, parent, false);
            }
            assert v != null;
            TextView doctorId = v.findViewById(R.id.textViewDoctorIdBook);
            TextView doctorName = v.findViewById(R.id.textViewDoctorNameBook);
            TextView doctorSurname = v.findViewById(R.id.textViewDoctorSurnameBook);
            TextView doctorRole = v.findViewById(R.id.textViewDoctorRoleBook);
            TextView doctorCity = v.findViewById(R.id.textViewDoctorCityBook);
            ImageButton diabetologistRole = v.findViewById(R.id.button_diabetologist_role_sign);
            ImageButton familyRole = v.findViewById(R.id.button_family_role_sign);
            ImageButton doctorDelete = v.findViewById(R.id.button_delete);
            ImageButton doctorAccess = v.findViewById(R.id.button_access_list);
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
            if (listDoctorAdapter.get(position).getRole() != null &&
                    !listDoctorAdapter.get(position).getRole()[0].equals("null")) {
                diabetologistRole.setImageResource(R.mipmap.diabetologist_activ_icon);
                diabetologistRole.setVisibility(View.VISIBLE);
            }
            if (listDoctorAdapter.get(position).getRole() != null &&
                    !listDoctorAdapter.get(position).getRole()[1].equals("null")) {
                familyRole.setImageResource(R.mipmap.family_doctor_activ_icon);
                familyRole.setVisibility(View.VISIBLE);
            }
            DialogFragment askToDeleteParticipantDialog = new AskToDeleteParticipantDialog();
            DialogFragment askToDeleteRoleDialog = new AskToDeleteRoleDialog();
            DialogFragment askToAccessTypeDialog = new AskToAccessTypeDialog();

            doctorDelete.setOnClickListener(v12 -> {
                //Send data to AskToDeleteParticipantDialog
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(doctorId.getText().toString())
                        .name(doctorName.getText().toString())
                        .surname(doctorSurname.getText().toString())
                        .dialogFragment(askToDeleteParticipantDialog)
                        .fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            });
            diabetologistRole.setOnClickListener(v13 -> {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(doctorId.getText().toString())
                        .name(doctorName.getText().toString())
                        .surname(doctorSurname.getText().toString())
                        .role(getString(R.string.diabetologist))
                        .dialogFragment(askToDeleteRoleDialog)
                        .fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            });
            familyRole.setOnClickListener(v14 -> {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(doctorId.getText().toString())
                        .name(doctorName.getText().toString())
                        .surname(doctorSurname.getText().toString())
                        .role(getString(R.string.family_doctor))
                        .dialogFragment(askToDeleteRoleDialog)
                        .fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            });
            // set a special access for doctor
            doctorAccess.setOnClickListener(v15 -> {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(doctorId.getText().toString())
                        .name(doctorName.getText().toString())
                        .surname(doctorSurname.getText().toString())
                        .dialogFragment(askToAccessTypeDialog)
                        .fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            });
            return v;
        }
    }
}
