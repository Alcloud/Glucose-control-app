package eu.credential.app.patient.ui.searchParticipant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.GetParticipantData;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;

/**
 * Created by Aleksei Piatkin on 04.10.17.
 * <p>
 * A participant list screen that shows list of doctors after search operation.
 */
public class ParticipantListFragment extends ListFragment {

    private static final String TAG = "Performance";

    public static String doctorId;
    public static String name;
    public static String surname;
    public static String role;
    public static String city;
    private String dataId = SavePreferences.getDefaultsString("dataIdDMS1", PatientApp.getContext());

    ArrayList<Doctor> listDoctorAdapter = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getResources().getString(R.string.error_list));
        MyListAdapter myListAdapter = new MyListAdapter(getActivity(),
                R.layout.fragment_participant_list, SearchParticipant.listDoctor);
        setListAdapter(myListAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent documentIntent = new Intent(getActivity(), DoctorDetailsActivity.class);
        startActivity(documentIntent);
        TextView textViewId = v.findViewById(R.id.textViewDoctorIdDetails);
        TextView textViewName = v.findViewById(R.id.textViewDoctorNameDetails);
        TextView textViewSurname = v.findViewById(R.id.textViewDoctorSurnameDetails);
        TextView textViewRole = v.findViewById(R.id.textViewDoctorRoleDetails);
        TextView textViewCity = v.findViewById(R.id.textViewDoctorCityDetails);
        doctorId = textViewId.getText().toString();
        name = textViewName.getText().toString();
        surname = textViewSurname.getText().toString();
        role = textViewRole.getText().toString();
        city = textViewCity.getText().toString();
    }

    private class MyListAdapter extends ArrayAdapter<Doctor> {

        private Context myContext;

        MyListAdapter(Context context, int textViewResourceId, ArrayList<Doctor> listDoctor) {
            super(context, textViewResourceId, listDoctor);
            myContext = context;
            listDoctorAdapter = listDoctor;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            // return super.getView(position, convertView, parent);
            LayoutInflater inflater = (LayoutInflater) myContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View row = inflater.inflate(R.layout.fragment_participant_list,
                    parent, false);

            TextView doctorIdTextView = row.findViewById(R.id.textViewDoctorIdDetails);
            TextView doctorNameTextView = row.findViewById(R.id.textViewDoctorNameDetails);
            TextView doctorSurnameTextView = row.findViewById(R.id.textViewDoctorSurnameDetails);
            TextView doctorRoleTextView = row.findViewById(R.id.textViewDoctorRoleDetails);
            TextView doctorPostCodeTextView = row.findViewById(R.id.textViewDoctorPostCodeDetails);
            TextView doctorCityTextView = row.findViewById(R.id.textViewDoctorCityDetails);

            ImageView iconDoctor = row.findViewById(R.id.iconDoctor);
            iconDoctor.setImageResource(R.drawable.doctor);

            if (listDoctorAdapter.get(position).getId() != null) {
                doctorIdTextView.setText(listDoctorAdapter.get(position).getId());
            }
            if (listDoctorAdapter.get(position).getName() != null) {
                doctorNameTextView.setText(listDoctorAdapter.get(position).getName());
            }
            if (listDoctorAdapter.get(position).getSurname() != null) {
                doctorSurnameTextView.setText(listDoctorAdapter.get(position).getSurname());
            }
            if (listDoctorAdapter.get(position).getMainRole() != null) {
                doctorRoleTextView.setText(listDoctorAdapter.get(position).getMainRole());
            }
            if (listDoctorAdapter.get(position).getPostCode() != null) {
                doctorPostCodeTextView.setText(listDoctorAdapter.get(position).getPostCode());
            }
            if (listDoctorAdapter.get(position).getCity() != null) {
                doctorCityTextView.setText(listDoctorAdapter.get(position).getCity());
            }

            ImageButton addTo = row.findViewById(R.id.button_add_to);
            addTo.setOnClickListener(v -> {
                try {
                    long startTime = System.nanoTime();
                    GetParticipantData getParticipantData = new GetParticipantData(dataId);
                    JSONArray doctorArray = getParticipantData.execute().get();

                    if (doctorInAddressBookExist(doctorArray, listDoctorAdapter.get(position).getId()) == 0) {
                        // save data to address book
                        UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                                UpdateParticipantBuilder().array(doctorArray)
                                .doctorId(listDoctorAdapter.get(position).getId())
                                .doctorName(listDoctorAdapter.get(position).getName())
                                .doctorSurname(listDoctorAdapter.get(position).getSurname())
                                .doctorCity(listDoctorAdapter.get(position).getCity())
                                .doctorMainRole(listDoctorAdapter.get(position).getMainRole())
                                .doctorRole("null")
                                .dataId(dataId)
                                .context(myContext)
                                .operationId("addressBook").build();
                        updateParticipantData.execute();
                        long endTime = System.nanoTime();
                        Log.i(TAG, "Add doctor to address book|Add doctor to address book|mix|-|"
                                + startTime / 1000000 + "|" + (endTime - startTime) / 1000000 + "|-");
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Doctor " +
                                listDoctorAdapter.get(position).getName() + " " +
                                listDoctorAdapter.get(position).getSurname() +
                                " was added to address book", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Doctor " +
                                listDoctorAdapter.get(position).getName() + " " +
                                listDoctorAdapter.get(position).getSurname() +
                                " is already in the address book", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            });
            return row;
        }
    }

    private int doctorInAddressBookExist(JSONArray doctorArray, String id) throws JSONException {
        int j = 0;
        // check and inform if there is no connection
        if (doctorArray.length() > 0 && doctorArray.getJSONObject(0).has("error")) {
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
                if (doctorArray.getJSONObject(i).getString("id").equals(id)) {
                    j++;
                }
            }
        }
        return j;
    }
}