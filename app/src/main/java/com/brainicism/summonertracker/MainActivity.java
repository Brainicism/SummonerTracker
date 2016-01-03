package com.brainicism.summonertracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final String apiKey = "YOUR API KEY HERE";
    private static final String TAG = "MainActivity";
    ArrayList<String> summonerNames = new ArrayList<>();
    TextView nameOne, nameTwo, nameThree;
    TextView trackingStatus;
    ListView trackingList;
    SummonerAdapter listAdapter;

    @Override
    protected void onRestart() {
        updateStatus();
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //will replace with adding summoners to track later
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                alarmActive();
            }
        });
        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                summonerNames.clear();
                summonerNames.add(nameOne.getText().toString()); //get inputs from text fields
                summonerNames.add(nameTwo.getText().toString());
                summonerNames.add(nameThree.getText().toString());
                saveArray(summonerNames, getBaseContext()); //save array to shared prefs
                List<String> outputs = loadArray(getBaseContext());
                for (int i = 0; i < outputs.size(); i++) {
                    Log.i(TAG, "Name: " + outputs.get(i));
                }

                scheduleAlarm();
                Snackbar.make(v, "Tracking Begin", Snackbar.LENGTH_SHORT).show();
                Log.i(TAG, "Tracking begin");
                listAdapter = new SummonerAdapter(MainActivity.this, summonerNames);
                trackingList.setAdapter(listAdapter);
                updateStatus();
            }
        });

        findViewById(R.id.stop_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlarm();
                Snackbar.make(v, "Tracking End", Snackbar.LENGTH_SHORT).show();
                updateStatus();
            }
        });
        trackingStatus = (TextView) findViewById(R.id.trackingStatus);
        trackingList = (ListView) findViewById(R.id.trackingList);
        nameOne = (TextView) findViewById(R.id.summonerBox1);
        nameTwo = (TextView) findViewById(R.id.summonerBox2);
        nameThree = (TextView) findViewById(R.id.summonerBox3);
        nameOne.setText("terminator6736");
        nameTwo.setText("sebi");
        nameThree.setText("clg imaqtpie69");
        updateStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean saveArray(List<String> array, Context mContext) { //save array to sharedprefs
        SharedPreferences prefs = mContext.getSharedPreferences("summoner_names", 0);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>();
        set.addAll(array);
        editor.putStringSet("summoner_names", set);
        return editor.commit();
    }

    public List<String> loadArray(Context mContext) { //load array from sharedprefs
        SharedPreferences prefs = mContext.getSharedPreferences("summoner_names", 0);
        Set<String> setNames = prefs.getStringSet("summoner_names", null);
        List<String> listNames = new ArrayList<>();
        listNames.addAll(setNames);
        return listNames;
    }

    public void scheduleAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putStringArrayListExtra("summName", summonerNames);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5, pIntent);
    }

    public void cancelAlarm() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        pIntent.cancel();
    }

    public boolean alarmActive() {
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        boolean alarmUp = ((PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE)) != null);
        Log.i(TAG, String.valueOf(alarmUp));
        return alarmUp;
    }

    public void updateStatus() {
        if (alarmActive()) {
            trackingStatus.setText("Currently tracking");
            trackingStatus.setTextColor(Color.GREEN);
        } else {
            trackingStatus.setText("Not tracking");
            trackingStatus.setTextColor(Color.RED);
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}

