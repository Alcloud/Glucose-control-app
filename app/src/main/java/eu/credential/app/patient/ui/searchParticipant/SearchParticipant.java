package eu.credential.app.patient.ui.searchParticipant;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.helper.SetURLConnection;

/**
 * Created by Aleksei Piatkin on 01.11.17.
 * <p>
 * A screen that offers participant search with filter.
 */
public class SearchParticipant extends Fragment implements AsyncTaskCompleteListener<JSONArray> {

    private static final String TAG = "Performance";
    private ProgressDialog dialog;
    private GetLDAPdata getLDAPdata;
    private Context context;

    // init UI
    private Spinner spinnerProfession;
    private AutoCompleteTextView editTextCity;
    private AutoCompleteTextView editTextName;
    private String participantPLZ;
    private String participantName;
    private String profession;

    private JSONArray doctorArray;
    public static ArrayList<Doctor> listDoctor = new ArrayList<>();

    final String[] profList = new String[]{"all", "Diabetologe", "Family doctor"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_participant, container, false);

        context = getContext();
        ImageButton searchParticipant = v.findViewById(R.id.button_search_participant);
        ImageButton clearSearch = v.findViewById(R.id.button_clear_participant_search);
        spinnerProfession = v.findViewById(R.id.spinner_profession);
        editTextCity = v.findViewById(R.id.participant_city);
        editTextName = v.findViewById(R.id.participant_name);
        setSpinnerAdapter(spinnerProfession, profList);

        searchParticipant.setOnClickListener(v1 -> {
            long startTime = System.nanoTime();
            listDoctor.clear();

            participantPLZ = editTextCity.getText().toString();
            participantName = editTextName.getText().toString();

            // search right list of doctors from LDAP
            getLDAPdata = new GetLDAPdata(this);
            getLDAPdata.execute();

            long endTime = System.nanoTime();
            Log.i(TAG, "Search participant|Search participant|mix|-|" + startTime / 1000000
                    + "|" + (endTime - startTime) / 1000000 + "|-");
        });

