package eu.credential.app.patient.ui.searchParticipant;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.administrator.credential_v020.R;

public class SearchParticipant extends Fragment {

    ImageButton clearSearch;
    ImageButton searchParticipant;
    Spinner spinnerRole;
    Spinner spinnerProfession;
    EditText editTextCity;
    EditText editTextName;

    // Hardcode dummy doctors data
    final String[] roleList = new String[]{"all", "doctor", "patient"};
    final String[] profList = new String[]{"all", "diabetologist", "family doctor"};

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
        View v = inflater.inflate(R.layout.fragment_search_participant, container, false);

        searchParticipant = (ImageButton) v.findViewById(R.id.button_search_participant);
        clearSearch = (ImageButton) v.findViewById(R.id.button_clear_participant_search);
        spinnerRole = (Spinner) v.findViewById(R.id.spinner_role);
        spinnerProfession = (Spinner) v.findViewById(R.id.spinner_profession);
        editTextCity = (EditText) v.findViewById(R.id.participant_city);
        editTextName = (EditText) v.findViewById(R.id.participant_name);

        setSpinnerAdapter(spinnerRole, roleList);
        setSpinnerAdapter(spinnerProfession, profList);

        searchParticipant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParticipantListMainFragment fragment =new ParticipantListMainFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.search_participant, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                //TODO: Implement search logic

                String participantCity = editTextCity.getText().toString();
                String participantName = editTextName.getText().toString();
            }
        });
        return v;
    }

    private void setSpinnerAdapter (Spinner spinner, String[] itemList){
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.row_spinner_search, R.id.item_search, itemList);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
}
