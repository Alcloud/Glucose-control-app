package eu.credential.app.patient.ui.vitals;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BoundaryMode;
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
import eu.credential.app.patient.integration.model.Measurement;
import eu.credential.app.patient.integration.model.WeightMeasurement;
import eu.credential.app.patient.ui.settings.SettingsBroadcastReceiver;
import eu.credential.app.patient.integration.upload.UploadBroadcastReceiver;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class VitalsFragment extends Fragment implements WithCollectorService {
    // tag used for android logging
    private static final String TAG = VitalsFragment.class.getSimpleName();
    private LocalBroadcastManager localBroadcastManager;

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;
    private SettingsBroadcastReceiver settingsBroadcastReceiver;

    // current state of the collected data
    private static Map<Integer, Measurement> measurementMap;
    private int currentCounter;

    // Permission request constants
    private XYPlot plot;
    private XYPlot plot2;
    private static final int DOMAIN_BOUNDARY_SIZE = 8;

    private XYSeries series1;
    private XYSeries series2;
    private LineAndPointFormatter series1Format;
    private BarFormatter series2Format;

    public static int getDomainBoundarySize() {
        return DOMAIN_BOUNDARY_SIZE;
    }

    public VitalsFragment() {
        super();

        // services
        this.collectorService = null;
        this.measurementMap = Collections.synchronizedMap(new TreeMap<Integer, Measurement>());
        this.currentCounter = 0;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_camera);
        item.setVisible(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshMeasurements();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        this.localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this.getActivity(), CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        getActivity().bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vitals, container, false);
        // initialize our XYPlot reference:
        plot = (XYPlot) v.findViewById(R.id.plot1);
        plot2 = (XYPlot) v.findViewById(R.id.plot2);

        plot.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), WeightZoomActivity.class);
            startActivity(intent);
        });
        this.localBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());

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

        // register broadcast listener for settings messages
        this.settingsBroadcastReceiver = new SettingsBroadcastReceiver(this);
        IntentFilter settingsActions = settingsBroadcastReceiver.getIntentFilter();
        localBroadcastManager.registerReceiver(settingsBroadcastReceiver, settingsActions);

        // on first execution the collector service will not be bound
        if (this.collectorService != null) {
            refreshMeasurements();
            save();
        }
    }

    @Override
    public void onPause() {

        localBroadcastManager.unregisterReceiver(collectorBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(uploadBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(settingsBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(this.collectorServiceConnection);
        save();
    }

    /**
     * registers the data collectorService service at the activity after connection.
     * Gets executed on creation of the service connection.
     *
     * @param collectorService
     */
    @Override
    public void setCollectorService(CollectorService collectorService) {
        this.collectorService = collectorService;
        if (this.collectorService != null) {
            //listMessage("CollectorService service successfully bound.");
            refreshMeasurements();
        }
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
                getActivity().getApplicationContext(),
                deviceName + " " + statusText,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshMeasurements() {
        // Check connection
        if (this.collectorService == null) {
            Log.w(TAG, "Could not grep data from collector service, because it is still not connected.");
            return;
        }

        // Check revision
        if (collectorService.getDataCount() == this.currentCounter) {
            Log.d(TAG, "The current state \"" + this.currentCounter + "\" is up-to-date." +
                    "No data will be grepped.");
            return;
        }
        // When the versions differ receive the new data.
        Map<Integer, Measurement> there = collectorService.getMeasurementMap();
        Map<Integer, Measurement> here = this.measurementMap;

        // Search for new entries and collect them
        for (Integer id : there.keySet()) {
            if (!here.containsKey(id)) {
                Measurement newMeasurement = there.get(id);
                here.put(id, newMeasurement);
            }
        }

        // update the current revision
        this.currentCounter = collectorService.getDataCount();

        // show new measurements
        plot.clear();
        plot2.clear();
        graphVitals();
        plot.redraw();
        plot2.redraw();
    }

    @Override
    public void listMessage(final String message) {
    }

    protected static ArrayList series() {
        ArrayList series = new ArrayList();
        WeightMeasurement weightValue;
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if (meas instanceof WeightMeasurement) {
                weightValue = (WeightMeasurement) meas;
                series.add(weightValue.getWeight());
            }
        }
        return series;
    }

    protected void save() {
        WeightMeasurement weightValue;
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if (meas instanceof WeightMeasurement) {
                weightValue = (WeightMeasurement) meas;
                weightValue.writeJSON(getActivity().getApplicationContext());
            }
        }
    }

    protected static ArrayList domainLabels() {
        ArrayList<Date> domainLabels = new ArrayList();
        WeightMeasurement weightValue;
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if (meas instanceof WeightMeasurement) {
                weightValue = (WeightMeasurement) meas;
                domainLabels.add(weightValue.getBaseTime());
            }
        }
        return domainLabels;
    }

    /*protected ArrayList seriesRead() {
        ArrayList series = new ArrayList();
        String path = getActivity().getFilesDir() + "/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        double weight;
        JSONObject jsonObject;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()){
            try {
                jsonObject = new JSONObject(readFileToString(getActivity().getFilesDir() + "/" + listOfFiles[i].getName()+".json"));
                weight = jsonObject.getDouble("weight");
                series.add(weight);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }
        return series;
    }*/

    /*private ArrayList domainLabelsRead() {
        ArrayList<Date> domain = new ArrayList();
        String path = getActivity().getFilesDir() + "/";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        Date date;
        JSONObject jsonObject;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()){
            try {
                jsonObject = new JSONObject(readFileToString(getActivity().getFilesDir() + "/" + listOfFiles[i].getName()+".json"));
                date = new Date(jsonObject.getLong("receiveTime"));
                domain.add(date);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        }
        return domain;
    }*/

    private void isAnyValueExist(XYPlot plot, ArrayList list) {
        if (list.size() < 1) {
            plot.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_zoom, Toast.LENGTH_LONG).show();
            plot.redraw();
        } else if (list.size() == 1) {
            plot.setRangeBoundaries(30, 150, BoundaryMode.FIXED);
            plot.setRangeStep(StepMode.SUBDIVIDE, 5);
            plot.setDomainBoundaries(0, 1, BoundaryMode.FIXED);
            plot.setDomainStep(StepMode.SUBDIVIDE, 1);
            plot.centerOnDomainOrigin(0, 1, BoundaryMode.FIXED);
            plot.redraw();
        } else if (list.size() > 1 && list.size() < DOMAIN_BOUNDARY_SIZE) {
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

    private void graphVitals() {
        plot.getLegend().setVisible(false);
        plot2.getLegend().setVisible(false);

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        series1 = new SimpleXYSeries(series(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");
        series2 = new SimpleXYSeries(series("steps.json"), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Steps");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        series1Format = new LineAndPointFormatter(this.getActivity(), R.xml.line_point_formatter_with_labels2);
        series2Format = new BarFormatter(Color.argb(255, 0, 151, 128), Color.argb(255, 0, 151, 128));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot2.addSeries(series2, series2Format);

        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).longValue());
                return dateFormat.format(domainLabels().get(i), toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        plot2.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
            private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.US);

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                int i = Math.round(((Number) obj).longValue());
                return dateFormat.format(domainLabels("steps.json").get(i), toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });

        isAnyValueExist(plot, domainLabels());
        isAnyValueExist(plot2, domainLabels("steps.json"));
    }

    /* private String readFileToString(String path) {

        StringBuilder content = new StringBuilder();
        try {
            FileInputStream input = new FileInputStream(
                    new File(path));
            Scanner scanner = new Scanner(input);

            while (scanner.hasNextLine()) content.append(scanner.nextLine());

        } catch (IOException e) {
            Log.e("VitalsFragment", "Could not read file from JSON.");
            e.printStackTrace();
        }
        return content.toString();
    }*/
//temporary series for HBA1c Plot
    private ArrayList series(String fileName) {
        ArrayList series = new ArrayList();
        AssetManager am = getActivity().getAssets();
        try {
            InputStream is = am.open(fileName);
            String json = readStream(is);
            is.close();
            JSONObject obj = (JSONObject)new JSONTokener(json).nextValue();
            JSONArray arr = obj.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONArray point = arr.getJSONArray(i);
                double val = point.getDouble(1);
                series.add(val);
            }
        } catch (Exception e) {
            Log.d("graph",e.getMessage());
        }
        return series;
    }

    private ArrayList domainLabels(String fileName) {
        ArrayList <Date> domainLabels = new ArrayList();
        AssetManager am = getActivity().getAssets();
        try {
            InputStream is = am.open(fileName);
            String json = readStream(is);
            is.close();
            JSONObject obj = (JSONObject)new JSONTokener(json).nextValue();
            JSONArray arr = obj.getJSONArray("data");
            for (int i = 0; i < arr.length(); i++) {
                JSONArray point = arr.getJSONArray(i);
                Date d = new Date(point.getLong(0));
                domainLabels.add(d);
            }
        } catch (Exception e) {
            Log.d("graph",e.getMessage());
        }
        return domainLabels;
    }

    private String readStream(InputStream is) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n;
        try {
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            Log.d("graph",e.getMessage());
            return null;
        }
        return output.toString();
    }
}