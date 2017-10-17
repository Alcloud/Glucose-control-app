package eu.credential.app.patient.ui.configuration;

import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.administrator.credential_v020.R;

public class ConfigurationFragment extends Fragment {
    ImageButton myDevices;
    ImageButton extendConfig;
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.action_camera);
        item.setVisible(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_configuration, container, false);

        myDevices = (ImageButton) v.findViewById(R.id.button_my_devices);
        myDevices.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), DevicesActivity.class);
            startActivity(intent);
        });
        extendConfig = (ImageButton) v.findViewById(R.id.button_extend_config);
        extendConfig.setOnClickListener(v12 -> {
            FragmentManager manager = getFragmentManager();
            ExtendConfigDialog dialog = new ExtendConfigDialog().newInstance();
            dialog.show(manager, "example");

        });
        return v;
    }

}
