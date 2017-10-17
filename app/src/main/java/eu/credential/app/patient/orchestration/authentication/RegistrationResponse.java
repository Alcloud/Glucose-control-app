package eu.credential.app.patient.orchestration.authentication;

/**
 * Created by tfl on 24.07.17.
 */

public class RegistrationResponse {

    public String status;

    public static final String successfullRegistration = "Successfull Registration";
    public static final String accountAlreadyAvailable = "Account already available";
    public static final String passwordIncorect = "Password Incorect";
    public static final String serviceNotReachable = "Service Not Reachable";
    public static final String clientError = "Client Error";
    public static final String jsonParsingError = "JSON Parsing Error";

    /**
     * Registration Response constructor. Please use the RegistrationResponseBuilder to create a registration response.
     * @param response
     */
    protected RegistrationResponse(String response) {
        this.status = response;
    }
}
