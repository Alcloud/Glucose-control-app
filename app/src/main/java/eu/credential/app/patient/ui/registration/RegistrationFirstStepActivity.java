package eu.credential.app.patient.ui.registration;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;

import com.example.administrator.credential_v020.R;

/**
 * Created by Aleksei Piatkin on 02.12.17.
 * <p>
 * A registration screen that offers registration via name/email/password.
 */
public class RegistrationFirstStepActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView nameView;
    private AutoCompleteTextView surnameView;
    private AutoCompleteTextView cityView;
    private AutoCompleteTextView emailView;
    private String name;
    private String surname;
    private String city;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_first_step);

        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_registration);
        toolbar.setTitle(R.string.toolbar_registration_first);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // To avoid automatically appear android keyboard when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Set up the registration form.
        nameView = findViewById(R.id.first_name);
        surnameView = findViewById(R.id.last_name);
        cityView = findViewById(R.id.city_registration);
        emailView = findViewById(R.id.email_registration);
        ImageButton nextImageButton = findViewById(R.id.next_imageButton);

        nextImageButton.setOnClickListener(v -> {
            // Store values at the time of the registration attempt.
            name = nameView.getText().toString();
            surname = surnameView.getText().toString();
            city = cityView.getText().toString();
            email = emailView.getText().toString();
            attemptRegister();
        });
    }

    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister() {

        // Reset errors.
        nameView.setError(null);
        surnameView.setError(null);
        emailView.setError(null);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid name.
        if (TextUtils.isEmpty(name)) {
            nameView.setError(getString(R.string.error_field_required));
            focusView = nameView;
            cancel = true;
            // Check for a valid surname.
        } else if (TextUtils.isEmpty(surname)) {
            surnameView.setError(getString(R.string.error_field_required));
            focusView = surnameView;
            cancel = true;

        } else if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;

        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Go to the next screen
            Intent intent = new Intent(RegistrationFirstStepActivity.this,
                    RegistrationSecondStepActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("surname", surname);
            intent.putExtra("city", city);
            intent.putExtra("email", email);
            startActivity(intent);
        }
    }

    private static boolean isEmailValid(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }
}
