package eu.credential.app.patient.orchestration.collection;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.credential.app.patient.integration.bluetooth.BleBroadcastReceiver;
import eu.credential.app.patient.integration.bluetooth.BleService;
import eu.credential.app.patient.integration.bluetooth.BleServiceConnection;
import eu.credential.app.patient.integration.model.DeviceInformation;
import eu.credential.app.patient.integration.model.GlucoseMeasurement;
import eu.credential.app.patient.integration.model.Measurement;
import eu.credential.app.patient.integration.model.WeightMeasurement;
import eu.credential.app.patient.orchestration.http.PHRdata;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Collects data from wireless health devices in order to hold them for the activity.
 */
public class CollectorService extends Service {

    // actions, that will be broadcasted
    public final static String ACTION_MEASUREMENT_COLLECTED =
            "CollectorService.ACTION_MEASUREMENT_COLLECTED";
    public final static String ACTION_DEVICE_INFO_COLLECTED =
            "CollectorService.ACTION_DEVICE_INFO_COLLECTED";
    public final static String NEW_MESSAGES =
            "CollectorService.NEW_MESSAGES";
    public final static String DEVICE_ADDR =
            "CollectorService.DEVICE_ADDR";
    public final static String COLLECTOR_STOPPED =
            "CollectorService.COLLECTOR_STOPPED";
    public final static String ACTION_DEVICE_CONNECTED =
            "CollectorService.ACTION_DEVICE_CONNECTED";
    public final static String ACTION_DEVICE_DISCONNECTED =
            "CollectorService.ACTION_DEVICE_DISCONNECTED";

    public enum Type {GLUCOSE_COLLECTION, WEIGHT_COLLECTION}

    // name of device address field
    public final static String DEVICE_ADDRESS = "CollectorService.DEVICE_ADDRESS";
    // name of device name field
    public final static String DEVICE_NAME = "CollectorService.DEVICE_NAME";

    // messages container which can be externally accessed by a user interface
    private Queue<String> messageQueue;

    // logging indicator
    private final static String TAG = CollectorService.class.getSimpleName();

    // performance indicator
    private static final String TAG_PERF = "Performance";

    // BroadcastManager
    private LocalBroadcastManager localBroadcastManager;

    // message container where all received health data is stored
    private Map<Integer, Measurement> measurementMap;
    private int counter;
    private Map<String, DeviceInformation> deviceInformationMap;

    // broadcast receiver for incoming device data
    private BleBroadcastReceiver bleBroadcastReceiver;

    // service connection stuff
    private BleServiceConnection bleServiceConnection;
    private BleService bleService;

    // List of collection handlers, which manage device specific collection processes
    private Map<String, CollectionHandler> collectionHandlers;

    // Listener for getting information about changed devices to listen
    private CollectorServicePreferenceListener preferenceListener;
    private SharedPreferences preferences;

    /**
     * Default constructor
     */
    public CollectorService() {

        // initialize the data collection
        this.measurementMap = Collections.synchronizedMap(new TreeMap<Integer, Measurement>());
        this.counter = 0;
        this.bleService = null;
        this.collectionHandlers = Collections.synchronizedMap(new HashMap<String, CollectionHandler>());
        this.deviceInformationMap = Collections.synchronizedMap(new HashMap<String, DeviceInformation>());

        // create the broadcast receiver (needs to get registered in onCreate)
        this.bleBroadcastReceiver = new BleBroadcastReceiver(this);

        // create the ble service connection (this only holds callbacks)
        this.bleServiceConnection = new BleServiceConnection(this);

        // Instantiate the message queue
        this.messageQueue = new LinkedBlockingQueue<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        Log.i(TAG, "Collector Service successfully started.");
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        // Register the broadcast-receiver
        this.localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = bleBroadcastReceiver.getIntentFilter();
        localBroadcastManager.registerReceiver(this.bleBroadcastReceiver, filter);

        // Register the BLE service and start it
        Intent bleServiceIntent = new Intent(this, BleService.class);
        bindService(bleServiceIntent, bleServiceConnection, BIND_AUTO_CREATE);

        // Listen for change of settings
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.preferenceListener = new CollectorServicePreferenceListener(this);
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
        // preference listener does not get triggered on startup, but we do this later,
        // when the ble service has been bound
    }

