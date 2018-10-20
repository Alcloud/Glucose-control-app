package eu.credential.app.patient.ui.vitals;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplot.util.SeriesUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.CatmullRomInterpolator;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.orchestration.collection.CollectorBroadcastReceiver;
import eu.credential.app.patient.orchestration.collection.CollectorService;
import eu.credential.app.patient.orchestration.collection.CollectorServiceConnection;
import eu.credential.app.patient.orchestration.collection.WithCollectorService;
import eu.credential.app.patient.orchestration.http.PHRdata;
import eu.credential.app.patient.integration.upload.UploadBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Created by Aleksei Piatkin on 02.12.17.
 * <p>
 * A graph screen that shows weight and step counter values.
 */
public class VitalsFragment extends Fragment implements WithCollectorService {

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;
    private LocalBroadcastManager localBroadcastManager;

    // Permission request constants
    private XYPlot plot;
    private static final int DOMAIN_BOUNDARY_SIZE = 8;

    protected static int getDomainBoundarySize() {
        return DOMAIN_BOUNDARY_SIZE;
    }

    public VitalsFragment() {
        super();
        this.collectorService = null;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_refresh:
                try {
                    refreshMeasurements();
                } catch (InterruptedException | ExecutionException | JSONException | ParseException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            refreshMeasurements();
        } catch (InterruptedException | ParseException | JSONException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        this.localBroadcastManager = LocalBroadcastManager.getInstance(Objects.requireNonNull(getActivity()));

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this.getActivity(), CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        getActivity().bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vitals, container, false);
        // initialize our XYPlot reference:
        plot = v.findViewById(R.id.plot1);

        plot.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), WeightZoomActivity.class);
            startActivity(intent);
        });
        this.localBroadcastManager = LocalBroadcastManager.getInstance(Objects.requireNonNull(this.getActivity()));

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this.getActivity(), CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        getActivity().bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        // register tbe broadcast listener, to get collector events
        this.collectorBroadcastReceiver = new CollectorBroadcastReceiver(this);
        IntentFilter collectorActions = collectorBroadcastReceiver.getIntentFilter();
        localBroadcastManager.registerReceiver(collectorBroadcastReceiver, collectorActions);

        // register broadcast listener for upload events
        this.uploadBroadcastReceiver = new UploadBroadcastReceiver(this);
        IntentFilter uploadActions = uploadBroadcastReceiver.getIntentFilter();
        localBroadcastManager.registerReceiver(uploadBroadcastReceiver, uploadActions);

        // on first execution the collector service will not be bound
        if (this.collectorService != null) {
            try {
                refreshMeasurements();
            } catch (InterruptedException | ExecutionException | JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {

        localBroadcastManager.unregisterReceiver(collectorBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(uploadBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Objects.requireNonNull(getActivity()).unbindService(this.collectorServiceConnection);
    }

    /**
     * registers the data collectorService service at the activity after connection.
     * Gets executed on creation of the service connection.
     */
    @Override
    public void setCollectorService(CollectorService collectorService) {
        this.collectorService = collectorService;
    }

    /**
     * Polls all new messages from the collector service.
     */
    @Override
    public void refreshMessages() {
        /*Queue<String> queue = collectorService.getMessageQueue();
        while (queue.peek() != null) {
            listMessage(queue.poll());
        }*/
    }

    /**
     * Triggers data update, so that the collector data from the collector service is grepped.
     */
    @Override
    public void displayConnectionStateChange(String deviceAddress, String deviceName, boolean hasConnected) {
        String statusText = hasConnected ? "connected" : "disconnected";
        Toast.makeText(
                Objects.requireNonNull(getActivity()).getApplicationContext(),
                deviceName + " " + statusText,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshMeasurements() throws InterruptedException, ExecutionException, JSONException, ParseException {
        plot.clear();
        setGraph();
        plot.redraw();
    }

    static class AddWeightMeasurment {
        // X/Y values
        private static final ArrayList<Double> series = new ArrayList<>();
        private static final ArrayList<Date> domainLabels = new ArrayList<>();

        static ArrayList<Double> getSeries() {
            return series;
        }

        static ArrayList<Date> getDomainLabels() {
            return domainLabels;
        }

        private static void addWeightMeasurmentValue(Context context) throws ExecutionException, InterruptedException, JSONException, ParseException {
            DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
            JSONArray phrDocumentArray = new PHRdata("weightId", "retrieveDocuments", null)
                    .execute().get();
            if (phrDocumentArray.getJSONObject(0).has("error")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(context));
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("Warning!");
                builder.setMessage(context.getString(R.string.server_not_respond));
                builder.setPositiveButton(context.getString(R.string.ok),
                        (arg0, arg1) -> {
                        });
                builder.show();
            } else {
                series.clear();
                domainLabels.clear();
                for (int i = 0; i < phrDocumentArray.length(); i++) {
                    series.add(Double.parseDouble(phrDocumentArray.getJSONObject(i)
                            .getJSONObject("properties").getJSONObject("weight").getString("value")));
                    domainLabels.add(df.parse(phrDocumentArray.getJSONObject(i).getJSONObject("properties")
                            .getJSONObject("receiveTime").getString("value")));
                }
            }
        }
    }

    @Override
    public void listMessage(final String message) {
    }

    private void setBoundaries(XYPlot plot, ArrayList<Date> list, Double value) {
        if (list.size() < 1) {
            plot.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),
                    R.string.toast_zoom, Toast.LENGTH_LONG).show();
            plot.redraw();
        } else if (list.size() == 1) {
            plot.setRangeBoundaries(value - 4, value + 4, BoundaryMode.FIXED);
            plot.setRangeStep(StepMode.SUBDIVIDE, DOMAIN_BOUNDARY_SIZE);
            plot.setDomainBoundaries(0, 1, BoundaryMode.FIXED);
            plot.setDomainStep(StepMode.SUBDIVIDE, 1);
            plot.centerOnDomainOrigin(0, 1, BoundaryMode.FIXED);
            plot.redraw();
        } else if (list.size() < DOMAIN_BOUNDARY_SIZE) {
            plot.setDomainBoundaries(0, BoundaryMode.FIXED, list.size(), BoundaryMode.AUTO);
            //plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 5);
            plot.setDomainStep(StepMode.SUBDIVIDE, list.size());
            plot.redraw();
        } else {
            plot.setDomainBoundaries(list.size() - DOMAIN_BOUNDARY_SIZE, BoundaryMode.FIXED, list.size(), BoundaryMode.AUTO);
            plot.setDomainStep(StepMode.SUBDIVIDE, DOMAIN_BOUNDARY_SIZE);
            plot.redraw();
        }
    }

    private void setGraph() throws InterruptedException, ExecutionException, ParseException, JSONException {
        plot.getLegend().setVisible(false);
        AddWeightMeasurment.addWeightMeasurmentValue(getContext());
        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        XYSeries series1 = new SimpleXYSeries(AddWeightMeasurment.getSeries(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(this.getActivity(), R.xml.line_point_formatter_with_labels2);

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.US);

            @Override
            public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
                int i = Math.round(((Number) obj).longValue());
                return dateFormat.format(AddWeightMeasurment.getDomainLabels().get(i), toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, @NonNull ParsePosition pos) {
                return null;
            }
        });

        if (SeriesUtils.minMax(series1).getMaxY() != null) {
            plot.setRangeUpperBoundary(SeriesUtils.minMax(series1).getMaxY().doubleValue() + 0.5, BoundaryMode.FIXED);
            setBoundaries(plot, AddWeightMeasurment.getDomainLabels(), AddWeightMeasurment.getSeries().get(0));
        } else {
            Toast.makeText(getContext(), R.string.toast_zoom, Toast.LENGTH_SHORT).show();
        }
    }
}