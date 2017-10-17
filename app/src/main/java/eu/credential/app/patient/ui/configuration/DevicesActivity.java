package eu.credential.app.patient.ui.configuration;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.credential_v020.R;
import eu.credential.app.patient.orchestration.collection.CollectorBroadcastReceiver;
import eu.credential.app.patient.orchestration.collection.CollectorService;
import eu.credential.app.patient.orchestration.collection.CollectorServiceConnection;
import eu.credential.app.patient.orchestration.collection.WithCollectorService;
import eu.credential.app.patient.ui.settings.SettingsBroadcastReceiver;
import eu.credential.app.patient.integration.upload.UploadBroadcastReceiver;
import eu.credential.app.patient.ui.diary.DiaryFragment;

public class DevicesActivity extends AppCompatActivity implements WithCollectorService{

    // tag used for android logging
    private static final String TAG = DiaryFragment.class.getSimpleName();
    private LocalBroadcastManager localBroadcastManager;

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;
    private SettingsBroadcastReceiver settingsBroadcastReceiver;

    //Glucometer UI definition
    ImageView glucometer;
    ImageView paringGlucometer;
    ImageView searchGlucometer;
    TextView textGlucometer;
    TextView textGlucometerName;
    //Scale UI definition
    ImageView scale;
    ImageView paringScale;
    ImageView searchScale;
    TextView textScale;
    TextView textScaleName;

    public DevicesActivity() {
        super();
        this.collectorService = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_devices);
        toolbar.setTitle(R.string.diary_toolbar_devices);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        //Glucometer UI identification
        glucometer = (ImageView) findViewById(R.id.imageView_glucometer);
        paringGlucometer = (ImageView) findViewById(R.id.button_paring_glucometer);
        searchGlucometer = (ImageView) findViewById(R.id.search_glucometer);
        textGlucometer = (TextView) findViewById(R.id.text_glucometer);
        textGlucometerName = (TextView) findViewById(R.id.glucometer_name);
        //Scale UI identification
        scale = (ImageView) findViewById(R.id.imageView_scale);
        paringScale = (ImageView) findViewById(R.id.button_paring_scale);
        searchScale = (ImageView) findViewById(R.id.button_search_scale);
        textScale = (TextView) findViewById(R.id.text_device_scale);
        textScaleName = (TextView) findViewById(R.id.scale_name);

        this.localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this, CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        this.bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // Load the preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }
    @Override
    protected void onResume() {
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
    }

    @Override
    protected void onPause() {

        localBroadcastManager.unregisterReceiver(collectorBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(uploadBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(settingsBroadcastReceiver);

        super.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(this.collectorServiceConnection);
    }
    @Override
    public void listMessage(final String message) {
    }
    @Override
    public void setCollectorService(CollectorService collectorService) {
        this.collectorService = collectorService;
        if (this.collectorService != null) {
            refreshMeasurements();
        }
    }
    @Override
    public void refreshMeasurements() {
    }
    @Override
    public void refreshMessages() {
    }
    @Override
    public void displayConnectionStateChange(String deviceAddress, String deviceName, boolean hasConnected) {

                // Glucometer
                // Device not found
            if (deviceName == "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.GLUCOSE_COLLECTION) {
                glucometer.setImageResource(R.drawable.x_mark);
                paringGlucometer.setVisibility(View.INVISIBLE);
                textGlucometer.setText(R.string.textview_deviceNotFound);
                textGlucometerName.setText("");
                // Device found und connected
            } else if (hasConnected && deviceName != "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.GLUCOSE_COLLECTION) {
                glucometer.setImageResource(R.drawable.high_connection);
                paringGlucometer.setImageResource(R.drawable.paring);
                paringGlucometer.setVisibility(View.VISIBLE);
                textGlucometer.setText(R.string.textview_devicePaired);
                textGlucometerName.setText(deviceName);
                searchGlucometer.setVisibility(View.INVISIBLE);
                // Device found und disconnected
            } else if (!hasConnected && deviceName != "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.GLUCOSE_COLLECTION) {
                glucometer.setImageResource(R.drawable.no_connection);
                textGlucometer.setText(R.string.textview_deviceFound);
                textGlucometerName.setText(deviceName);
                searchGlucometer.setVisibility(View.INVISIBLE);
                paringGlucometer.setVisibility(View.VISIBLE);
                paringGlucometer.setImageResource(R.drawable.unparing);

                // Scale
            } else if (deviceName == "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.WEIGHT_COLLECTION) {
                scale.setImageResource(R.drawable.x_mark);
                paringScale.setVisibility(View.INVISIBLE);
                textScale.setText(R.string.textview_deviceNotFound);
                textScaleName.setText("");
            } else if (hasConnected && deviceName != "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.WEIGHT_COLLECTION) {
                scale.setImageResource(R.drawable.high_connection);
                paringScale.setImageResource(R.drawable.paring);
                paringScale.setVisibility(View.VISIBLE);
                textScale.setText(R.string.textview_devicePaired);
                textScaleName.setText(deviceName);
                searchScale.setVisibility(View.INVISIBLE);
            } else if (!hasConnected && deviceName != "" && collectorService.getCollectionState(deviceAddress) == CollectorService.Type.WEIGHT_COLLECTION) {
                scale.setImageResource(R.drawable.no_connection);
                paringScale.setVisibility(View.VISIBLE);
                paringScale.setImageResource(R.drawable.unparing);
                textScale.setText(R.string.textview_deviceFound);
                textScaleName.setText(deviceName);
                searchScale.setVisibility(View.INVISIBLE);
            }
    }

    public void onClickSearchGlucometer(View v) {
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentBluetooth);
    }

    public void onClickSearchScale(View view) {
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentBluetooth);
    }

    public void onClickSearchSteps(View view) {
        Intent intentBluetooth = new Intent();
        intentBluetooth.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
        startActivity(intentBluetooth);
    }
}
