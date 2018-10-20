package eu.credential.app.patient.ui.searchParticipant;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;

/**
 * Created by Aleksei Piatkin on 13.07.17.
 * <p>
 * A doctor details screen that shows doctors main data.
 */
public class DoctorDetailsActivity extends AppCompatActivity {

    TextView doctorName;
    TextView doctorSurname;
    TextView doctorRole;
    TextView doctorCity;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_details);
        Toolbar toolbar = findViewById(R.id.toolbar_doctor_details);

        toolbar.setTitle(ParticipantListFragment.name + " " + ParticipantListFragment.surname);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        doctorName = findViewById(R.id.textView_doctor_name);
        doctorSurname = findViewById(R.id.textView_doctor_surname);
        doctorRole = findViewById(R.id.textView_doctor_role);
        doctorCity = findViewById(R.id.textView_doctor_city);
        TextView doctorDescription = findViewById(R.id.text_doctor_description);
        doctorName.setText(ParticipantListFragment.name);
        doctorSurname.setText(ParticipantListFragment.surname);
        doctorRole.setText(ParticipantListFragment.role);
        doctorCity.setText(ParticipantListFragment.city);
        doctorDescription.setText("Doctor " + ParticipantListFragment.surname + " is a " +
                ParticipantListFragment.role + " from " + ParticipantListFragment.city +
                ". If you want to add Dr. " + ParticipantListFragment.name + " " +
                ParticipantListFragment.surname + " to your address " + "book, just push an Add button below.");
    }

    public void onClickAdd(View view) {
        try {
            GetParticipantData getParticipantData = new GetParticipantData(SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext()));
            JSONArray doctorArray = getParticipantData.execute().get();

            if (doctorInAddressBookExist(doctorArray, ParticipantListFragment.doctorId) == 0) {
                // save data to address book
                UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                        UpdateParticipantBuilder().array(doctorArray)
                        .doctorName(ParticipantListFragment.name)
                        .doctorSurname(ParticipantListFragment.surname)
                        .doctorCity(ParticipantListFragment.city)
                        .doctorMainRole(ParticipantListFragment.role)
                        .doctorRole("null")
                        .dataId(SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext()))
                        .doctorId(ParticipantListFragment.doctorId)
                        .operationId("addressBook").build();
                updateParticipantData.execute();
                Toast.makeText(getApplicationContext(), "Doctor " + ParticipantListFragment.name +
                        " " + ParticipantListFragment.surname +
                        " was added to address book", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Doctor " + ParticipantListFragment.name +
                        " " + ParticipantListFragment.surname +
                        " is already in the address book", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        finish();
    }

    private int doctorInAddressBookExist(JSONArray doctorArray, String id) throws JSONException {
        int j = 0;
        // check and inform if there is no connection
        if (doctorArray.getJSONObject(0).has("error")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setTitle("Warning!");
            builder.setMessage(getString(R.string.server_not_respond));
            builder.setPositiveButton(getString(R.string.ok),
                    (arg0, arg1) -> {
                    });
            builder.show();
        } else {
            for (int i = 0; i < doctorArray.length(); i++) {
                if (doctorArray.getJSONObject(i).getString("id").equals(id)) {
                    j++;
                }
            }
        }
        return j;
    }
}
