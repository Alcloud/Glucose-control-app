package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToAccessTypeDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteParticipantDialog;
import eu.credential.app.patient.ui.searchParticipant.ParticipantListFragment;

public class BookDoctorDetailsActivity extends AppCompatActivity {

    TextView doctorName;
    TextView doctorSurname;
    TextView doctorRole;
    TextView doctorCity;
    TextView accessData;
    TextView doctorDescription;
    private String accountId = "HansAugust";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_doctor_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_doctor_details);

        toolbar.setTitle(ParticipantListFragment.name + " " + ParticipantListFragment.surname);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        doctorName = (TextView) findViewById(R.id.textView_doctor_name_book);
        doctorSurname = (TextView) findViewById(R.id.textView_doctor_surname_book);
        doctorRole = (TextView) findViewById(R.id.textView_doctor_role_book);
        doctorCity = (TextView) findViewById(R.id.textView_doctor_city_book);
        accessData = (TextView) findViewById(R.id.text_doctor_access_data);
        doctorDescription = (TextView) findViewById(R.id.text_doctor_description_book);

        doctorName.setText(AddressBookFragment.name);
        doctorSurname.setText(AddressBookFragment.surname);
        doctorRole.setText(AddressBookFragment.role);
        doctorCity.setText(AddressBookFragment.city);

        refresh();
        doctorDescription.setText("Doctor " + AddressBookFragment.name + " " +
                AddressBookFragment.surname + " is a " + AddressBookFragment.role + " from " +
                AddressBookFragment.city + ". If you want to set Dr. " + AddressBookFragment.name +
                " as " + AddressBookFragment.role + ", just push an Add button below.");
    }

    public void onClickAdd(View view) throws IOException {

        if (AddressBookMainFragment.role.equals(getString(R.string.diabetologist))) {
            addRole(getString(R.string.diabetologist), doctorName.getText().toString(),
                    doctorSurname.getText().toString(), doctorRole.getText().toString(), doctorCity.getText().toString());
        } else if (AddressBookMainFragment.role.equals(getString(R.string.family_doctor))) {
            addRole(getString(R.string.family_doctor), doctorName.getText().toString(),
                    doctorSurname.getText().toString(), doctorRole.getText().toString(), doctorCity.getText().toString());
        }
    }

    public void onClickDelete(View view) {
        AskToDeleteParticipantDialog myDialogFragment = new AskToDeleteParticipantDialog();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        Bundle bundle = new Bundle();

        bundle.putString("name", doctorName.getText().toString());
        bundle.putString("surname", doctorSurname.getText().toString());
        bundle.putString("role", AddressBookFragment.role);
        bundle.putInt("activity", 1);

        myDialogFragment.setArguments(bundle);
        myDialogFragment.show(transaction, "dialog");
    }

    public void onClickAccess(View view) {

        AskToAccessTypeDialog myDialogFragment = new AskToAccessTypeDialog();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Bundle bundle = new Bundle();
        bundle.putString("name", doctorName.getText().toString());

        myDialogFragment.setArguments(bundle);
        myDialogFragment.show(transaction, "dialog");
    }

    private void setDoctor(String name, String surname, String role, String city, String mainRole) {

        if (AddressBookMainFragment.name.equals("") || AddressBookMainFragment.name.equals("not registered")) {

            for (int i = 0; i < AddressBookMainFragment.listDoctor.size(); i++) {
                if (name.equals(AddressBookMainFragment.listDoctor.get(i).getName()) &&
                        surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname()) &&
                        AddressBookMainFragment.listDoctor.get(i).getRole().equals("null")) {
                    GetParticipantData getParticipantData = new GetParticipantData(accountId);
                    try {
                        JSONArray doctorArray = getParticipantData.execute().get();
                        for (int j = 0; j < doctorArray.length(); j++) {
                            // TODO: Change "surname" to "id", when doctors will be get from LDAP
                            if (doctorArray.getJSONObject(j).getString("surname").equals(surname) &&
                                    doctorArray.getJSONObject(j).getString("name").equals(name)) {
                                doctorArray.getJSONObject(j).put("role", role);
                            }
                        }
                        UpdateParticipantData updateParticipantData = new UpdateParticipantData
                                (doctorArray, accountId, "delete");
                        updateParticipantData.execute();
                        Toast.makeText(getApplicationContext(), "Doctor " + name + " " + surname +
                                " was set as " + role + ".", Toast.LENGTH_SHORT).show();
                        finish();
                    } catch (InterruptedException | ExecutionException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (name.equals(AddressBookMainFragment.listDoctor.get(i).getName()) &&
                        surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname()) &&
                        AddressBookMainFragment.listDoctor.get(i).getRole().equals(getString(R.string.diabetologist))) {
                    Toast.makeText(getApplicationContext(), "This doctor is already set as " +
                            getString(R.string.diabetologist), Toast.LENGTH_SHORT).show();
                }
                if (name.equals(AddressBookMainFragment.listDoctor.get(i).getName()) &&
                        surname.equals(AddressBookMainFragment.listDoctor.get(i).getSurname()) &&
                        AddressBookMainFragment.listDoctor.get(i).getRole().equals(getString(R.string.family_doctor))) {
                    Toast.makeText(getApplicationContext(), "This doctor is already set as " +
                            getString(R.string.family_doctor), Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            //Send data to AskToChangeFragment
            AskToChangeFragment myDialogFragment = new AskToChangeFragment();
            FragmentManager manager = getSupportFragmentManager();
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
            bundle.putString("previousrole", AddressBookMainFragment.role);
            bundle.putString("previouscity", AddressBookMainFragment.city);

            myDialogFragment.setArguments(bundle);
            myDialogFragment.show(transaction, "dialog");
        }
    }

    private void addRole(String role, String name, String surname, String mainRole, String city) {
        if (AddressBookMainFragment.role.equals(role)) {
            if (AddressBookMainFragment.name.equals(name) && AddressBookMainFragment.surname.equals(surname)) {
                Toast.makeText(getApplicationContext(), "This doctor is already set as " +
                        role, Toast.LENGTH_SHORT).show();
            } else {
                setDoctor(name, surname, role, city, mainRole);
            }
        }
    }

    public void refresh() {
        try {
            String access;
            if (!JSONFile.readJSONAccess(getApplicationContext()).isEmpty()) {
                access = JSONFile.readJSONAccess(getApplicationContext()).get(0);
                accessData.setText(access);
            } else {
                accessData.setText("no access");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
