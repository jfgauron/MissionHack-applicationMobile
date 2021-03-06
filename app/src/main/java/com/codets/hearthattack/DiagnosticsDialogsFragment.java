package com.codets.hearthattack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class DiagnosticsDialogsFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        CharSequence[] diagnostics = getArguments().getCharSequenceArray("diagnostics");

        if(diagnostics == null){
            builder.setTitle(R.string.dialog_diagnostics_no_results_title)
                    .setMessage(R.string.dialog_diagnostics_no_results)
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }else{
            builder.setTitle(R.string.dialog_diagnostics_results)
                    .setItems(diagnostics, null)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //TODO set diagnostic visible
                        }
                    })
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
        }
        return builder.create();
    }

    public static DiagnosticsDialogsFragment newInstance(CharSequence[] diagnostic){
        DiagnosticsDialogsFragment f = new DiagnosticsDialogsFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putCharSequenceArray("diagnostics", diagnostic);
        f.setArguments(args);

        return f;
    }

}
