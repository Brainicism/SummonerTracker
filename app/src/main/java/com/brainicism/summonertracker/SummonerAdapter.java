package com.brainicism.summonertracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
                MainActivity.trackingList.showContextMenu();
            }
        });

        return convertView;
    }
}
