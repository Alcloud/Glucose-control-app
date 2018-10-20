package eu.credential.app.patient.ui.configuration;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.administrator.credential_v020.R;

public class ConfigurationFragment extends Fragment {
    ImageButton myDevices;
    ImageButton extendConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_configuration, container, false);

        myDevices = v.findViewById(R.id.button_my_devices);
        myDevices.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), DevicesActivity.class);
            startActivity(intent);
        });
        extendConfig = v.findViewById(R.id.button_extend_config);
        extendConfig.setOnClickListener(v12 -> {
            Intent intent = new Intent(getActivity(), NotificationList.class);
            startActivity(intent);
        });
        return v;
    }

}
