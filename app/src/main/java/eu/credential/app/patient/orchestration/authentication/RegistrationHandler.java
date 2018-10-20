package eu.credential.app.patient.orchestration.authentication;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;

import eu.credential.accessmanagement.common.Utils;
import eu.credential.accessmanagement.common.ClientProperties;
import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.http.CreateParticipantData;
import eu.credential.app.patient.orchestration.http.PHRdata;
import eu.credential.wallet.openam.LoginClient;
import eu.credential.wallet.openam.RegisterClientAFGH;

/**
 * Created by tfl on 24.07.17.
 * <p>
 * This class handles the registration tasks towards the CREDENTIAL Wallet.
 */

public class RegistrationHandler {

    private static final String TAG = RegistrationHandler.class.getSimpleName();
    private ClientProperties clientProperties;

    /**
     * Creates a CREDENTIAL Account for the user with the given username and password
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public RegistrationResponse createAccount(String accountId, String username, String userSurname,
                                              String userCity, String userEmail, String password) {

        try {
            clientProperties = Utils.getPropertiesFromContext(PatientApp.getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
        long startTime = System.nanoTime();

        String userKeyScheme = clientProperties.getProperty("USER_KEY_SCHEME");
        Log.d(TAG, "crypto scheme: " + userKeyScheme);

        RegisterClientAFGH registerClient;

        if (userKeyScheme.equals("AFGH")) {
            registerClient = new RegisterClientAFGH(
                    clientProperties.getProperty("openam.protocol"),
                    clientProperties.getProperty("openam.host"),
                    clientProperties.getProperty("openam.port"),
                    clientProperties.getProperty("openam.realm"));
        } else {
            Log.d("Registration Handler", "Old scheme LV not supported. Please use AFGH");
            return null;
        }

        if (registerClient.registerUser(accountId, password)) {
            long endTime = System.nanoTime();

            // Save accountId
            SavePreferences.setDefaultsString("accountId", accountId, PatientApp.getContext());

            Log.i(TAG, "Registration|Wallet registration|https://wallet.credentialdocker.eu:" +
                    "8080/piam/json/umarealm/selfservice/userRegistration|-|" + startTime / 1000000 +
                    "|" + (endTime - startTime) / 1000000 + "|" + "-");

            // if we are successful with registration we have to login the user to get a valid SSOToken
            LoginClient loginClient = new LoginClient(
                    clientProperties.getProperty("openam.protocol"),
                    clientProperties.getProperty("openam.host"),
                    clientProperties.getProperty("openam.port"),
                    clientProperties.getProperty("openam.realm"));

            String ssoToken = loginClient.login(accountId, password);
            SavePreferences.setDefaultsString("ssoToken", ssoToken, PatientApp.getContext());
            Log.d(TAG, "ssoToken after login: " + ssoToken);

            // Create address book, phr key, user details on DMS
            CreateParticipantData createParticipantData = new CreateParticipantData(accountId,
                    username, userSurname, userCity, userEmail);
            createParticipantData.execute();
            SavePreferences.setDefaultsString("userName", username, PatientApp.getContext());
            SavePreferences.setDefaultsString("userSurname", userSurname, PatientApp.getContext());
            SavePreferences.setDefaultsString("userCity", userCity, PatientApp.getContext());
            SavePreferences.setDefaultsString("userEmail", userEmail, PatientApp.getContext());

            //TODO: TEMP! Change keys save method to safe one!!!
            SavePreferences.setDefaultsString("privateKey", Base64.encodeToString(registerClient.
                    getPrvKeyEncoded(), Base64.DEFAULT), PatientApp.getContext());
            SavePreferences.setDefaultsString("publicKey", Base64.encodeToString(registerClient.
                    getPubKeyEncoded(), Base64.DEFAULT), PatientApp.getContext());

            //Connect to PHR, generate and save IDs
            PHRdata phrData = new PHRdata(accountId, "initPhr");
            phrData.execute();

            Log.d("Register:APPID:", SavePreferences.getDefaultsString("appid", PatientApp.getContext()));
            eu.credential.app.patient.orchestration.http.Request request =
                    new eu.credential.app.patient.orchestration.http.Request.RequestBuilder()
                            .addressURL("addPreferences")
                            .accountId(accountId)
                            .id(SavePreferences.getDefaultsString("appid", PatientApp.getContext()))
                            .dataId("ids")
                            .requestId("addAppId").build();
            request.execute();
            endTime = System.nanoTime();
            Log.i(TAG, "Registration|Registration|mix|-|" + startTime / 1000000 + "|" +
                    (endTime - startTime) / 1000000 + "|-");
            return RegistrationResponseBuilder.createSuccessfulRegistrationResponse();
        } else {
            return RegistrationResponseBuilder.createClientError();
        }
    }
}
