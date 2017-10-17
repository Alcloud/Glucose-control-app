package eu.credential.app.patient.orchestration.authentication;

/**
 * Created by tfl on 24.07.17.
 */

public class RegistrationResponseBuilder {

    public static RegistrationResponse createSuccessfulRegistrationResponse() {
        return new RegistrationResponse(RegistrationResponse.successfullRegistration);
    }

    public static RegistrationResponse createAccountAlreadyAvailableResponse() {
        return new RegistrationResponse(RegistrationResponse.accountAlreadyAvailable);
    }

    public static RegistrationResponse createPasswordIncorectResponse() {
        return new RegistrationResponse(RegistrationResponse.passwordIncorect);
    }

    public static RegistrationResponse createServiceNotReachableResponse() {
        return new RegistrationResponse(RegistrationResponse.serviceNotReachable);
    }

    public static RegistrationResponse createClientError() {
        return new RegistrationResponse(RegistrationResponse.clientError);
    }

    public static RegistrationResponse createJsonParsingError() {
        return new RegistrationResponse(RegistrationResponse.jsonParsingError);
    }

}
