package eu.credential.app.patient.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.app.patient.orchestration.http.CreateParticipantData;
import eu.credential.app.patient.orchestration.http.Request;
import eu.credential.app.patient.ui.registration.RegistrationFirstStepActivity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Performance measure values.
     */
    private static final String TAG = "Performance";
    long startTime;
    long endTime;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask loginTask = null;
    private Context context;
    private static boolean login;

    // UI references.
    private AutoCompleteTextView accountView;
    private AutoCompleteTextView passwordView;
    private View progressView;
    private View loginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = PatientApp.getContext();

        //Toolbar settings
        Toolbar toolbar = findViewById(R.id.toolbar_login);
        toolbar.setTitle(R.string.toolbar_signIn);
        toolbar.setTitleTextColor(Color.WHITE);

        // To avoid automatically appear android keyboard when activity start
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Set up the login form.
        accountView = findViewById(R.id.email_login);
        passwordView = findViewById(R.id.password);

        ImageButton accountSignInButton = findViewById(R.id.account_sign_in_button);
        accountSignInButton.setOnClickListener(view -> attemptLogin());

        loginFormView = findViewById(R.id.login_form);
        progressView = findViewById(R.id.login_progress);
        TextView register = findViewById(R.id.register_button);
        register.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegistrationFirstStepActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (loginTask != null) {
            return;
        }

        // Reset errors.
        accountView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String accountId = accountView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordView.setError(getString(R.string.error_invalid_password));
            focusView = passwordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(accountId)) {
            accountView.setError(getString(R.string.error_field_required));
            focusView = accountView;
            cancel = true;
        } else if (!isAccountNameValid(accountId)) {
            accountView.setError(getString(R.string.error_invalid_account));
            focusView = accountView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            loginTask = new UserLoginTask(accountId, password);
            loginTask.execute((Void) null);
        }
    }

    private boolean isAccountNameValid(String accountName) {
        return !accountName.isEmpty();
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String accountId;
        private final String password;

        UserLoginTask(String accountId, String password) {
            this.accountId = accountId;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            new SetURLConnection.SetURLConnectionBuilder()
                    .protocol("wallet.protocol")
                    .host("wallet.host")
                    .port("wallet.port")
                    .path("wallet.path").build();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                startTime = System.nanoTime();

                SavePreferences.setDefaultsString("accountId", accountId, context);

                URL url = new URL(SetURLConnection.setURL() + "/authenticate");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setRequestProperty("X-OpenAM-Username", accountId);
                httpURLConnection.setRequestProperty("X-OpenAM-Password", password);
                httpURLConnection.setRequestProperty("Content-Type", "application/json");
                httpURLConnection.connect();
                httpURLConnection.getResponseCode();

                endTime = System.nanoTime();
                Log.i(TAG, "Login|Authenticate|" + SetURLConnection.setURL() + "/authenticate"
                        + "|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    login = true;
                    // Get ssoToken
                    JSONObject responseJSON = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));
                    String ssoToken = responseJSON.getString("tokenId");
                    SavePreferences.setDefaultsString("ssoToken", ssoToken, context);
                    Log.d("ssoToken", "ssoToken: " + ssoToken);

                    CreateParticipantData createParticipantData = new CreateParticipantData(accountId, true);
                    createParticipantData.execute();

                    Request request = new Request.RequestBuilder()
                            .accountId(accountId)
                            .requestId("getPreference").context(getApplicationContext()).build();
                    request.execute();

                    SavePreferences.setDefaultsBoolean("login", true, context);

                    endTime = System.nanoTime();
                    Log.i(TAG, "Login|Login|mix|-|" + startTime / 1000000 + "|" +
                            (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());
                } else {
                    login = false;
                    Log.d("Login", "no connection");
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return login;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            loginTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
                passwordView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            loginTask = null;
            showProgress(false);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
