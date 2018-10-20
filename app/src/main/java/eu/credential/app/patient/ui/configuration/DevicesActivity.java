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

import java.util.Objects;

import eu.credential.app.patient.orchestration.collection.CollectorBroadcastReceiver;
import eu.credential.app.patient.orchestration.collection.CollectorService;
import eu.credential.app.patient.orchestration.collection.CollectorServiceConnection;
import eu.credential.app.patient.orchestration.collection.WithCollectorService;
import eu.credential.app.patient.integration.upload.UploadBroadcastReceiver;

/**
 * Created by Aleksei Piatkin on 02.05.17.
 * <p>
 * A device screen that offers to choose and connect bluetooth devices.
 */
public class DevicesActivity extends AppCompatActivity implements WithCollectorService {
    private LocalBroadcastManager localBroadcastManager;

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;

    //Glucometer UI definition
    private ImageView glucometer;
    private ImageView searchGlucometer;
    private TextView textGlucometer;
    private TextView textGlucometerName;
    //Scale UI definition
    private ImageView scale;
    private ImageView searchScale;
    private TextView textScale;
    private TextView textScaleName;

    public DevicesActivity() {
        super();
        this.collectorService = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);

        Toolbar toolbar = findViewById(R.id.toolbar_devices);
        toolbar.setTitle(R.string.diary_toolbar_devices);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> finish());
        //Glucometer UI identification
        glucometer = findViewById(R.id.imageView_glucometer);
        searchGlucometer = findViewById(R.id.search_glucometer);
        textGlucometer = findViewById(R.id.text_glucometer);
        textGlucometerName = findViewById(R.id.glucometer_name);
        //Scale UI identification
        scale = findViewById(R.id.imageView_scale);
        searchScale = findViewById(R.id.button_search_scale);
        textScale = findViewById(R.id.text_device_scale);
        textScaleName = findViewById(R.id.scale_name);

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
    }

    @Override
    protected void onPause() {
        localBroadcastManager.unregisterReceiver(collectorBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(uploadBroadcastReceiver);
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
        if (Objects.equals(deviceName, "") && collectorService.getCollectionState(deviceAddress)
                == CollectorService.Type.GLUCOSE_COLLECTION) {
            glucometer.setImageResource(R.drawable.x_mark);
            textGlucometer.setText(R.string.textview_deviceNotFound);
            textGlucometerName.setText("");
            // Device found und connected
        } else if (hasConnected && !Objects.equals(deviceName, "") && collectorService
                .getCollectionState(deviceAddress) == CollectorService.Type.GLUCOSE_COLLECTION) {
            glucometer.setImageResource(R.drawable.high_connection);
            textGlucometer.setText(R.string.textview_devicePaired);
            textGlucometerName.setText(deviceName);
            searchGlucometer.setVisibility(View.INVISIBLE);
            // Device found und disconnected
        } else if (!hasConnected && !Objects.equals(deviceName, "") && collectorService
                .getCollectionState(deviceAddress) == CollectorService.Type.GLUCOSE_COLLECTION) {
            glucometer.setImageResource(R.drawable.no_connection);
            textGlucometer.setText(R.string.textview_deviceFound);
            textGlucometerName.setText(deviceName);
            searchGlucometer.setVisibility(View.INVISIBLE);

            // Scale
        } else if (Objects.equals(deviceName, "") && collectorService.getCollectionState(deviceAddress)
                == CollectorService.Type.WEIGHT_COLLECTION) {
            scale.setImageResource(R.drawable.x_mark);
            textScale.setText(R.string.textview_deviceNotFound);
            textScaleName.setText("");
        } else if (hasConnected && !Objects.equals(deviceName, "") && collectorService
                .getCollectionState(deviceAddress) == CollectorService.Type.WEIGHT_COLLECTION) {
            scale.setImageResource(R.drawable.high_connection);
            textScale.setText(R.string.textview_devicePaired);
            textScaleName.setText(deviceName);
            searchScale.setVisibility(View.INVISIBLE);
        } else if (!hasConnected && !Objects.equals(deviceName, "") && collectorService
                .getCollectionState(deviceAddress) == CollectorService.Type.WEIGHT_COLLECTION) {
            scale.setImageResource(R.drawable.no_connection);
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
