package eu.credential.app.patient.ui.diary;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import eu.credential.app.patient.integration.model.DeviceInformation;
import eu.credential.app.patient.integration.model.GlucoseMeasurement;
import eu.credential.app.patient.integration.model.Measurement;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class DiaryDialogFragment extends DialogFragment implements WithCollectorService{
    // tag used for android logging
    private static final String TAG = DiaryFragment.class.getSimpleName();
    private LocalBroadcastManager localBroadcastManager;
    // name of the bluetooth device data has to received from

    // Logging list
    private List<String> logList;
    //private ListView logListView;
    //private ArrayAdapter<String> logListAdapter;

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;
    private SettingsBroadcastReceiver settingsBroadcastReceiver;

    // current state of the collected data
    private static Map<Integer, Measurement> measurementMap;
    private Map<String, DeviceInformation> deviceInformationMap;
    private int currentCounter;

    // Permission request constants
    public static final int PERMISSION_REQUEST_CODE_LOGIN_ACTIVIY_WRITE_UPDATE = 1;

    private XYPlot plot;
    private XYPlot plot2;
    private XYSeries series1;
    private XYSeries series2;
    private XYSeries goal;
    private static final int DOMAIN_BOUNDARY_SIZE = 8;
    private static final int GOAL_SIZE = 113;
    public DiaryDialogFragment() {
        super();

        // services
        this.collectorService = null;
        this.measurementMap = Collections.synchronizedMap(new TreeMap<Integer, Measurement>());
        this.currentCounter = 0;
        this.deviceInformationMap = Collections.synchronizedMap(new HashMap<String, DeviceInformation>());
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
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_diary, (ViewGroup) this.getView());
        builder.setTitle(R.string.diary_toolbar_name).setPositiveButton("Back", (dialog, id) -> dialog.cancel()).setView(view);

        // initialize our XYPlot reference:
        plot = (XYPlot) view.findViewById(R.id.plot1);
        plot2 = (XYPlot) view.findViewById(R.id.plot2);
        plot.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), BloodZoomActivity.class);
            startActivity(intent);
        });

        this.localBroadcastManager = LocalBroadcastManager.getInstance(this.getActivity());

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this.getActivity(), CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        getActivity().bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // create controls and bind onclick handlers
        //initLogView();

        // Load the preferences
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        return builder.create();
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
        if(this.collectorService != null) {
            refreshMeasurements();
            //refreshDeviceInformation();
            //refreshMessages();
            plot.clear();
            plot2.clear();
            graph();
            plot.redraw();
            plot2.redraw();
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
    }

    @Override
    public void listMessage(final String message) {
        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logListAdapter.insert(message, 0);
            }
        });
        Log.d(TAG, "Length of log list: " + logList.size());*/
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
            //refreshMessages();
        } else {
            //listMessage("CollectorService service unbound.");
        }
    }
    /**
     * Triggers data update, so that the collector data from the collector service is grepped.
     */
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
        // show the new data
        //showMeasurements();
    }
    @Override
    public void refreshMessages() {
        /*Queue<String> queue = collectorService.getMessageQueue();
        while (queue.peek() != null) {
            listMessage(queue.poll());
        }*/
    }

    /**
     * This checks whether the permissions we need to run this activity are granted at this particular time. We need to
     * remember, that the user can revoke the permission at their leisure.
     * @return Whether or not the permissions are already granted
     */
    /*private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getContext().getApplicationContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new android.app.AlertDialog.Builder(this.getActivity()).setTitle("Permissions for update").setMessage("We need the external " +
                    "storage permission in order to download the new app.")
                    .setNeutralButton("OK", null).show();
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE_LOGIN_ACTIVIY_WRITE_UPDATE);
        } else {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE_LOGIN_ACTIVIY_WRITE_UPDATE);
        }
    }*/
    @Override
    public void displayConnectionStateChange(String deviceAddress, String deviceName, boolean hasConnected) {
        String statusText = hasConnected ? "connected" : "disconnected";
        Toast.makeText(
                getActivity().getApplicationContext(),
                deviceName + " " + statusText,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_LOGIN_ACTIVIY_WRITE_UPDATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //checkForUpdates();
                }
                break;
        }
    }
    protected static ArrayList series(){
        ArrayList series = new ArrayList();
        GlucoseMeasurement glucoseValue;
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if(meas instanceof GlucoseMeasurement){
                glucoseValue = (GlucoseMeasurement) meas;
                series.add(Math.round(glucoseValue.getGlucoseConcentration()*100000));
            }
        }
        return series;
    }

    protected static ArrayList domainLabels (){
        ArrayList <Date> domainLabels = new ArrayList();
        GlucoseMeasurement glucoseValue;
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if(meas instanceof GlucoseMeasurement){
                glucoseValue = (GlucoseMeasurement) meas;
                domainLabels.add(glucoseValue.getBaseTime());
            }
        }
        return domainLabels;
    }
    //TODO: change goal
    //hardcode for goal
    protected static ArrayList goalArray (){
        ArrayList goalList = new ArrayList();
        for (Integer key : measurementMap.keySet()) {
            Measurement meas = measurementMap.get(key);
            if(meas instanceof GlucoseMeasurement){
                goalList.add(GOAL_SIZE);
            }
        }
        return goalList;
    }
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
    private void isAnyValueExist (XYPlot plot, ArrayList list) {
        if (list.size()<1){
            plot.setDomainBoundaries(0, 2, BoundaryMode.FIXED);
            Toast.makeText(getActivity().getApplicationContext(), R.string.toast_zoom,Toast.LENGTH_LONG).show();
            plot.redraw();
        }
        else if (list.size()==1){
            plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
            plot.setDomainBoundaries(0, 1, BoundaryMode.FIXED);
            plot.setDomainStep(StepMode.INCREMENT_BY_VAL, DOMAIN_BOUNDARY_SIZE);
            plot.redraw();
        }
        else {
            plot.setDomainBoundaries(list.size()-DOMAIN_BOUNDARY_SIZE, BoundaryMode.FIXED, list.size(), BoundaryMode.AUTO);
            plot.setDomainStep(StepMode.SUBDIVIDE, DOMAIN_BOUNDARY_SIZE);
            plot.redraw();
        }
    }
    private void graph() {
        plot.getLegend().setVisible(false);
        plot2.getLegend().setVisible(false);
        plot2.getGraph().setMargins(72,50,20,78);
        plot.getGraph().setMargins(72,50,20,78);

        // turn the above arrays into XYSeries':
        // (Y_VALS_ONLY means use the element index as the x value)
        series1 = new SimpleXYSeries(series(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Weight");
        series2 = new SimpleXYSeries(series("steps.json"), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Steps");
        goal = new SimpleXYSeries(goalArray(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "goal");

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(this.getActivity(), R.xml.line_point_formatter_with_labels2);
        LineAndPointFormatter series2Format = new LineAndPointFormatter(this.getActivity(), R.xml.line_point_formatter_with_labels);
        LineAndPointFormatter goalFormat = new LineAndPointFormatter(this.getActivity(), R.xml.goal_line);

        // add some smoothing to the lines:
        series1Format.setInterpolationParams(new CatmullRomInterpolator.Params(10, CatmullRomInterpolator.Type.Centripetal));

        // add a new series' to the xyplot:
        plot.addSeries(series1, series1Format);
        plot.addSeries(goal, goalFormat);
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
}
