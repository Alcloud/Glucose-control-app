package eu.credential.app.patient.ui.my_doctors;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.AddressBookMainFragment;

public class MyDoctorsFragment extends Fragment {
    ImageButton addressBookDiabetologist;
    ImageButton addressBookFamily;
    TextView textViewNameDiabetologist;
    TextView textViewSurnameDiabetologist;
    TextView textViewRoleDiabetologist;
    TextView textViewCityDiabetologist;
    TextView textViewNameFamily;
    TextView textViewSurnameFamily;
    TextView textViewRoleFamily;
    TextView textViewCityFamily;
    private String accountId = "HansAugust";

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_camera);
        item.setVisible(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_doctors, container, false);
        textViewNameDiabetologist = (TextView) v.findViewById(R.id.textView_name_diabetologist);
        textViewSurnameDiabetologist = (TextView) v.findViewById(R.id.textView_surname_diabetologist);
        textViewRoleDiabetologist = (TextView) v.findViewById(R.id.textView_role_diabetologist);
        textViewCityDiabetologist = (TextView) v.findViewById(R.id.textView_city_diabetologist);
        textViewNameFamily = (TextView) v.findViewById(R.id.textView_name_family);
        textViewSurnameFamily = (TextView) v.findViewById(R.id.textView_surname_family);
        textViewRoleFamily = (TextView) v.findViewById(R.id.textView_role_family);
        textViewCityFamily = (TextView) v.findViewById(R.id.textView_city_family);

        GetParticipantData getParticipantData = new GetParticipantData(accountId);
        JSONArray doctorArray;
        try {
            // get data in online mode
            doctorArray = getParticipantData.execute().get();
            Log.d("CheckJSONarray", "Refreshed token: " + doctorArray);
            for (int i = 0; i < doctorArray.length(); i++) {
                String doctorName = doctorArray.getJSONObject(i).getString("name");
                String doctorSurname = doctorArray.getJSONObject(i).getString("surname");
                String doctorCity = doctorArray.getJSONObject(i).getString("city");
                String doctorMainRole = doctorArray.getJSONObject(i).getString("mainrole");
                String doctorRole = doctorArray.getJSONObject(i).getString("role");

                if (doctorRole.equals(getString(R.string.family_doctor))) {
                    setText(textViewNameFamily, doctorName);
                    setText(textViewSurnameFamily, doctorSurname);
                    setText(textViewRoleFamily, doctorMainRole);
                    setText(textViewCityFamily, doctorCity);

                }
                if (doctorRole.equals(getString(R.string.diabetologist))) {
                    setText(textViewNameDiabetologist, doctorName);
                    setText(textViewSurnameDiabetologist, doctorSurname);
                    setText(textViewRoleDiabetologist, doctorMainRole);
                    setText(textViewCityDiabetologist, doctorCity);
                }
            }
        } catch ( InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        addressBookDiabetologist = (ImageButton) v.findViewById(R.id.imageView_address_book_diabetologist);
        addressBookFamily = (ImageButton) v.findViewById(R.id.imageView_address_book_family);
        addressBookDiabetologist.setOnClickListener(v1 -> {
            changeFragmentToAddressBookMainFragment(getString(R.string.diabetologist), textViewNameDiabetologist,
                    textViewSurnameDiabetologist, textViewRoleDiabetologist, textViewCityDiabetologist);

        });
        addressBookFamily.setOnClickListener(v12 -> {
            changeFragmentToAddressBookMainFragment(getString(R.string.family_doctor), textViewNameFamily,
                    textViewSurnameFamily, textViewRoleFamily, textViewCityFamily);
        });
        return v;
    }

    private void setText(TextView textView, String value) {
        if (value != null) {
            textView.setText(value);
        } else {
            textView.setText("not registered");
        }
    }

    protected void changeFragmentToAddressBookMainFragment(String s, TextView textView, TextView textView1,
                                                           TextView textView2, TextView textView3) {
        //Send data to AddressBookMainFragment
        AddressBookMainFragment fragment = new AddressBookMainFragment();
        Bundle bundle = new Bundle();
        bundle.putString("role", s);
        bundle.putString("name", textView.getText().toString());
        bundle.putString("surname", textView1.getText().toString());
        bundle.putString("mainrole", textView2.getText().toString());
        bundle.putString("city", textView3.getText().toString());
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_myDoctors, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
