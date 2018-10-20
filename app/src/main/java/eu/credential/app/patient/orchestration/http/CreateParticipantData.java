package eu.credential.app.patient.orchestration.http;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import at.tugraz.iaik.pre.afgh.key.PREAFGHPrivateKey;
import at.tugraz.iaik.pre.afgh.key.PREAFGHPublicKey;
import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.helper.SetURLConnection;
import eu.credential.dms.common.utils.Crypto;

/**
 * Created by Aleksei Piatkin on 12.12.17.
 * <p>
 * This class helps to create and save a participant data in DMS.
 * Participant data:
 * dataIdDMS1 - Address book
 * dataIdDMS2 - PHR key
 * dataIdDMS3 - User details
 * dataIdDMS4 - Glucose document id
 * dataIdDMS5 - Weight document id
 */
public class CreateParticipantData extends AsyncTask<String, Void, Void> {


    private static final String TAG = "Performance";

    private String accountId;
    private String userName = "";
    private String userSurname = "";
    private String userCity = "";
    private String userEmail = "";
    private boolean login = false;

    private static final int USER_DATA_FIELDS = 3;

    public CreateParticipantData(String accountId, String userName, String userSurname,
                                 String userCity, String userEmail) {
        super();
        this.accountId = accountId;
        this.userName = userName;
        this.userSurname = userSurname;
        this.userCity = userCity;
        this.userEmail = userEmail;
    }

    public CreateParticipantData(String accountId, boolean login) {
        super();
        this.accountId = accountId;
        this.login = login;
    }

