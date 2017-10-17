package eu.credential.app.patient.ui.searchParticipant;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;

public class ParticipantListFragment extends ListFragment {

    public static String name;
    public static String surname;
    public static String role;
    public static String city;
    private String accountId = "HansAugust";

    TextView doctorNameTextView;
    TextView doctorSurnameTextView;
    TextView doctorRoleTextView;
    TextView doctorCityTextView;

    // Hardcode dummy doctors data
    final String[] doctorName = new String[]{"Erna", "Sam", "Max"};
    final String[] doctorSurname = new String[]{"Mueller", "Smith", "Musterman"};
    final String[] doctorRole = new String[]{"diabetologist", "family doctor", "muster role"};
    final String[] doctorCity = new String[]{"Berlin", "Berlin", "Berlin"};

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        MyListAdapter myListAdapter = new MyListAdapter(getActivity(),
                R.layout.fragment_participant_list, doctorName);
        setListAdapter(myListAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent documentIntent = new Intent(getActivity(), DoctorDetailsActivity.class);
        startActivity(documentIntent);
        TextView textViewName = (TextView) v.findViewById(R.id.textViewDoctorNameDetails);
        TextView textViewSurname = (TextView) v.findViewById(R.id.textViewDoctorSurnameDetails);
        TextView textViewRole = (TextView) v.findViewById(R.id.textViewDoctorRoleDetails);
        TextView textViewCity = (TextView) v.findViewById(R.id.textViewDoctorCityDetails);
        name = textViewName.getText().toString();
        surname = textViewSurname.getText().toString();
        role = textViewRole.getText().toString();
        city = textViewCity.getText().toString();
    }

    private class MyListAdapter extends ArrayAdapter<String> {

        private Context myContext;

        MyListAdapter(Context context, int textViewResourceId,
                      String[] objects) {
            super(context, textViewResourceId, objects);
            myContext = context;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // return super.getView(position, convertView, parent);
            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.fragment_participant_list, parent,
                    false);
            doctorNameTextView = (TextView) row.findViewById(R.id.textViewDoctorNameDetails);
            doctorSurnameTextView = (TextView) row.findViewById(R.id.textViewDoctorSurnameDetails);
            doctorRoleTextView = (TextView) row.findViewById(R.id.textViewDoctorRoleDetails);
            doctorCityTextView = (TextView) row.findViewById(R.id.textViewDoctorCityDetails);
            doctorNameTextView.setText(doctorName[position]);
            doctorSurnameTextView.setText(doctorSurname[position]);
            doctorRoleTextView.setText(doctorRole[position]);
            doctorCityTextView.setText(doctorCity[position]);
            ImageView iconDoctor = (ImageView) row.findViewById(R.id.iconDoctor);

            iconDoctor.setImageResource(R.drawable.doctor);
            ImageButton addTo = (ImageButton) row.findViewById(R.id.button_add_to);
            addTo.setOnClickListener(v -> {

                try {
                    GetParticipantData getParticipantData = new GetParticipantData(accountId);
                    JSONArray doctorArray = getParticipantData.execute().get();

                    // TODO: Change "doctorSurname[position]", when doctors will be get from LDAP
                    if (doctorInAddressBookExist(doctorArray, doctorSurname[position]) == 0) {
                        // save data to address book
                        UpdateParticipantData updateParticipantData = new UpdateParticipantData
                                (doctorArray, null, accountId,
                                        doctorName[position], doctorSurname[position],
                                        doctorCity[position], doctorRole[position], null);
                        updateParticipantData.execute();
                        Toast.makeText(getActivity().getApplicationContext(), "Doctor " + doctorName[position] +
                                " was added to address book", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "Doctor " + doctorName[position] +
                                " is already in the address book", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            });
            return row;
        }
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
