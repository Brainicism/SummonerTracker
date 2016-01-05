package com.brainicism.summonertracker;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SummonerAdapter extends ArrayAdapter<String> {

    public SummonerAdapter(Context context, ArrayList<String> summoners) {
        super(context, 0, summoners);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_layout, parent, false);
        }
        final TextView summonerNameText;
        final String summonerName;
        final ImageView cancel;

        summonerName = getItem(position);
        summonerNameText = (TextView) convertView.findViewById(R.id.summonerName);
        summonerNameText.setText(summonerName);
        cancel = (ImageView) convertView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() { //removing a tracked summoner
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Delete Summoner");
                builder.setMessage("Are you sure you want to remove this summoner from tracking" +
                        "?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), summonerName, Toast.LENGTH_SHORT).show();
                        MainActivity.summonerNames.remove(summonerName);
                        MainActivity.saveArray(MainActivity.summonerNames , getContext());
                        MainActivity.listAdapter.notifyDataSetChanged();
                        MainActivity.scheduleAlarm(getContext());
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        return convertView;
    }
}
