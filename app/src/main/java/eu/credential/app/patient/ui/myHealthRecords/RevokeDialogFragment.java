package eu.credential.app.patient.ui.myHealthRecords;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.widget.Toast;

import com.example.administrator.credential_v020.R;

public class RevokeDialogFragment extends DialogFragment {

    public static RevokeDialogFragment newInstance() {
        RevokeDialogFragment f = new RevokeDialogFragment();
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] confArray = {getString(R.string.checkbox_revoke)};
        final boolean[] checkedItemsArray = {false};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.revoke_title)
                .setMultiChoiceItems(confArray, checkedItemsArray,
                        (dialog, which, isChecked) -> checkedItemsArray[which] = isChecked)
                .setPositiveButton(R.string.accept,
                        (dialog, id) -> {
                            //TODO save this data to a new class(?)
                            //also to "NewActivity, there create method "okClicked"
                            //((NewActivity) getActivity()).okClicked();
                            //in NewActivity:
                            /*public void okClicked() {
                             Toast.makeText(getApplicationContext(), "You chosen OK!",
                             Toast.LENGTH_LONG).show();}*/

                            StringBuilder state = new StringBuilder();
                            for (int i = 0; i < confArray.length; i++) {
                                state.append(confArray[i]);
                                if (checkedItemsArray[i])
                                    state.append(" - accept\n");
                                else
                                    state.append(" - not accept\n");
                            }
                            Toast.makeText(getActivity(),
                                    state.toString(), Toast.LENGTH_LONG)
                                    .show();
                        })
                .setNegativeButton(getString(R.string.reject),
                        (dialog, id) -> dialog.cancel());

        return builder.create();
    }
}
