package eu.credential.app.patient.ui.myHealthRecords;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.administrator.credential_v020.R;

import eu.credential.app.patient.helper.TransmitData;
import eu.credential.app.patient.ui.myHealthRecords.event.EventListMain;
import eu.credential.app.patient.ui.myHealthRecords.protocol.ProtocolListMain;

/**
 * Created by Aleksei Piatkin on 02.04.17.
 * <p>
 * A health records screen that shows list of patient health documents.
 */
public class MyHealthRecordFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_health_record, container, false);

        ImageButton eventButton = v.findViewById(R.id.event_button);
        ImageButton protocolButton = v.findViewById(R.id.protocol_button);
        //ImageButton revokeButton = v.findViewById(R.id.revoke_button);

        final TransmitData toEvent = new TransmitData.TransmitDataBuilder()
                .fragment(new EventListMain()).fragmentActivity(getActivity()).build();
        final TransmitData toProtocol = new TransmitData.TransmitDataBuilder()
                .fragment(new ProtocolListMain()).fragmentActivity(getActivity()).build();

        eventButton.setOnClickListener(v1 -> toEvent.changeFragment(R.id.fragment_my_health_record));
        protocolButton.setOnClickListener(v12 -> toProtocol.changeFragment(R.id.fragment_my_health_record));

        /*revokeButton.setOnClickListener(v13 -> {
            FragmentManager manager = getFragmentManager();
            RevokeDialogFragment dialog = RevokeDialogFragment.newInstance();
            if (manager != null) {
                dialog.show(manager, "example");
            }
        });*/
        return v;
    }
}