    @Override
    public void onDestroy() {
        // Unregister the broadcast receiver
        this.localBroadcastManager.unregisterReceiver(this.bleBroadcastReceiver);

        // Unregister the ble service
        unbindService(bleServiceConnection);

        // Unregister preference listener
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener);
    }

    /**
     * Gives information about what count the data collection has currently reached.
     * But does not represent what is the actual size of the data collection.
     *
     * @return
     */
    public int getDataCount() {
        return this.counter;
    }

    /**
     * Returns the map of the currently collected measurements with their integer ids.
     *
     * @return
     */
    public Map<Integer, Measurement> getMeasurementMap() {
        return this.measurementMap;
    }

    /**
     * Returns the map of the currently collected deviceInformations with their device addresses.
     *
     * @return
     */
    public Map<String, DeviceInformation> getDeviceInformationMap() {
        return this.deviceInformationMap;
    }

    /**
     * Creates a simple broadcast message without further context.
     */
    private void broadcastMeasurementCollected() {
        Intent intent = new Intent(ACTION_MEASUREMENT_COLLECTED);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Creates a simple broadcast message without further context.
     */
    private void broadcastDeviceInformationCollected() {
        Intent intent = new Intent(ACTION_DEVICE_INFO_COLLECTED);
        localBroadcastManager.sendBroadcast(intent);
    }


    public void broadcastConnectionLost(String deviceAddress, String deviceName) {
        broadcastConnectionChange(deviceAddress, deviceName, false);
    }

    public void broadcastConnectionEstablished(String deviceAddress, String deviceName) {
        broadcastConnectionChange(deviceAddress, deviceName, true);
    }

    private void broadcastConnectionChange(String deviceAddress, String deviceName, boolean hasConnected) {
        String type = hasConnected ? ACTION_DEVICE_CONNECTED : ACTION_DEVICE_DISCONNECTED;
        Intent intent = new Intent(type);

        intent.putExtra(DEVICE_NAME, deviceName);
        intent.putExtra(DEVICE_ADDRESS, deviceAddress);

        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Creates a broadcast message, that informs its receiver about a new device event.
     *
     * @param message
     */
    public void broadcastDeviceMessage(String message) {
        // Queue the message
        this.messageQueue.add(message);
        // Inform environment
        Intent intent = new Intent(NEW_MESSAGES);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * Function to store new measurements by devices
     *
     * @param measurement
     */
    public void receiveMeasurement(Measurement measurement, String deviceAddress) {
        Log.d(TAG, "Received measurement from " + deviceAddress + ": \"" + measurement.toString() + "\"");
        // Take first the counter and then increment
        Integer id = this.counter;
        measurementMap.put(id, measurement);
        // set new measurement (JSON array element) and save it to PHR document
        if (measurement instanceof GlucoseMeasurement) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(((GlucoseMeasurement) measurement).getBaseTime());
            // TODO: Change the logic to get only one (last) glucose value.
            if (cal.get(Calendar.MINUTE) <= 57) {
                cal.add(Calendar.MINUTE, 2);
            }
            if (cal.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) &&
                    cal.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH) &&
                    cal.get(Calendar.DAY_OF_MONTH) == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                    cal.get(Calendar.HOUR_OF_DAY) == Calendar.getInstance().get(Calendar.HOUR_OF_DAY) &&
                    cal.get(Calendar.MINUTE) > Calendar.getInstance().get(Calendar.MINUTE)) {
                saveMeasurementToPHR(setGlucoseInPHRDocument(
                        String.valueOf(((GlucoseMeasurement) measurement).getSequenceNumber()),
                        String.valueOf(Math.round(((GlucoseMeasurement) measurement).getGlucoseConcentration() * 100000)),
                        String.valueOf(((GlucoseMeasurement) measurement).getUnit()),
                        String.valueOf(((GlucoseMeasurement) measurement).getType()),
                        String.valueOf(((GlucoseMeasurement) measurement).getSampleLocation()),
                        String.valueOf(((GlucoseMeasurement) measurement).getSensorStatus()),
                        String.valueOf(((GlucoseMeasurement) measurement).getBaseTime()),
                        String.valueOf(measurement.getReceiveTime())), "glucoseId");
            }
        }
        if (measurement instanceof WeightMeasurement) {
            saveMeasurementToPHR(setWeightInPHRDocument(
                    String.valueOf(((WeightMeasurement) measurement).getWeight()),
                    String.valueOf(((WeightMeasurement) measurement).getHeight()),
                    String.valueOf(((WeightMeasurement) measurement).getBmi()),
                    String.valueOf(((WeightMeasurement) measurement).getWeightUnit()),
                    String.valueOf(((WeightMeasurement) measurement).getHeightUnit()),
                    String.valueOf(((WeightMeasurement) measurement).getBaseTime()),
                    String.valueOf(measurement.getReceiveTime())), "weightId");
        }
        this.counter++;
        // broadcast the new status update
        broadcastMeasurementCollected();
    }

    /**
     * Function to store new information by devices
     *
     * @param deviceInformation
     */
    public void receiveDeviceInformation(DeviceInformation deviceInformation, String deviceAdress) {
        Log.d(TAG, "Received deviceInformation from " + deviceAdress + ": \"" + deviceInformation.toString() + "\"");

        deviceInformationMap.put(deviceAdress, deviceInformation);
        // broadcast the new status update
        broadcastDeviceInformationCollected();
    }

    /**
     * Registers the given BleService object at the data collector. this service has to be
     * initialized.
     *
     * @param bleService can be null
     */
    public void setBleService(BleService bleService) {
        // accepts nulls
        if (bleService == null) {
            this.bleService = null;
            return;
        }

        // take only initialized services
        if (bleService.isProperlyInitialized()) {
            this.bleService = bleService;
            // do a collector startup
            this.preferenceListener.trigger(this.preferences);
        } else {
            this.bleService = null;
            Log.e(TAG, "BLE service not correctly initialized.");
        }
    }

    /**
     * Returns the current Ble service
     *
     * @return null, if no ble service registered
     */
    public BleService getBleService() {
        return this.bleService;
    }

    public Queue<String> getMessageQueue() {
        return messageQueue;
    }

    /**
     * The binder allows to access the services methods from another class.
     */
    public class LocalBinder extends Binder {
        public CollectorService getService() {
            return CollectorService.this;
        }
    }

    private final IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service has been bound.");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Unbinding procedure started.");
        return super.onUnbind(intent);
    }

    /**
     * Initializes a new connection handler and starts it (if not already running). Other
     * collection handlers with same address but different type will be stopped.
     *
     * @param deviceAddress
     * @param wantedType
     */
    public void startCollection(String deviceAddress, Type wantedType) {
        // Start the collection handler
        CollectionHandler handler;
        switch (wantedType) {
            case WEIGHT_COLLECTION:
                handler = new WeightHandler(deviceAddress, this.bleService, this);
                break;
            case GLUCOSE_COLLECTION:
                handler = new GlucoseHandler(deviceAddress, this.bleService, this);
                break;
            default:
                handler = null;
                break;
        }

        if (handler != null) {
            Type currentType = getCollectionState(deviceAddress);

            if (currentType != wantedType) {
                if (currentType != null) stopCollection(deviceAddress);
                // start normally
                this.collectionHandlers.put(deviceAddress, handler);
                handler.start();
            }

        }
    }

    @Deprecated
    public void startGlucoseCollection(String deviceAddress) {
        startCollection(deviceAddress, Type.GLUCOSE_COLLECTION);
    }

    @Deprecated
    public void startWeightCollection(String deviceAddress) {
        startCollection(deviceAddress, Type.WEIGHT_COLLECTION);
    }

    /**
     * Returns a Map with all currently collected device addresses and their collection type.
     *
     * @return
     */
    public Map<String, Type> getCollectionSituation() {
        Map<String, Type> result = new HashMap<>();
        for (CollectionHandler handler : this.collectionHandlers.values()) {
            Type state = handler instanceof GlucoseHandler ?
                    Type.GLUCOSE_COLLECTION : Type.WEIGHT_COLLECTION;
            result.put(handler.getDeviceAddress(), state);
        }
        return result;
    }

    /**
     * Stops the collection handler for the given device.
     */
    public void stopCollection(String deviceAddress) {
        Log.d(TAG, "Sending stop signal to collection handler " + deviceAddress);
        CollectionHandler handler = this.collectionHandlers.get(deviceAddress);
        if (handler != null) handler.stop();
        unregisterCollector(deviceAddress);
    }

    /**
     * Gives information whether there is a collector listening on the given device address.
     *
     * @param deviceAddress
     * @return Type of the collection.
     */
    @Nullable
    public Type getCollectionState(String deviceAddress) {
        CollectionHandler handler = this.collectionHandlers.get(deviceAddress);
        if (handler != null) {
            return handler instanceof WeightHandler ?
                    Type.WEIGHT_COLLECTION : Type.GLUCOSE_COLLECTION;
        }
        return null;
    }

    /**
     * Removes the collector handler from the collector service, when it has done its work.
     *
     * @param deviceAddress device address the collector was registered on.
     */
    private void unregisterCollector(String deviceAddress) {
        this.collectionHandlers.remove(deviceAddress);
        broadcastDeviceMessage("Collector handler for " + deviceAddress + " unregistered.");
        Intent intent = new Intent(COLLECTOR_STOPPED);
        intent.putExtra(DEVICE_ADDR, deviceAddress);
        localBroadcastManager.sendBroadcast(intent);
    }

    /**
     * This method passes the device event to the corresponding collection handler.
     *
     * @param deviceAddress
     */
    public void forwardResultToCollector(String deviceAddress, Intent intent) {
        CollectionHandler handler = collectionHandlers.get(deviceAddress);
        if (handler == null) {
            Log.e(TAG, "Collection handler for address " + deviceAddress + " not found.");
        }
        Log.d(TAG, "Received result for " + deviceAddress + ", sending to " + handler.getClass().getSimpleName());
        handler.processResult(intent);
    }

    /**
     * This method add the new measurement to array in PHR document.
     * When no document exist, firstly create a new one.
     *
     * @param measurementJSONObject
     */
    private void saveMeasurementToPHR(String measurementJSONObject, String type) {
        JSONObject jsonObject;
        try {
            long startTime = System.nanoTime();

            // check if the PHR document exist and retrieve the document with glucose or weight data
            JSONArray documentContentArray = new PHRdata(type, "newMeasurement", null).execute().get();

            if (documentContentArray != null && !documentContentArray.toString().equals("[]")) {
                // add new value to document content
                jsonObject = new JSONObject(measurementJSONObject);
                documentContentArray.put(jsonObject);

                // create a new PHR document
                new PHRdata(type, "createDocuments", documentContentArray).execute();
                long endTime = System.nanoTime();
                Log.i(TAG_PERF, "Measure|Retrieve a PHR document with measurement|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");

                // first create a document in PHR
            } else {
                documentContentArray = new JSONArray();
                jsonObject = new JSONObject(measurementJSONObject);
                documentContentArray.put(jsonObject);
                new PHRdata(type, "createDocuments", documentContentArray).execute();
                long endTime = System.nanoTime();
                Log.i(TAG_PERF, "Measure|Create new PHR document with measurement|mix|-|" + startTime / 1000000 + "|" +
                        (endTime - startTime) / 1000000 + "|" + "-");
            }
            long endTime = System.nanoTime();
            Log.i(TAG_PERF, "Measure|Measure|mix|-|" + startTime / 1000000 + "|" +
                    (endTime - startTime) / 1000000 + "|" + "-");
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method create a new JSON element with weight measurement values.
     */
    private String setWeightInPHRDocument(String weight, String height, String bmi, String weightUnit,
                                          String heightUnit, String deviceTime, String receiveTime) {
        return "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                "  \"title\": \"Weight Measurement\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"weight\": {\n" +
                "      \"description\": \"measured mass in weight units\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + weight + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"weightUnit\": {\n" +
                "      \"description\": \"mass unit\",\n" +
                "      \"value\": \"" + weightUnit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"kg\",\n" +
                "        \"lb\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"height\": {\n" +
                "      \"description\": \"measured size in height units\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + height + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"heightUnit\": {\n" +
                "      \"description\": \"size unit\",\n" +
                "      \"value\": \"" + heightUnit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"m\",\n" +
                "        \"in\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"bmi\": {\n" +
                "      \"description\": \"calculated body mass index\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"value\": \"" + bmi + "\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"deviceTime\": {\n" +
                "      \"description\": \"ISO time string given by measurement device\",\n" +
                "      \"value\": \"" + deviceTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"receiveTime\": {\n" +
                "      \"description\": \"ISO time string, stating when the app received measurement from device\",\n" +
                "      \"value\": \"" + receiveTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }

    /**
     * This method create a new JSON element with glucose measurement values.
     */
    private String setGlucoseInPHRDocument(String sequenceNumber, String concentration, String unit,
                                           String fluidType, String sampleLocation, String sensorStatus,
                                           String deviceTime, String receiveTime) {
        return "{\n" +
                "  \"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" +
                "  \"title\": \"Glucose Measurement\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"sequenceNumber\": {\n" +
                "      \"description\": \"internal measurement id given by the device\",\n" +
                "      \"value\": \"" + sequenceNumber + "\",\n" +
                "      \"type\": \"integer\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"deviceTime\": {\n" +
                "      \"description\": \"ISO time string given by measurement device\",\n" +
                "      \"value\": \"" + deviceTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"receiveTime\": {\n" +
                "      \"description\": \"ISO time string, stating when the app received measurement from device\",\n" +
                "      \"value\": \"" + receiveTime + "\",\n" +
                "      \"type\": \"string\"\n" +
                "    },\n" +
                "    \"concentration\": {\n" +
                "      \"description\": \"how much glucose the device has measured in the given unit\",\n" +
                "      \"value\": \"" + concentration + "\",\n" +
                "      \"type\": \"number\",\n" +
                "      \"minimum\": 0\n" +
                "    },\n" +
                "    \"unit\": {\n" +
                "      \"description\": \"physical unit describing the glucose concentration\",\n" +
                "      \"value\": \"" + unit + "\",\n" +
                "      \"enum\": [\n" +
                "        \"kg/L\",\n" +
                "        \"mol/L\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"fluidType\": {\n" +
                "      \"description\": \"fluid type delivered to device\",\n" +
                "      \"value\": \"" + fluidType + "\",\n" +
                "      \"enum\": [\n" +
                "        \"Capillary Whole blood\",\n" +
                "        \"Capillary Plasma\",\n" +
                "        \"Venous Whole blood\",\n" +
                "        \"Venous Plasma\",\n" +
                "        \"Arterial Whole blood\",\n" +
                "        \"Arterial Plasma\",\n" +
                "        \"Undetermined Whole blood\",\n" +
                "        \"Undetermined Plasma\",\n" +
                "        \"Interstitial Fluid (ISF)\",\n" +
                "        \"Control Solution\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"sampleLocation\": {\n" +
                "      \"description\": \"body location the fluid was taken from\",\n" +
                "      \"value\": \"" + sampleLocation + "\",\n" +
                "      \"enum\": [\n" +
                "        \"Finger\",\n" +
                "        \"Alternate Site Test (AST)\",\n" +
                "        \"Earlobe\",\n" +
                "        \"Control solution\",\n" +
                "        \"Sample Location value not available\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"sensorStatus\": {\n" +
                "      \"description\": \"technical annunciations by the measurement device\",\n" +
                "      \"value\": \"" + sensorStatus + "\",\n" +
                "      \"type\": \"array\",\n" +
                "      \"uniqueItems\": true,\n" +
                "      \"minItems\": 0,\n" +
                "      \"items\": {\n" +
                "        \"enum\": [\n" +
                "          \"Device battery low at time of measurement\",\n" +
                "          \"Sensor malfunction or faulting at time of measurement\",\n" +
                "          \"Sample size for blood or control solution insufficient at time of measurement\",\n" +
                "          \"Strip insertion error\",\n" +
                "          \"Strip type incorrect for device\",\n" +
                "          \"Sensor result higher than the device can process\",\n" +
                "          \"Sensor result lower than the device can process\",\n" +
                "          \"Sensor temperature too high for valid test/result at time of measurement\",\n" +
                "          \"Sensor temperature too low for valid test/result at time of measurement\",\n" +
                "          \"Sensor read interrupted because strip was pulled too soon at time of measurement\",\n" +
                "          \"General device fault has occurred in the sensor\",\n" +
                "          \"Time fault has occurred in the sensor and time may be inaccurate\"\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }
}