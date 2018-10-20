package eu.credential.app.patient.ui.user_details;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.UpdateParticipantData;

/**
 * Created by Aleksei Piatkin on 12.01.18.
 * <p>
 * A users account data screen that offers edit user data.
 */
public class EditUserDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_details);
        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_edit_user_details);
        toolbar.setTitle("Edit user details");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        // Set up the login form.
        EditText nameView = findViewById(R.id.first_name);
        EditText surnameView = findViewById(R.id.last_name);
        EditText cityView = findViewById(R.id.city);
        EditText emailView = findViewById(R.id.email);

        ImageButton confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setOnClickListener(v -> {
            String name = nameView.getText().toString();
            String surname = surnameView.getText().toString();
            String city = cityView.getText().toString();
            String email = emailView.getText().toString();

            UpdateParticipantData updateParticipantData = new UpdateParticipantData.
                    UpdateParticipantBuilder()
                    .doctorName(name)
                    .doctorSurname(surname)
                    .doctorCity(city)
                    .doctorMainRole(email)
                    .dataId(SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext()))
                    .operationId("userDetails").build();
            updateParticipantData.execute();
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
