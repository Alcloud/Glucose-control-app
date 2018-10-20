package eu.credential.app.patient.orchestration.collection;

import org.json.JSONException;

import java.text.ParseException;
import java.util.concurrent.ExecutionException;

public interface WithCollectorService {

    void setCollectorService(CollectorService collectorService) throws ParseException, InterruptedException, ExecutionException, JSONException;

    void refreshMessages();

    void refreshMeasurements() throws ParseException, InterruptedException, ExecutionException, JSONException;

    void listMessage(final String message);

    void displayConnectionStateChange(String deviceAddress, String deviceName, boolean hasConnected);
}
