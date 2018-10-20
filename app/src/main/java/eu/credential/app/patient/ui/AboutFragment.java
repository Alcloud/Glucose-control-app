package eu.credential.app.patient.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.administrator.credential_v020.R;
/**
 * Created by Aleksei Piatkin on 02.02.17.
 * <p>
 * "About" screen, that shows info about app.
 */
public class AboutFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("About Credential")
                .setMessage("CREDENTIAL Patient Application.\nVersion 0.9.0. ")
                .setIcon(R.drawable.fokus_icon)
                .setPositiveButton("Ok", (dialog, id) -> dialog.cancel());
        return builder.create();
    }
}
