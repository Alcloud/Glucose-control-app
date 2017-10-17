package eu.credential.app.patient.ui.myHealthRecords;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import eu.credential.app.patient.ui.myHealthRecords.protocol.ProtocolActivity;
import com.example.administrator.credential_v020.R;
import eu.credential.app.patient.ui.myHealthRecords.event.EventActivity;

public class MyHealthRecordFragment extends Fragment {

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
        View v = inflater.inflate(R.layout.fragment_my_health_record, container, false);

        ImageButton eventButton = (ImageButton) v.findViewById(R.id.event_button);
        eventButton.setOnClickListener(v1 -> {
            Intent intent = new Intent(getActivity(), EventActivity.class);
            startActivity(intent);
        });
        ImageButton protocolButton = (ImageButton) v.findViewById(R.id.protocol_button);
        protocolButton.setOnClickListener(v12 -> {
            Intent intent = new Intent(getActivity(), ProtocolActivity.class);
            startActivity(intent);
        });
        ImageButton revokeButton = (ImageButton) v.findViewById(R.id.revoke_button);
        revokeButton.setOnClickListener(v13 -> {
            FragmentManager manager = getFragmentManager();
            RevokeDialogFragment dialog = new RevokeDialogFragment().newInstance();
            dialog.show(manager, "example");
        });
        return v;
    }

}
