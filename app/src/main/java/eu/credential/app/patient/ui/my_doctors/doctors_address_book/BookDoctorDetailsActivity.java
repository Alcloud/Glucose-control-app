package eu.credential.app.patient.ui.my_doctors.doctors_address_book;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.GetData;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToAccessTypeDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteParticipantDialog;

/**
 * Created by Aleksei Piatkin on 26.06.17.
 * <p>
 * This activity shows the doctor's data in details.
 * The user can set or delete the doctor and his role from address book right from this activity.
 */
public class BookDoctorDetailsActivity extends AppCompatActivity {
    // Text
    private TextView doctorName;
    private TextView doctorSurname;
    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    private GetData getData = new GetData(dataId, this);

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_doctor_details);
        Toolbar toolbar = findViewById(R.id.toolbar_doctor_book_details);

        toolbar.setTitle(AddressBookFragment.name + " " + AddressBookFragment.surname);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // get doctors data from DMS
        getData.refreshList();

        ImageView firstRoleIcon = findViewById(R.id.role_icon);
        ImageView secondRoleIcon = findViewById(R.id.role_second_icon);
        doctorName = findViewById(R.id.textView_doctor_name_book);
        doctorSurname = findViewById(R.id.textView_doctor_surname_book);
        TextView doctorRole = findViewById(R.id.textView_doctor_role_book);
        TextView doctorCity = findViewById(R.id.textView_doctor_city_book);
        TextView accessData = findViewById(R.id.text_doctor_access_data);
        TextView doctorDescription = findViewById(R.id.text_doctor_description_book);

        doctorName.setText(AddressBookFragment.name);
        doctorSurname.setText(AddressBookFragment.surname);
        doctorRole.setText(AddressBookFragment.role);
        doctorCity.setText(AddressBookFragment.city);

        if (AddressBookFragment.diabetologist) {
            firstRoleIcon.setImageResource(R.mipmap.diabetologist_activ_icon);
        } else if (AddressBookFragment.familyDoctor) {
            firstRoleIcon.setImageResource(R.mipmap.family_doctor_activ);
        } else if (AddressBookFragment.diabetologist && AddressBookFragment.familyDoctor) {
            firstRoleIcon.setImageResource(R.mipmap.diabetologist_activ_icon);
            secondRoleIcon.setImageResource(R.mipmap.family_doctor_activ);
        }
        refresh();

        doctorDescription.setText("Doctor " + AddressBookFragment.name + " " +
                AddressBookFragment.surname + " is a " + AddressBookFragment.role + " from " +
                AddressBookFragment.city + ". If you want to set Dr. " + AddressBookFragment.name +
                " " + AddressBookFragment.surname + " as diabetologist or family doctor, just " +
                "press a corresponding button below.");
    }

    @Override
    public void onResume() {
        super.onResume();
        getData.refreshList();
    }

    // delete a doctor or his role from address book
    public void onClickDelete(View view) {
        DialogFragment askToDeleteParticipantDialog = new AskToDeleteParticipantDialog();
        final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                .id(AddressBookFragment.ids)
                .name(doctorName.getText().toString())
                .surname(doctorSurname.getText().toString())
                .dialogFragment(askToDeleteParticipantDialog)
                .activity(2).fragmentActivity(this).build();
        transmitData.changeFragmentToDialog();
    }

    // set a special access for doctor
    public void onClickAccess(View view) {
        DialogFragment askToAccessTypeDialog = new AskToAccessTypeDialog();
        final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                .id(AddressBookFragment.ids)
                .name(doctorName.getText().toString())
                .surname(doctorSurname.getText().toString())
                .dialogFragment(askToAccessTypeDialog)
                .fragmentActivity(this).build();
        transmitData.changeFragmentToDialog();
    }

    public void refresh() {
        //TODO: show access list
    }
}
