package eu.credential.app.patient.helper;

/*
 * Created by Aleksei Piatkin on 19.03.18.
 */

public class Protocol {
    private String eventType;
    private String requestedEventType;
    private String eventIndicator;
    private String requestingUser;
    private String userId;
    private String trackingId;
    private String eventId;
    private String eventCreationTime;
    private String responseCode;

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public Protocol() {

    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getRequestedEventType() {
        return requestedEventType;
    }

    public void setRequestedEventType(String requestedEventType) {
        this.requestedEventType = requestedEventType;
    }

    public String getEventIndicator() {
        return eventIndicator;
    }

    public void setEventIndicator(String eventIndicator) {
        this.eventIndicator = eventIndicator;
    }

    public String getRequestingUser() {
        return requestingUser;
    }

    public void setRequestingUser(String requestingUser) {
        this.requestingUser = requestingUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventCreationTime() {
        return eventCreationTime;
    }

    public void setEventCreationTime(String eventCreationTime) {
        this.eventCreationTime = eventCreationTime;
    }
}
