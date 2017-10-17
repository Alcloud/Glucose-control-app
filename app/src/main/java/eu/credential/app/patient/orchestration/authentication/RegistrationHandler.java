package eu.credential.app.patient.orchestration.authentication;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import eu.credential.app.patient.PatientApp;

/**
 * Created by tfl on 24.07.17.
 *
 * This class handles the registration tasks towards the CREDENTIAL Wallet.
 */

public class RegistrationHandler {

    protected static final Logger logger = Logger.getLogger(RegistrationHandler.class.getName());

    protected static final String host = "194.95.175.35";

    protected static final String openAmHost = "194.95.175.35";

    protected static final String registrationEndpointUrl = "http://"+openAmHost+":8079/piam/json/selfservice/userRegistration";

    /**
     * Creates a CREDENTIAL Account for the user with the given username and password
     * @param username
     * @param password

     */
    public RegistrationResponse createAccount(String username, String password) {
        // Use default endpoint urls. Change later for dynamic reading


        Map<String, String> tokens = getRegistrationTokens();

        String preregisterToken = preregisterAccount(tokens);

        RequestQueue queue = Volley.newRequestQueue(PatientApp.getAppContext());

        String postBody = "{\"input\":{\"user\":{\"username\":\"" + username + "\",\"userPassword\":\"" + password + "\"}},\"token\":\""+preregisterToken+"\"}";

        JSONObject jsonObject;
        try {

            jsonObject = new JSONObject(postBody);
        } catch (JSONException e) {
            Log.d("Register", "Could not build json request for contacting registration service");
            e.printStackTrace();
            return RegistrationResponseBuilder.createClientError();
        }



        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, registrationEndpointUrl+"?_action=submitRequirements", jsonObject, future, future){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("Content-Type", "application/json");
                return headerMap;
            }


        };

        queue.add(jsObjRequest);
        try {
            JSONObject response = future.get(10, TimeUnit.SECONDS);
            if (response.getJSONObject("status").getBoolean("success")) {
                return RegistrationResponseBuilder.createSuccessfulRegistrationResponse();
            } else {
                return RegistrationResponseBuilder.createClientError();
            }
        } catch (InterruptedException | ExecutionException e) {
            Log.d("Register", "Exception is "+e.getClass().toString());
            Log.d("Register", "Exception Root is "+e.getCause().getClass().toString());
            if (VolleyError.class.isAssignableFrom(e.getCause().getClass())) {
                VolleyError ve = (VolleyError) e.getCause();
                Log.d("Register","Error response with status code "+ve.networkResponse.statusCode);
                if (ve.networkResponse.statusCode == 404) {
                    return RegistrationResponseBuilder.createPasswordIncorectResponse();
                } else if (ve.networkResponse.statusCode == 409) {
                    return RegistrationResponseBuilder.createAccountAlreadyAvailableResponse();
                } else if(ve.networkResponse.statusCode == 500) {
                    return RegistrationResponseBuilder.createClientError();
                }

            }

            e.printStackTrace();

        }  catch (TimeoutException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            logger.info("Could not transform create Account request to JSON Object");
            e.printStackTrace();
            return RegistrationResponseBuilder.createJsonParsingError();
        }

        return RegistrationResponseBuilder.createClientError();
    }

    /**
     * Preregisters the account with the given tokens. It returns the token that can be used to register the account with password username.
     * @param tokens
     * @return
     */
    protected String preregisterAccount(Map<String, String> tokens) {
        RequestQueue queue = Volley.newRequestQueue(PatientApp.getAppContext());

        String body = "{\"input\": {\n" +
                "\"encryptedChallenge\":\""+tokens.get("challenge")+"\",\n" +
                "\"publicKey\":\"PUBLICKEY\",\n" +
                "\"recoveryKey\":\"RECOVERYKEY\"\n" +
                "},\n" +
                "\"token\": \n" +
                "\""+tokens.get("token")+"\"\n" +
                "}";

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(body);
        } catch (JSONException e) {
            Log.d("Register", "Could not build json request for contacting registration service");
            e.printStackTrace();
            return null;
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, registrationEndpointUrl+"?_action=submitRequirements", jsonObject,future, future){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("Content-Type", "application/json");
                return headerMap;
            }


        };

        queue.add(jsObjRequest);

        try {
            JSONObject response = future.get(10, TimeUnit.SECONDS);

            return response.getString("token");

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (VolleyError.class.isAssignableFrom(e.getCause().getClass())) {
                VolleyError ve = (VolleyError) e.getCause();
                if (ve.networkResponse.statusCode == 409) {
                    return null;
                }else if(ve.networkResponse.statusCode == 500) {
                    return null;
                }
            }
            e.printStackTrace();
        } catch (JSONException e) {
            logger.info("Could not transform create Account request to JSON Object");
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Calls the registration service with an initial attempt in order to retrieve a registration token that has to be used
     * during the registration process.
     * @return
     */
    private Map<String, String> getRegistrationTokens() {
        RequestQueue queue = Volley.newRequestQueue(PatientApp.getAppContext());

        String body = "{\"input\": {}}";

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(body);
        } catch (JSONException e) {
            Log.d("Register", "Could not build json request for contacting registration service");
            e.printStackTrace();
            return null;
        }

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, registrationEndpointUrl+"?_action=submitRequirements", jsonObject,future, future){

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headerMap = new HashMap<String, String>();
                headerMap.put("Content-Type", "application/json");
                return headerMap;
            }

        };

        queue.add(jsObjRequest);

        try {
            JSONObject response = future.get(10, TimeUnit.SECONDS);
            Map<String, String> tokens = new HashMap<String, String>();
            tokens.put("token", response.getString("token"));
            tokens.put("challenge", response.getJSONObject("requirements").getJSONObject("properties").getJSONObject("encryptedChallenge").getString("description"));
            return tokens;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (VolleyError.class.isAssignableFrom(e.getCause().getClass())) {
                VolleyError ve = (VolleyError) e.getCause();
                if (ve.networkResponse.statusCode == 409) {
                    return null;
                }else if(ve.networkResponse.statusCode == 500) {
                    return null;
                }
            }
            e.printStackTrace();
        } catch (JSONException e) {
            logger.info("Could not transform create Account request to JSON Object");
            e.printStackTrace();
        }

        return new HashMap<String, String>();
    }
}
