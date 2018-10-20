package eu.credential.app.patient.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.PatientApp;
import eu.credential.app.patient.helper.LogoutService;
import eu.credential.app.patient.helper.SavePreferences;
import eu.credential.app.patient.orchestration.collection.CollectorBroadcastReceiver;
import eu.credential.app.patient.orchestration.collection.CollectorService;
import eu.credential.app.patient.orchestration.collection.CollectorServiceConnection;
import eu.credential.app.patient.orchestration.collection.WithCollectorService;
import eu.credential.app.patient.integration.model.GlucoseMeasurement;
import eu.credential.app.patient.integration.model.Measurement;
import eu.credential.app.patient.integration.model.WeightMeasurement;
import eu.credential.app.patient.orchestration.services.RefreshSchedule;
import eu.credential.app.patient.ui.my_doctors.MyDoctorsFragment;
import eu.credential.app.patient.ui.my_doctors.doctors_address_book.AddressBookMainFragment;
import eu.credential.app.patient.ui.scanner.ScanFragment;
import eu.credential.app.patient.ui.searchParticipant.SearchParticipant;
import eu.credential.app.patient.integration.upload.UploadBroadcastReceiver;
import eu.credential.app.patient.ui.vitals.DiaryFragment;
import eu.credential.app.patient.ui.myHealthRecords.MyHealthRecordFragment;
import eu.credential.app.patient.ui.configuration.ConfigurationFragment;
import eu.credential.app.patient.ui.user_details.AskToLogout;
import eu.credential.app.patient.ui.user_details.UserActivity;
import eu.credential.app.patient.ui.vitals.HBA1cFragment;
import eu.credential.app.patient.ui.vitals.VitalsFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Aleksei Piatkin on 04.03.17.
 * <p>
 * Main Activity.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, WithCollectorService {

    // User name in navigation board
    private TextView navAccountName;

    // tag used for android logging
    private static final String TAG = DiaryFragment.class.getSimpleName();
    private LocalBroadcastManager localBroadcastManager;

    // Services the activity works with
    private CollectorService collectorService;
    private CollectorServiceConnection collectorServiceConnection;
    private CollectorBroadcastReceiver collectorBroadcastReceiver;
    private UploadBroadcastReceiver uploadBroadcastReceiver;

    // current state of the collected data
    private static Map<Integer, Measurement> measurementMap;
    private int currentCounter;

    private RefreshSchedule refreshSchedule = new RefreshSchedule(MainActivity.this);
    Fragment fragment = null;
    Class fragmentClass = null;
    FragmentManager fragmentManager = getSupportFragmentManager();

    public MainActivity() {
        super();

        // services
        this.collectorService = null;
        measurementMap = Collections.synchronizedMap(new TreeMap<Integer, Measurement>());
        this.currentCounter = 0;
    }

    @SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        writeLogs();

        // refresh ssoToken
        refreshSchedule.startRepeatingTask();
        // start auto logout timer
        refreshSchedule.timer().start();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //Menu nav_Menu = navigationView.getMenu();
        //nav_Menu.findItem(R.id.nav_my_vital_data).setVisible(false);
        View hView = navigationView.getHeaderView(0);
        navAccountName = hView.findViewById(R.id.textView_lo);

        // Show general info by very first login
        if (SavePreferences.getDefaultsBoolean("firstLogin", PatientApp.getContext())) {
            GeneralInfo generalInfo = new GeneralInfo();
            generalInfo.show(getSupportFragmentManager(), "FirstLogin");
        }

        this.localBroadcastManager = LocalBroadcastManager.getInstance(this);

        // Register the BLE service and bind it
        Intent collectorServiceIntent = new Intent(this, CollectorService.class);
        this.collectorServiceConnection = new CollectorServiceConnection(this);
        this.bindService(collectorServiceIntent, collectorServiceConnection, Context.BIND_AUTO_CREATE);

        // Load the preferences
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        // To get user name and surname
        getUserDetails();
    }

    @Override
    public void onUserInteraction() {
        refreshSchedule.timer().cancel();
        refreshSchedule.timer().start();
    }

    @SuppressLint("SetTextI18n")
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

        // on first execution the collector service will not be bound
        if (this.collectorService != null) {
            refreshMeasurements();
        }
        // To get user name and surname
        getUserDetails();
    }

    @Override
    protected void onPause() {

        localBroadcastManager.unregisterReceiver(collectorBroadcastReceiver);
        localBroadcastManager.unregisterReceiver(uploadBroadcastReceiver);
        refreshSchedule.timer().cancel();

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        refreshSchedule.timer().cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogoutService.clearAllPreferences();
        unbindService(this.collectorServiceConnection);
        refreshSchedule.timer().cancel();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_camera).setVisible(false);
        menu.findItem(R.id.action_refresh).setVisible(false);
        return false;
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
    public void displayConnectionStateChange(String deviceAddress, String deviceName, boolean hasConnected) {
        String statusText = hasConnected ? "connected" : "disconnected";
        if (!MainActivity.this.isFinishing() && !this.isFinishing()) {
            Toast.makeText(
                    this.getApplicationContext(),
                    deviceName + " " + statusText,
                    Toast.LENGTH_SHORT).show();
        }
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
        Map<Integer, Measurement> here = measurementMap;

        // Search for new entries and collect them
        for (Integer id : there.keySet()) {
            if (!here.containsKey(id)) {
                Measurement newMeasurement = there.get(id);
                here.put(id, newMeasurement);
            }
        }
        //show toast message
        showToast(here);
        // update the current revision
        this.currentCounter = collectorService.getDataCount();
    }

    // Show measurement und device state as toast notification
    private void showToast(Map<Integer, Measurement> here) {
        StringBuilder builder = new StringBuilder();
        ArrayList<Number> toastSeries = new ArrayList<>();
        GlucoseMeasurement glucoseValue;
        WeightMeasurement weightValue;

        for (Integer key : here.keySet()) {
            Measurement meas = here.get(key);
            if (meas instanceof GlucoseMeasurement) {
                glucoseValue = (GlucoseMeasurement) meas;
                toastSeries.add(glucoseValue.getGlucoseConcentration() * 100000);
            } else if (meas instanceof WeightMeasurement) {
                weightValue = (WeightMeasurement) meas;
                toastSeries.add(weightValue.getWeight());
            }
        }
        //show toast message
        builder.append(toastSeries.get(toastSeries.size() - 1).toString());
        Toast.makeText(this.getApplicationContext(), "Last Value: " + builder.toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refreshMessages() {
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.openDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                fragmentClass = ConfigurationFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_refresh:
                return false;
            case R.id.action_address_book:
                fragmentClass = AddressBookMainFragment.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_camera:
                try { // start camera
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_CAMERA_BUTTON);
                    intent.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_CAMERA));
                    sendOrderedBroadcast(intent, null);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                return false;
        }
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.nav_health_records) {
            fragmentClass = MyHealthRecordFragment.class;
        } else if (id == R.id.nav_doctors) {
            fragmentClass = MyDoctorsFragment.class;
        } else if (id == R.id.nav_search_participant) {
            fragmentClass = SearchParticipant.class;
        } else if (id == R.id.nav_blood_sugar) {
            fragmentClass = DiaryFragment.class;
        } else if (id == R.id.nav_hba1c) {
            fragmentClass = HBA1cFragment.class;
            //} else if (id == R.id.nav_clinical) {
            //    fragmentClass = ClinicalMainFragment.class;
        } else if (id == R.id.nav_weight) {
            fragmentClass = VitalsFragment.class;
        } else if (id == R.id.nav_conf) {
            fragmentClass = ConfigurationFragment.class;
        } else if (id == R.id.nav_about) {
            if (fragmentClass == null) {
                setFragment(MyHealthRecordFragment.class);
            }
            AboutFragment aboutFragment = new AboutFragment();
            aboutFragment.setCancelable(false);
            aboutFragment.show(fragmentManager, "Dialog!");
        } else if (id == R.id.nav_scan) {
            fragmentClass = ScanFragment.class;
        } else if (id == R.id.logout) {
            if (fragmentClass == null) {
                setFragment(MyHealthRecordFragment.class);
            }
            AskToLogout askToLogout = new AskToLogout();
            askToLogout.show(fragmentManager, "Logout");
        }
        setFragment(fragmentClass);
        item.setChecked(true);
        setTitle(item.getTitle());
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onClickAccount(View v) {
        Intent intent = new Intent(this, UserActivity.class);
        startActivity(intent);
    }

    private void setFragment(Class fragmentClass) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();
    }

    @SuppressLint("SetTextI18n")
    private void getUserDetails() {
        String name = SavePreferences.getDefaultsString("userName", PatientApp.getContext());
        String surname = SavePreferences.getDefaultsString("userSurname", PatientApp.getContext());
        // Get user name from shared preferences
        if (name != null && surname != null && !name.equals("") && !surname.equals("")) {
            navAccountName.setText(name + " " + surname);
        } else {
            navAccountName.setText(SavePreferences.getDefaultsString("accountId", PatientApp.getContext()));
        }
    }

    private void writeLogs() {
        try {
            File logFile = new File(getApplication().getFilesDir() + "/", "performance.txt");
            Runtime.getRuntime().exec("logcat -f " + logFile + " logcat -v raw Performance:I *:S");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Dialog screen that shows some registration info after successful registration.
     */
    @SuppressLint("ValidFragment")
    public static class GeneralInfo extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String title = "Welcome to Credential app";
            String message = getString(R.string.start_text);

            AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
            builder.setTitle(title)
                    .setMessage(message)
                    .setIcon(R.drawable.credential_logo);
            builder.setPositiveButton(getString(R.string.ok), (dialog, id) -> {
                SavePreferences.setDefaultsBoolean("firstLogin", false, PatientApp.getContext());
            });
            builder.setCancelable(false);
            return builder.create();
        }
    }
}