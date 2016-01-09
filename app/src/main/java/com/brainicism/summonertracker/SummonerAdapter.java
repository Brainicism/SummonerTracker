package com.brainicism.summonertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SummonerAdapter extends ArrayAdapter<String> {
TextView postNotifText;
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
        postNotifText = (TextView) convertView.findViewById(R.id.postNotifText);
        SharedPreferences prefs = getContext().getSharedPreferences("summoner_prefs", Context.MODE_PRIVATE);
        summonerName = getItem(position);
        summonerNameText = (TextView) convertView.findViewById(R.id.summonerName);
        summonerNameText.setText(summonerName);
        boolean postNotif = prefs.getBoolean(summonerName+"_postNotif",false);
        if (postNotif){
            postNotifText.setText("Post-game notifications: ON");
        }
        else{
            postNotifText.setText("Post-game notifications: OFF");
        }

        return convertView;
    }
}
