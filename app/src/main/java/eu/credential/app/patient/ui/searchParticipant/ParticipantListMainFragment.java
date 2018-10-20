package eu.credential.app.patient.ui.searchParticipant;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.administrator.credential_v020.R;

/**
 * Created by Aleksei Piatkin on 02.10.17.
 * <p>
 * A main class for participant list screen.
 */
public class ParticipantListMainFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        return inflater.inflate(R.layout.fragment_participant_list_main, container, false);
    }

}
