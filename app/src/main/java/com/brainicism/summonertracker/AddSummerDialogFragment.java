package com.brainicism.summonertracker;


import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

public class AddSummerDialogFragment extends DialogFragment{
    LayoutInflater inflater;
    View v;
    NoticeDialogListener mListener;

    public interface NoticeDialogListener{
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            mListener = (NoticeDialogListener) activity;
        }
        catch(ClassCastException e){
            throw new ClassCastException(activity.toString() + "must implement NoticeDialog Listener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        inflater = getActivity().getLayoutInflater();
        v= inflater.inflate(R.layout.add_summoner_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogPositiveClick(AddSummerDialogFragment.this);

            }
        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onDialogNegativeClick(AddSummerDialogFragment.this);
            }
        });
        return builder.create();
    }
}
