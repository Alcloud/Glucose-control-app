package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;

/**
 * Created by Aleksei Piatkin on 26.06.17.
 * <p>
 * This fragment shows user address book.
 * The user can set or delete the doctor and his role from address book right from this fragment.
 */

public class AddressBookMainFragment extends Fragment {

    public static String role;
    public static String name;
    public static String surname;
    public static String mainRole;
    public static String city;
    private String accountId = "HansAugust";

    String doctorName;
    String doctorSurname;
    String doctorMainRole;
    String doctorCity;

    public static ArrayList<Doctor> listDoctor = new ArrayList<>();

    // get actual list of doctors in address book
    public void refreshList() {
        listDoctor.clear();
        GetParticipantData getParticipantData = new GetParticipantData(accountId);
        JSONArray doctorArray;
        try {
            doctorArray = getParticipantData.execute().get();

            for (int i = 0; i < doctorArray.length(); i++) {
                String [] doctorRole = new String[2];
                Doctor doctor = new Doctor();
                doctorName = doctorArray.getJSONObject(i).getString("name");
                doctorSurname = doctorArray.getJSONObject(i).getString("surname");
                doctorCity = doctorArray.getJSONObject(i).getString("city");
                doctorMainRole = doctorArray.getJSONObject(i).getString("mainrole");
                doctorRole [0] = (String) doctorArray.getJSONObject(i).getJSONArray("role").get(0);
                doctorRole [1] = (String) doctorArray.getJSONObject(i).getJSONArray("role").get(1);

                doctor.setName(doctorName);
                doctor.setSurname(doctorSurname);
                doctor.setCity(doctorCity);
                doctor.setMainRole(doctorMainRole);
                doctor.setRole(doctorRole);
                listDoctor.add(doctor);
            }

        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshList();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_address_book_main, container, false);

        //Data from MyDoctorsFragment for actual person
        role = this.getArguments().getString("role");
        name = this.getArguments().getString("name");
        surname = this.getArguments().getString("surname");
        mainRole = this.getArguments().getString("mainrole");
        city = this.getArguments().getString("city");

        getActivity().setTitle("Set " + role);
        return v;
    }
}

