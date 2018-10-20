package eu.credential.app.patient.helper;

import android.content.Context;
import android.support.v7.app.AlertDialog;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import eu.credential.app.patient.helper.Doctor;
import eu.credential.app.patient.orchestration.http.GetParticipantData;

/**
 * Created by Aleksei Piatkin on 24.10.17.
 * <p>
 * This class makes it possible to get data from dms and refresh address book.
 */
public class GetData {
    private ArrayList<Doctor> listDoctor = new ArrayList<>();
    private String accountId;
    private JSONArray doctorArray;
    private Context context;

    public ArrayList<Doctor> getListDoctor() {
        return listDoctor;
    }

    public JSONArray getJSONArray() {
        return doctorArray;
    }

    public GetData(String accountId, Context context) {
        this.accountId = accountId;
        this.context = context;
    }

    /**
     * get actual list of doctors in address book.
     */
    public void refreshList() {
        listDoctor.clear();
        GetParticipantData getParticipantData = new GetParticipantData(accountId);
        try {
            doctorArray = getParticipantData.execute().get();
            // check and inform if there is no connection
            if (doctorArray.getJSONObject(0).has("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(context.getString(R.string.server_not_respond));
                builder.setPositiveButton(context.getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                for (int i = 0; i < doctorArray.length(); i++) {
                    String[] doctorRole = new String[2];
                    Doctor doctor = new Doctor();
                    String doctorId = doctorArray.getJSONObject(i).getString("id");
                    String doctorName = doctorArray.getJSONObject(i).getString("name");
                    String doctorSurname = doctorArray.getJSONObject(i).getString("surname");
                    String doctorCity = doctorArray.getJSONObject(i).getString("city");
                    String doctorMainRole = doctorArray.getJSONObject(i).getString("mainrole");
                    doctorRole[0] = (String) doctorArray.getJSONObject(i).getJSONArray("role").get(0);
                    doctorRole[1] = (String) doctorArray.getJSONObject(i).getJSONArray("role").get(1);

                    doctor.setId(doctorId);
                    doctor.setName(doctorName);
                    doctor.setSurname(doctorSurname);
                    doctor.setCity(doctorCity);
                    doctor.setMainRole(doctorMainRole);
                    doctor.setRole(doctorRole);
                    listDoctor.add(doctor);
                }
            }
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }
}
