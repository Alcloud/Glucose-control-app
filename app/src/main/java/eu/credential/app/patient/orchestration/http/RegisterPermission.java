package eu.credential.app.patient.orchestration.http;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import android.util.Base64;

import at.tugraz.iaik.pre.afgh.key.PREAFGHPrivateKey;
import at.tugraz.iaik.pre.afgh.PREAFGHGlobalParameters;
import at.tugraz.iaik.pre.afgh.key.PREAFGHPublicKey;
import at.tugraz.iaik.pre.afgh.key.PREAFGHReEncKeyGenerator;

import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.CreatePolicy;
import eu.credential.app.patient.helper.SavePreferences;

public class RegisterPermission extends AsyncTask<String, String, Void> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog dialog;

    public RegisterPermission(String doctorId, String resourceId, Context context) {
        super();
        this.doctorId = doctorId;
        this.resourceId = resourceId;
        this.context = context;
    }

    // get ids, which we need to send with policy
    private String doctorId;
    private String resourceId;
    private String ssoToken = SavePreferences.getDefaultsString("ssoToken", PatientApp.getContext());

    @Override
    protected void onPreExecute() {
        if (context != null) {
            dialog = ProgressDialog.show(context, "Einstellungen werden gespeichert", "Bitte warten");
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected Void doInBackground(String... params) {
        JSONObject responseJSONobject;

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("idproxy.protocol")
                .host("idproxy.host")
                .port("idproxy.port")
                .path("idproxy.path").build();

        KeyPair kpOwner = createKeyPairFromB64(SavePreferences.getDefaultsString("publicKey", PatientApp.getContext()),
                SavePreferences.getDefaultsString("privateKey", PatientApp.getContext()));

        String policy = CreatePolicy.createPolicy(resourceId, doctorId);
        Log.d("Policy: {}", policy);
        try {
            HttpURLConnection httpURLConnection = SetURLConnection.setConnection("GET",
                    doctorId, null);
            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                publishProgress("Connection successful");

                responseJSONobject = new JSONObject(SetURLConnection.jsonToString(httpURLConnection));
                String extractedPubKey = responseJSONobject.getJSONArray("credentialpublickey").get(0).toString();
                Log.d("Extracted PubKey: {}", extractedPubKey);

                byte[] pubKeyEncoded = Base64.decode(extractedPubKey, Base64.DEFAULT);
                PublicKey rsPub = new PREAFGHPublicKey(pubKeyEncoded);

                PublicKey reEncKey = PREAFGHReEncKeyGenerator.generateKey(kpOwner, rsPub, new PREAFGHGlobalParameters());

                String reEK = Base64.encodeToString(reEncKey.getEncoded(), Base64.NO_WRAP);
                // prepare request to RPS
                JSONObject registerPermissionObject = createRegisterPermissionObject(policy, ssoToken,
                        reEK, doctorId);
                Log.d("RPS request message: {}", registerPermissionObject.toString());

                new SetURLConnection.SetURLConnectionBuilder()
                        .protocol("permissions.protocol")
                        .host("permissions.host")
                        .port("permissions.port")
                        .path("permissions.path")
                        .ssoToken(ssoToken).build();

                HttpURLConnection httpURLConnection1 = SetURLConnection.setConnection("POST",
                        "regperm", registerPermissionObject.toString());
                Log.d("RPS_STATUS", String.valueOf(httpURLConnection1.getResponseCode()));
                if (httpURLConnection1.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
                    Log.d("RPS_response", "success");
                }
            }
        } catch (IOException | JSONException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
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
    protected void onPostExecute(Void result) {
        if (dialog != null) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    private static JSONObject createRegisterPermissionObject(String acceptedPolicy,
                                                             String owningUserSsoToken,
                                                             String reEncryptionKey,
                                                             String requestingUserUsername) throws JSONException {
        return new JSONObject(
                "{\"acceptedPolicy\":\"" + acceptedPolicy + "\",\n" +
                        "\"reEncryptionKey\":\"" + reEncryptionKey + "\",\n" +
                        "\"owningUser\":{\n" +
                        "\t\"ssoToken\":\"" + owningUserSsoToken + "\"\n" +
                        "\t},\n" +
                        "\"requestingUser\":{\n" +
                        "\t\"username\":\"" + requestingUserUsername + "\"\n" +
                        "\t}\n" +
                        "}");
    }

    private static KeyPair createKeyPairFromB64(String pubKeyStr, String privKeyStr) {
        PublicKey pubKey;
        PrivateKey privKey;
        KeyPair kp = null;
        try {
            privKey = new PREAFGHPrivateKey(Base64.decode(privKeyStr.getBytes(), Base64.DEFAULT));
            pubKey = new PREAFGHPublicKey(Base64.decode(pubKeyStr.getBytes(), Base64.DEFAULT));
            kp = new KeyPair(pubKey, privKey);
        } catch (InvalidKeyException e) {
            System.out.println("Exception during conversion to PublicKey and PrivateKey from String!");
            e.printStackTrace();
        }
        return kp;
    }
}
