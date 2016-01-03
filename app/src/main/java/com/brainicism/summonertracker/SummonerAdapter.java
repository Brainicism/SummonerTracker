package com.brainicism.summonertracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
        final CheckBox checkBox;

        summonerName = getItem(position);
        summonerNameText = (TextView) convertView.findViewById(R.id.summonerName);
        summonerNameText.setText(summonerName);

        checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Toast.makeText(getContext(), summonerName, Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }
}
