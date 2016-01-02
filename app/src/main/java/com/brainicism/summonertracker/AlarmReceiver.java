package com.brainicism.summonertracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public static final int REQUEST_CODE = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context , TrackingService.class);
        i.putExtra("summName", intent.getStringArrayListExtra("summName"));
        context.startService(i);
    }

}
