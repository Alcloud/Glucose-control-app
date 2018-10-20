package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.credential_v020.R;

import java.util.ArrayList;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.helper.GetData;
import eu.credential.app.patient.helper.SavePreferences;

/**
 * Created by Aleksei Piatkin on 26.06.17.
 * <p>
 * This fragment shows user address book.
 * The user can set or delete the doctor and his role from address book right from this fragment.
 */
public class AddressBookMainFragment extends Fragment {

    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    public static ArrayList<Doctor> listDoctor = new ArrayList<>();

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
        return inflater.inflate(R.layout.fragment_address_book_main, container, false);
    }

    public void refreshDoctorList() {
        GetData getData = new GetData(dataId, getContext());
        getData.refreshList();
        listDoctor = getData.getListDoctor();
    }
}

