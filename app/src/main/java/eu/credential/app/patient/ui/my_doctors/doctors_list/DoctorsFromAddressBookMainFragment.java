package eu.credential.app.patient.ui.my_doctors.doctors_list;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.GetData;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.Doctor;

/**
 * Created by Aleksei Piatkin on 18.10.17.
 * <p>
 * This fragment shows user list of doctors. User can set or delete the doctors role.
 */
public class DoctorsFromAddressBookMainFragment extends Fragment {
    public static String doctorId;
    public static String role;
    public static String name;
    public static String surname;
    public static ArrayList<Doctor> listDoctor = new ArrayList<>();

    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshDoctorList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDoctorList();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_doctors_from_address_book_main, container, false);

        //Data from MyDoctorsFragment for actual person
        doctorId = this.getArguments().getString("id");
        role = this.getArguments().getString("role");
        name = this.getArguments().getString("name");
        surname = this.getArguments().getString("surname");

        getActivity().setTitle("Set " + role);
        return v;
    }

    /**
     * Update a list of participants (doctors).
     */
    public void refreshDoctorList() {
        GetData getData = new GetData(dataId, getContext());
        getData.refreshList();
        listDoctor = getData.getListDoctor();
    }
}
