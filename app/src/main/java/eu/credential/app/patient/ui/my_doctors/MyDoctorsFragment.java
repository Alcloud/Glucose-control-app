package eu.credential.app.patient.ui.my_doctors;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.AddressBookMainFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.dialog_fragments.AskToDeleteRoleDialog;
import eu.credential.app.patient.ui.my_doctors.doctors_list.DoctorsFromAddressBookMainFragment;

/**
 * Created by Aleksei Piatkin on 15.06.17.
 * <p>
 * This fragment show two main doctors with defined role.
 * The user can open address book from this fragment.
 */

public class MyDoctorsFragment extends Fragment {
    // Buttons
    ImageButton editDiabetologist;
    ImageButton deleteDiabetologist;
    ImageButton editFamilyDoctor;
    ImageButton deleteFamilyDoctor;
    ImageButton addressBook;
    // Text
    TextView textViewNameDiabetologist;
    TextView textViewSurnameDiabetologist;
    TextView textViewRoleDiabetologist;
    TextView textViewCityDiabetologist;
    TextView textViewNameFamily;
    TextView textViewSurnameFamily;
    TextView textViewRoleFamily;
    TextView textViewCityFamily;
    // doctor ldap ids
    private String diabetologistId;
    private String familyId;

    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Objects.requireNonNull(getActivity()).setTitle(R.string.my_doctors);
    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        Objects.requireNonNull(getActivity()).setTitle(R.string.my_doctors);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_doctors, container, false);
        // Get buttons
        editDiabetologist = v.findViewById(R.id.address_book_diabetologist_edit);
        deleteDiabetologist = v.findViewById(R.id.address_book_diabetologist_delete);
        addressBook = v.findViewById(R.id.imageView_address_book);
        editFamilyDoctor = v.findViewById(R.id.address_book_family_edit);
        deleteFamilyDoctor = v.findViewById(R.id.address_book_family_delete);
        // Get text
        textViewNameDiabetologist = v.findViewById(R.id.textView_name_diabetologist);
        textViewSurnameDiabetologist = v.findViewById(R.id.textView_surname_diabetologist);
        textViewRoleDiabetologist = v.findViewById(R.id.textView_role_diabetologist);
        textViewCityDiabetologist = v.findViewById(R.id.textView_city_diabetologist);
        textViewNameFamily = v.findViewById(R.id.textView_name_family);
        textViewSurnameFamily = v.findViewById(R.id.textView_surname_family);
        textViewRoleFamily = v.findViewById(R.id.textView_role_family);
        textViewCityFamily = v.findViewById(R.id.textView_city_family);

        GetParticipantData getParticipantData = new GetParticipantData(dataId);
        try {
            // get data from dms service
            JSONArray doctorArray = getParticipantData.execute().get();
            // check and inform if there is no connection
            if (doctorArray.getJSONObject(0).has("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(getString(R.string.server_not_respond));
                builder.setPositiveButton(getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                for (int i = 0; i < doctorArray.length(); i++) {
                    String doctorName = doctorArray.getJSONObject(i).getString("name");
                    String doctorSurname = doctorArray.getJSONObject(i).getString("surname");
                    String doctorCity = doctorArray.getJSONObject(i).getString("city");
                    String doctorMainRole = doctorArray.getJSONObject(i).getString("mainrole");

                    if (doctorArray.getJSONObject(i).getJSONArray("role").get(1).equals(getString(R.string.family_doctor))) {
                        familyId = doctorArray.getJSONObject(i).getString("id");
                        setText(textViewNameFamily, doctorName);
                        setText(textViewSurnameFamily, doctorSurname);
                        setText(textViewRoleFamily, doctorMainRole);
                        setText(textViewCityFamily, doctorCity);
                    }

                    if (doctorArray.getJSONObject(i).getJSONArray("role").get(0).equals(getString(R.string.diabetologist))) {
                        diabetologistId = doctorArray.getJSONObject(i).getString("id");
                        setText(textViewNameDiabetologist, doctorName);
                        setText(textViewSurnameDiabetologist, doctorSurname);
                        setText(textViewRoleDiabetologist, doctorMainRole);
                        setText(textViewCityDiabetologist, doctorCity);
                    }
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
        // Check if doctor is define and show/hide some buttons
        if (!textViewNameFamily.getText().toString().equals("not registered")) {
            editFamilyDoctor.setBackgroundResource(R.drawable.edit_button);
            deleteFamilyDoctor.setBackgroundResource(R.drawable.delete_button);
        } else {
            editFamilyDoctor.setBackgroundResource(R.drawable.add_button);
            deleteFamilyDoctor.setBackgroundResource(R.color.colorBackground);
        }
        if (!textViewNameDiabetologist.getText().toString().equals("not registered")) {
            editDiabetologist.setBackgroundResource(R.drawable.edit_button);
            deleteDiabetologist.setBackgroundResource(R.drawable.delete_button);
        } else {
            editDiabetologist.setBackgroundResource(R.drawable.add_button);
            deleteDiabetologist.setBackgroundResource(R.color.colorBackground);
        }
        final TransmitData toAddressBook = new TransmitData.TransmitDataBuilder()
                .fragment(new AddressBookMainFragment()).fragmentActivity(getActivity()).build();
        addressBook.setOnClickListener(v1 -> toAddressBook.changeFragment(R.id.fragment_myDoctors));

        DialogFragment askToDeleteRoleDialog = new AskToDeleteRoleDialog();
        deleteDiabetologist.setOnClickListener(view -> {
            if (!textViewNameDiabetologist.getText().toString().equals("not registered")) {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(diabetologistId)
                        .name(textViewNameDiabetologist.getText().toString())
                        .surname(textViewSurnameDiabetologist.getText().toString())
                        .role(getString(R.string.diabetologist))
                        .activity(1)
                        .dialogFragment(askToDeleteRoleDialog).fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            } else {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Diabetologist is not defined.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        deleteFamilyDoctor.setOnClickListener(view -> {
            if (!textViewNameFamily.getText().toString().equals("not registered")) {
                final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                        .id(familyId)
                        .name(textViewNameFamily.getText().toString())
                        .surname(textViewSurnameFamily.getText().toString())
                        .role(getString(R.string.family_doctor))
                        .activity(1)
                        .dialogFragment(askToDeleteRoleDialog).fragmentActivity(getActivity()).build();
                transmitData.changeFragmentToDialog();
            } else {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Family doctor is not defined.",
                        Toast.LENGTH_SHORT).show();
            }
        });
        Fragment fragment = new DoctorsFromAddressBookMainFragment();
        editDiabetologist.setOnClickListener(v13 -> {
            final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                    .id(diabetologistId)
                    .name(textViewNameDiabetologist.getText().toString())
                    .surname(textViewSurnameDiabetologist.getText().toString())
                    .role(getString(R.string.diabetologist))
                    .fragment(fragment).fragmentActivity(getActivity()).build();
            transmitData.changeFragment(R.id.fragment_myDoctors);
        });
        editFamilyDoctor.setOnClickListener(v14 -> {
            final TransmitData transmitData = new TransmitData.TransmitDataBuilder()
                    .id(familyId)
                    .name(textViewNameFamily.getText().toString())
                    .surname(textViewSurnameFamily.getText().toString())
                    .role(getString(R.string.family_doctor))
                    .fragment(fragment).fragmentActivity(getActivity()).build();
            transmitData.changeFragment(R.id.fragment_myDoctors);
        });
        return v;
    }

    /**
     * Set a text string for specific textView.
     */
    private void setText(TextView textView, String value) {
        if (value != null) {
            textView.setText(value);
        } else {
            textView.setText(R.string.not_registered);
        }
    }
}
