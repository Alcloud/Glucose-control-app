package eu.credential.app.patient.orchestration.collection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import org.json.JSONException;

import java.text.ParseException;
import java.util.concurrent.ExecutionException;


/**
 * Specialized service connection, which is drawn between the user interface and the service wo
 * collects data from several devices
 */
public class CollectorServiceConnection implements ServiceConnection {

    // the parental service consumer
    private WithCollectorService withCollectorService;

    public CollectorServiceConnection(WithCollectorService withCollectorService) {
        this.withCollectorService = withCollectorService;
    }
    /*public CollectorServiceConnection(DiaryFragment diaryFragment) {
        this.diaryFragment = diaryFragment;
    }*/

    /**
     * Passes the new data collector service instance to the parent consumer.
     * @param componentName
     * @param service
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        CollectorService collectorService = ((CollectorService.LocalBinder) service).getService();
        try {
            withCollectorService.setCollectorService(collectorService);
        } catch (InterruptedException | JSONException | ExecutionException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        try {
            withCollectorService.setCollectorService(null);
        } catch (ParseException | JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
