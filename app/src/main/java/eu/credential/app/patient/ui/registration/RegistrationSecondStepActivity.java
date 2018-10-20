package eu.credential.app.patient.ui.registration;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import java.util.Objects;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.authentication.RegistrationHandler;
import eu.credential.app.patient.orchestration.authentication.RegistrationResponse;
import eu.credential.app.patient.ui.MainActivity;

public class RegistrationSecondStepActivity extends AppCompatActivity {
    /**
     * Keep track of the user registration task to ensure we can cancel it if requested
     */
    private UserRegistrationTask regTask = null;

    // UI references.
    private AutoCompleteTextView accountNameView;
    private AutoCompleteTextView passwordViewFirst;
    private AutoCompleteTextView passwordViewSecond;

    private View progressView;
    private View registrationFormView;
    private TextView statusTextView;
    private static String name;
    private static String surname;
    private static String city;
    private static String email;
    private static String accountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_second_step);

        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_registration);
        toolbar.setTitle(R.string.toolbar_registration_second);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());

        // To avoid automatically appear android keyboard when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        name = getIntent().getStringExtra("name");
        surname = getIntent().getStringExtra("surname");
        city = getIntent().getStringExtra("city");
        email = getIntent().getStringExtra("email");

        accountNameView = findViewById(R.id.account_name);
        passwordViewFirst = findViewById(R.id.password_first);
        passwordViewSecond = findViewById(R.id.password_second);
        progressView = findViewById(R.id.registration_progress);
        registrationFormView = findViewById(R.id.registration_form);
        statusTextView = findViewById(R.id.registrationTextBox);
        ImageButton registrationButton = findViewById(R.id.register_imageButton);
        ImageButton previousButton = findViewById(R.id.previous_imageButton);

        registrationButton.setOnClickListener(view -> attemptRegister());
        previousButton.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Attempts to register the account specified by the registration form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual registration attempt is made.
     */
    private void attemptRegister() {
        if (regTask != null) {
            return;
        }

        // Reset errors.
        passwordViewFirst.setError(null);
        passwordViewSecond.setError(null);

        // Store values at the time of the registration attempt.
        accountName = accountNameView.getText().toString();
        String passwordFirst = passwordViewFirst.getText().toString();
        String passwordSecond = passwordViewSecond.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordFirst) && passwordInvalid(passwordFirst)) {
            passwordViewFirst.setError(getString(R.string.error_invalid_password));
            focusView = passwordViewFirst;
            passwordViewFirst.setText("");
            cancel = true;
        }
        if (!TextUtils.isEmpty(passwordSecond) && passwordInvalid(passwordSecond)) {
            passwordViewSecond.setError(getString(R.string.error_invalid_password));
            focusView = passwordViewSecond;
            passwordViewSecond.setText("");
            cancel = true;
        }
        if (!passwordFirst.equals(passwordSecond)) {
            passwordViewSecond.setError(getString(R.string.error_matching_password));
            focusView = passwordViewSecond;
            passwordViewFirst.setText("");
            passwordViewSecond.setText("");
            cancel = true;
        }

        // Check for a valid account name.
        if (TextUtils.isEmpty(accountName)) {
            accountNameView.setError(getString(R.string.error_field_required));
            focusView = accountNameView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt registration and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user registration attempt.
            showProgress(true);
            regTask = new UserRegistrationTask(accountName, name, surname, city, email, passwordSecond);
            regTask.execute((Void) null);
        }
    }

    private boolean passwordInvalid(String password) {
        return password.length() <= 8;
    }

    @SuppressLint("StaticFieldLeak")
    public class UserRegistrationTask extends AsyncTask<Void, Void, RegistrationResponse> {

        private String accountId;
        private String userName;
        private String userSurname;
        private String userCity;
        private String userEmail;
        private String userPassword;

        UserRegistrationTask(String accountId, String userName, String userSurname, String userCity,
                             String userEmail, String password) {
            this.accountId = accountId;
            this.userName = userName;
            this.userSurname = userSurname;
            this.userCity = userCity;
            this.userEmail = userEmail;
            this.userPassword = password;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected RegistrationResponse doInBackground(Void... params) {
            RegistrationHandler regHandler = new RegistrationHandler();
            return regHandler.createAccount(accountId, userName, userSurname, userCity, userEmail, userPassword);
        }

        @Override
        protected void onPostExecute(final RegistrationResponse response) {
            regTask = null;
            showProgress(false);

            switch (response.status) {
                case RegistrationResponse.successfullRegistration:
                    SavePreferences.setDefaultsBoolean("login", true, PatientApp.getContext());
                    RegistrationInfo registrationInfo = new RegistrationInfo();
                    registrationInfo.show(getSupportFragmentManager(), "Registration");

                    break;
                case RegistrationResponse.accountAlreadyAvailable:
                    accountNameView.setError(getString(R.string.error_reg_account_already_available));
                    accountNameView.requestFocus();
                    break;
                case RegistrationResponse.clientError:
                    statusTextView.setError(getString(R.string.error_req_client_error));
                    statusTextView.setText(R.string.error_req_client_error);
                    break;
                case RegistrationResponse.passwordIncorect:
                    passwordViewSecond.setError(getString(R.string.error_reg_password_incorrect));
                    passwordViewSecond.requestFocus();
                    break;
                case RegistrationResponse.serviceNotReachable:
                    statusTextView.setError(getString(R.string.error_req_service_not_reachable));
                    break;
            }
        }

        @Override
        protected void onCancelled() {
            regTask = null;
            showProgress(false);
        }
    }

    /**
     * Shows the progress UI and hides the registration form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        registrationFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                registrationFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progressView.setVisibility(show ? View.VISIBLE : View.GONE);
        progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /**
     * Dialog screen that shows some registration info after successful registration.
     */
    @SuppressLint("ValidFragment")
    public static class RegistrationInfo extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String title = "Welcome to Credential app";
            String message = "Congratulations, " + name + " " + surname +
            "!\n You are successfully registered under " + accountName + " name.";

            AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
            builder.setTitle(title)
                    .setMessage(message)
                    .setIcon(R.drawable.credential_logo);
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                SavePreferences.setDefaultsBoolean("firstLogin", true, PatientApp.getContext());
                Intent i = new Intent(getActivity(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            });
            builder.setCancelable(false);
            return builder.create();
        }
    }
}