    @Override
    protected Void doInBackground(String... params) {
        JSONObject requestMessage;
        JSONObject responseJSON;
        JSONObject responseJSONobject;
        JSONArray responseJSONarray;
        String metadata;

        new SetURLConnection.SetURLConnectionBuilder()
                .protocol("dms.protocol")
                .host("dms.host")
                .path("dms.path").build();
        try {
            long startTime = System.nanoTime();

            HttpURLConnection httpURLConnection1 = SetURLConnection.setConnection("GET",
                    "data-root/" + accountId, null);
            httpURLConnection1.getResponseCode();

            long endTime = System.nanoTime();
            Log.i(TAG, "Registration or Login|Get root DMS id|" + SetURLConnection.setURL()
                    + "/data-root/|" + accountId + "|" + startTime / 1000000 + "|" +
                    (endTime - startTime) / 1000000 + "|" + httpURLConnection1.getResponseCode());

            if (httpURLConnection1.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseJSON = new JSONObject(SetURLConnection.jsonToString(httpURLConnection1));
                String parentId = responseJSON.getJSONObject("metadata").getJSONObject("identifier")
                        .getString("dataId");

                SavePreferences.setDefaultsString("parentId", parentId, PatientApp.getContext());

                if (login) {
                    HttpURLConnection httpURLConnection2 = SetURLConnection.setConnection("GET",
                            "data/" + parentId, null);
                    if (httpURLConnection2.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        responseJSONobject = new JSONObject(SetURLConnection.jsonToString(httpURLConnection2));
                        responseJSONarray = responseJSONobject.getJSONArray("metadataChildren");
                        for (int i = 0; i < responseJSONarray.length(); i++) {
                            metadata = responseJSONarray.getJSONObject(i).getJSONObject("identifier").getString("dataId");
                            SavePreferences.setDefaultsString("dataIdDMS" + String.valueOf(i + 1) +
                                    "", metadata, PatientApp.getContext());
                        }
                    }
                }
                if (!login) {
                    String[] itemName = {"addressBook", "phrKey", "userDetails"};

                    String userDetails = "{" + "\\" + "\"name" + "\\" + "\":" + "\\" + "\"" + userName +
                            "\\" + "\"," + "\\" + "\"surname" + "\\" + "\":" + "\\" + "\"" + userSurname +
                            "\\" + "\"," + "\\" + "\"city" + "\\" + "\":" + "\\" + "\"" + userCity + "\\" +
                            "\"," + "\\" + "\"email" + "\\" + "\":" + "\\" + "\"" + userEmail + "\\" + "\"}";

                    String[] object = {"null", "", userDetails};

                    for (int i = 0; i < USER_DATA_FIELDS; i++) {
                        String dataId = UUID.randomUUID().toString();
                        SavePreferences.setDefaultsString("dataIdDMS" + String.valueOf(i + 1) +
                                "", dataId, PatientApp.getContext());

                        requestMessage = new JSONObject("{\n" +
                                "  \"metadata\": {\n" +
                                "    \"identifier\": {\n" +
                                "      \"dataId\":\"" + dataId + "\",\n" +
                                "      \"ownerId\":\"" + accountId + "\",\n" +
                                "      \"parentId\":\"" + parentId + "\",\n" +
                                "      \"dataType\": \"file\",\n" +
                                "      \"name\":\"" + itemName[i] + "\"\n" +
                                "    },\n" +
                                " \"appSpecific\":{  },\n" +
                                "      \"tags\":[\n" +
                                "         \"name:Record Key\",\n" +
                                "         \"type: file\"\n" +
                                "      ],\n" +
                                "      \"fileSpecific\":{\n" +
                                "         \"crypto\":{\n" +
                                "            \"encapsulationKey\":{\n" +
                                "               \"type\":\"PRE-AFGH\"\n" +
                                "            }\n" +
                                "         },\n" +
                                "         \"metaPart\":[  \n" +
                                "     ],\n" +
                                "     \"parts\":[  \n" +
                                "        {  \n" +
                                "           \"tags\":[  \n" +
                                "              \"attr1\"\n" +
                                "           ]\n" +
                                "        }\n" +
                                "     ]\n" +
                                "  }\n" +
                                "\n" +
                                "   },\n" +
                                "    \"fileContent\": [\"" + object[i] + "\"]\n" +
                                "}");
                        startTime = System.nanoTime();

                        HttpURLConnection httpURLConnection = SetURLConnection.setConnection("PUT",
                                "data/" + dataId, requestMessage.toString());

                        endTime = System.nanoTime();
                        Log.i(TAG, "Registration|Add user data|" + SetURLConnection.setURL()
                                + "/data/|" + dataId + "|" + startTime / 1000000 + "|" +
                                (endTime - startTime) / 1000000 + "|" + httpURLConnection.getResponseCode());
                    }
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        if (login && result == null) {
            String dataIdDMS3 = SavePreferences.getDefaultsString("dataIdDMS3", PatientApp.getContext());
            String dataIdDMS2 = SavePreferences.getDefaultsString("dataIdDMS2", PatientApp.getContext());
            try {
                JSONArray userDetails = new GetParticipantData(dataIdDMS3).execute().get();

                String name = userDetails.getJSONObject(0).getString("name");
                SavePreferences.setDefaultsString("userName", name, PatientApp.getContext());
                String surname = userDetails.getJSONObject(0).getString("surname");
                SavePreferences.setDefaultsString("userSurname", surname, PatientApp.getContext());
                String city = userDetails.getJSONObject(0).getString("city");
                SavePreferences.setDefaultsString("userCity", city, PatientApp.getContext());
                String email = userDetails.getJSONObject(0).getString("email");
                SavePreferences.setDefaultsString("userEmail", email, PatientApp.getContext());

                JSONArray phrDetails = new GetParticipantData(dataIdDMS2).execute().get();

                if (phrDetails != null && phrDetails.length() > 0) {

                    String[] realContentStrArray = {phrDetails.getString(0), phrDetails.getString(1)};

                    KeyPair kpShared = createKeyPairFromB64(SavePreferences.getDefaultsString("publicKey", PatientApp.getContext()),
                            SavePreferences.getDefaultsString("privateKey", PatientApp.getContext()));

                    String[] decrypted = decrypt(realContentStrArray, kpShared);

                    Log.d("PhrKeyReader", "The decrypted message is " + Arrays.toString(decrypted));

                    String folderId = "";
                    String phrToken = "";
                    if (decrypted != null) {
                        folderId = decrypted[0].substring(decrypted[0].lastIndexOf(" ") + 1);
                        phrToken = decrypted[1].substring(decrypted[1].lastIndexOf(" ") + 1);
                    }

                    SavePreferences.setDefaultsString("registryObject", folderId, PatientApp.getContext());
                    SavePreferences.setDefaultsString("phrToken", phrToken, PatientApp.getContext());

                    Log.d("PhrKeyReader", "The phr id is " + folderId);
                    Log.d("PhrKeyReader", "The phr token is " + phrToken);
                }
            } catch (JSONException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
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

    private String[] decrypt(String[] fileContent, KeyPair kp) {
        String[] data = new String[fileContent.length];
        try {
            for (int i = 0; i < fileContent.length; i++) {

                byte[] ciphertextBytes = Base64.decode(fileContent[i], Base64.NO_WRAP);

                byte[] partPlainBytes = Crypto.decrypt(kp, ciphertextBytes, Crypto.Scheme.PRE.AFGH.name, false);
                if (partPlainBytes == null) {
                    return null;
                }

                String partPlain = new String(partPlainBytes);
                Log.d("PhrKeyReader", String.format("  Received part %d: %s", i, partPlain));

                data[i] = partPlain;
            }
        } catch (Exception e) {
            Log.d("PhrKeyReader", "An Error occured while decrypting.");
            e.printStackTrace();

            return null;
        }
        return data;
    }
}