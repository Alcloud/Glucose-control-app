package eu.credential.app.patient.ui.user_details;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.administrator.credential_v020.R;

import java.util.Objects;

import eu.credential.app.patient.helper.LogoutService;
import eu.credential.app.patient.ui.LoginActivity;

/**
 * Dialog screen that asks if user really want to log out.
 */
public class AskToLogout extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String title = "Logout";
        String message = "Do you want to exit?";

        AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(this.getContext()));
        builder.setTitle(title)
                .setMessage(message)
                .setIcon(R.drawable.logout);
        builder.setPositiveButton(getString(R.string.yes), (dialog, id) -> clearAllPreferences());
        builder.setNegativeButton(getString(R.string.no), (dialog, id) -> {
        });
        builder.setCancelable(true);
        return builder.create();
    }

    /**
     * To clear user preferences after logout and close this screen
     * (next start from login screen).
     */
    protected void clearAllPreferences() {
        LogoutService.clearAllPreferences();

        // back to login screen
        Intent i = new Intent(getActivity(), LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}