        clearSearch.setOnClickListener(v2 -> {
            listDoctor.clear();
            spinnerProfession.setSelection(0);
            editTextCity.setText("");
            editTextName.setText("");
        });
        return v;
    }

    private void setDoctorList(int i) throws JSONException {
        Doctor doctor = new Doctor();
        doctor.setId(doctorArray.getJSONObject(i).getString("uid").substring(5));
        doctor.setPostCode(doctorArray.getJSONObject(i).getString("postalCode"));
        doctor.setName(doctorArray.getJSONObject(i).getString("hpdProviderMailingAddress"));
        doctor.setSurname(doctorArray.getJSONObject(i).getString("hcRegisteredName"));
        doctor.setCity(doctorArray.getJSONObject(i).getString("postalAddress"));
        doctor.setMainRole(doctorArray.getJSONObject(i).getString("hcSpecialization"));
        listDoctor.add(doctor);
    }

    private void searchFilter(int i) throws JSONException {
        if (participantPLZ.equals("") && participantName.equals("")) {
            setDoctorList(i);
        }
        if (participantPLZ.equals(doctorArray.getJSONObject(i).getString("postalCode")) && participantName.equals("")) {
            setDoctorList(i);
        }
        if (participantPLZ.equals("") && (participantName.equals(doctorArray.getJSONObject(i).getString("hpdProviderMailingAddress")) ||
                participantName.equals(doctorArray.getJSONObject(i).getString("hcRegisteredName")))) {
            setDoctorList(i);
        }
        if (participantPLZ.equals(doctorArray.getJSONObject(i).getString("postalCode")) &&
                (participantName.equals(doctorArray.getJSONObject(i).getString("hpdProviderMailingAddress")) ||
                        participantName.equals(doctorArray.getJSONObject(i).getString("hcRegisteredName")))) {
            setDoctorList(i);
        }
        ParticipantListMainFragment fragment = new ParticipantListMainFragment();
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.search_participant, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void setSpinnerAdapter(Spinner spinner, String[] itemList) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.row_spinner_search, R.id.item_search, itemList);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profession = spinnerProfession.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                profession = "all";
            }
        });
    }

    @Override
    public void onTaskComplete(JSONArray result) {
        doctorArray = result;
        for (int i = 0; i < doctorArray.length(); i++) {
            try {
                if (doctorArray.getJSONObject(i).has("error")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("Warning!");
                    builder.setMessage(getString(R.string.server_not_respond));
                    builder.setPositiveButton(getString(R.string.ok),
                            (arg0, arg1) -> {
                            });
                    builder.show();
                } else {
                    // search filter
                    if (profession.equals(doctorArray.getJSONObject(i).getString("hcSpecialization"))) {
                        searchFilter(i);
                    } else if (profession.equals("all")) {
                        searchFilter(i);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class GetLDAPdata extends AsyncTask<String, String, JSONArray> {

        private static final String TAG = "Performance";
        private AsyncTaskCompleteListener<JSONArray> callback;
        private boolean flag = true;

        public GetLDAPdata(AsyncTaskCompleteListener<JSONArray> callback) {
            super();
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(context, "Searching...", "Bitte warten");
            dialog.setCanceledOnTouchOutside(true);
        }

        @Override
        protected JSONArray doInBackground(String... params) {
            JSONObject responseJSON;
            JSONArray parameter = new JSONArray();
            JSONArray error = new JSONArray();

            new SetURLConnection.SetURLConnectionBuilder()
                    .protocol("phr2.protocol")
                    .host("phr2.host")
                    .port("phr2.port")
                    .path("hpd.path").build();
            try {
                long startTime = System.nanoTime();

                error.put(new JSONObject("{\"error\": \"true\"}"));

                JSONObject requestMessage = new JSONObject("{\"o\":\"Arztpraxis\"}");
                HttpURLConnection httpURLConnection = SetURLConnection.setConnection("POST",
                        "search", requestMessage.toString());

                long endTime = System.nanoTime();
                Log.i(TAG, "Search participant|Search for doctor (LDAP)|" + SetURLConnection.setURL()
                        + "/search|-|" + startTime / 1000000 + "|" + (endTime - startTime) / 1000000 + "|"
                        + httpURLConnection.getResponseCode());
                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                    publishProgress("Connection successful");

                    responseJSON = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));
                    for (int i = 0; i < responseJSON.getJSONArray("results").length(); i++) {
                        String uid = responseJSON.getJSONArray("results").getJSONObject(i).getString("uid");
                        startTime = System.nanoTime();

                        HttpURLConnection httpURLConnection2 = SetURLConnection.setConnection("GET",
                                "details?uid=" + uid, null);
                        httpURLConnection2.getResponseCode();

                        endTime = System.nanoTime();
                        Log.i(TAG, "Search participant|Get doctor details (LDAP)|" +
                                SetURLConnection.setURL() + "/details?uid=|" + uid + "|" + startTime / 1000000 + "|" +
                                (endTime - startTime) / 1000000 + "|" + httpURLConnection2.getResponseCode());

                        // Control url and header
                        Log.d("requestCODELDAP", "request url: " + httpURLConnection2.getURL()
                                + "\n" + "method: " + httpURLConnection2.getRequestMethod() + "\n");
                        for (String headerKey : httpURLConnection2.getHeaderFields().keySet()) {
                            Log.d("requestCODELDAP", "header: " + headerKey + "="
                                    + httpURLConnection2.getHeaderField(headerKey));
                        }

                        if (httpURLConnection2.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            publishProgress("Getting data...");

                            JSONObject doctorDetails = new JSONObject(SetURLConnection.jsonToString(httpURLConnection2));
                            parameter.put(doctorDetails);
                        }
                    }
                } else {
                    Log.d("myLogs", "no connection");
                    flag = false;
                }
            } catch (IOException | JSONException e) {
                flag = false;
                e.printStackTrace();
            }
            if (!flag) {
                return error;
            } else return parameter;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            if (dialog != null) {
                if (values[0] != null) {
                    dialog.setMessage(values[0]);
                }
            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            if (callback != null) {
                callback.onTaskComplete(result);
            }
        }

        @Override
        protected void onCancelled() {
            getLDAPdata = null;
            dialog.dismiss();
        }
    }
}