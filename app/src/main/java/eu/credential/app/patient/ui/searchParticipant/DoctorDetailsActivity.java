package eu.credential.app.patient.ui.searchParticipant;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class DoctorDetailsActivity extends AppCompatActivity {

    TextView doctorName;
    TextView doctorSurname;
    TextView doctorRole;
    TextView doctorCity;
    private String accountId = "HansAugust";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_doctor_details);

        toolbar.setTitle(ParticipantListFragment.name + " " + ParticipantListFragment.surname);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        doctorName = (TextView) findViewById(R.id.textView_doctor_name);
        doctorSurname = (TextView) findViewById(R.id.textView_doctor_surname);
        doctorRole = (TextView) findViewById(R.id.textView_doctor_role);
        doctorCity = (TextView) findViewById(R.id.textView_doctor_city);
        TextView doctorDescription = (TextView) findViewById(R.id.text_doctor_description);
        doctorName.setText(ParticipantListFragment.name);
        doctorSurname.setText(ParticipantListFragment.surname);
        doctorRole.setText(ParticipantListFragment.role);
        doctorCity.setText(ParticipantListFragment.city);
        doctorDescription.setText("Doctor " + ParticipantListFragment.surname + " is a " +
                ParticipantListFragment.role + " from " + ParticipantListFragment.city +
                ". If you want to add Dr. " + ParticipantListFragment.name + " to your address " +
                "book, just push an Add button below.");
    }

    public void onClickAdd(View view) throws IOException, ExecutionException, InterruptedException {
        try {
            GetParticipantData getParticipantData = new GetParticipantData(accountId);
            JSONArray doctorArray = getParticipantData.execute().get();

            // TODO: Change "doctorSurname[position]", when doctors will be get from LDAP
            if (doctorInAddressBookExist(doctorArray, ParticipantListFragment.surname) == 0) {
                // save data to address book
                UpdateParticipantData updateParticipantData = new UpdateParticipantData
                        (doctorArray, null, accountId,
                                ParticipantListFragment.name, ParticipantListFragment.surname,
                                ParticipantListFragment.city, ParticipantListFragment.role, null);
                updateParticipantData.execute();
                Toast.makeText(getApplicationContext(), "Doctor " + ParticipantListFragment.name +
                        " was added to address book", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Doctor " + ParticipantListFragment.name +
                        " is already in the address book", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        finish();
    }

    // TODO: Change "surname" to "id", when doctors will be get from LDAP
    private int doctorInAddressBookExist(JSONArray doctorArray, String id) throws JSONException {
        int j = 0;
        for (int i = 0; i < doctorArray.length(); i++) {
            if (doctorArray.getJSONObject(i).getString("surname").equals(id)) {
                j++;
            }
        }
        return j;
    }
}
