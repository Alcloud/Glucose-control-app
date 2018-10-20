package eu.credential.app.patient.ui.user_details;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import org.json.JSONArray;
import org.json.JSONException;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.AsyncTaskCompleteListener;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.GetParticipantData;

/**
 * Created by Aleksei Piatkin on 24.11.17.
 * <p>
 * A users account data screen.
 */
public class UserActivity extends AppCompatActivity implements AsyncTaskCompleteListener<JSONArray> {

    // DMS id of user account data.
    private String dataId = SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext());

    // UI references
    private TextView userName;
    private TextView userSurname;
    private TextView userEmail;
    private TextView userCity;
    private TextView notificationOne;
    private TextView notificationTwo;
    private Toolbar toolbar;
    private JSONArray userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // Init UI
        ImageButton logout = findViewById(R.id.button_logout);
        ImageButton editUserDetails = findViewById(R.id.button_change_details);
        toolbar = findViewById(R.id.toolbar_user_details);
        userName = findViewById(R.id.textView_user_name);
        userSurname = findViewById(R.id.textView_user_surname);
        userEmail = findViewById(R.id.textView_user_email);
        userCity = findViewById(R.id.textView_user_city);
        notificationOne = findViewById(R.id.notification_one_text);
        notificationTwo = findViewById(R.id.notification_two_text);

        // Init toolbar
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        updateUserDetails();

        // logout
        logout.setOnClickListener(v -> {
            AskToLogout askToLogout = new AskToLogout();
            askToLogout.show(getSupportFragmentManager(), "Logout");
        });
        // edit user account data
        editUserDetails.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditUserDetailsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateUserDetails();
    }

    /**
     * To update a user account data (name, surname, city, e-mail).
     */
    private void updateUserDetails() {
        if (SavePreferences.getDefaultsBoolean("checkNewData", PatientApp.getContext())) {
            notificationOne.setText(getString(R.string.checkbox_new_data));
        } else {
            notificationOne.setText("");
        }
        if (SavePreferences.getDefaultsBoolean("checkDocumentAccess", PatientApp.getContext())) {
            notificationTwo.setText(getString(R.string.checkbox_contacts));
        } else {
            notificationTwo.setText("");
        }

        GetParticipantData getParticipantData = new GetParticipantData(dataId, UserActivity.this, this);
            // get data from dms service
            getParticipantData.execute();
            // check and inform if there is no connection
    }

    @Override
    public void onTaskComplete(JSONArray result) {
        userDetails = result;
        try {
            if (userDetails.getJSONObject(0).has("error")) {
                   /* AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                    builder.setIcon(android.R.drawable.ic_dialog_info);
                    builder.setTitle("Warning!");
                    builder.setMessage(getString(R.string.server_not_respond));
                    builder.setPositiveButton(getString(R.string.ok),
                            (arg0, arg1) -> {
                            });
                    builder.show();*/
            } else {
                userName.setText(userDetails.getJSONObject(0).getString("name"));
                userSurname.setText(userDetails.getJSONObject(0).getString("surname"));
                userCity.setText(userDetails.getJSONObject(0).getString("city"));
                userEmail.setText(userDetails.getJSONObject(0).getString("email"));
                toolbar.setTitle(userName.getText() + " " + userSurname.getText());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
