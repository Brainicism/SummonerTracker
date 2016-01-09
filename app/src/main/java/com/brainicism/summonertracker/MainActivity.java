package com.brainicism.summonertracker;

import android.app.AlarmManager;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.api.LoadPolicy;
import com.robrua.orianna.type.api.RateLimit;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.exception.APIException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements AddSummerDialogFragment.NoticeDialogListener {
    public static final String apiKey = "YOUR API KEY HERE";
    private static final String TAG = "MainActivity";
    public static ArrayList<String> summonerNames = new ArrayList<>();
    public static SummonerAdapter listAdapter;
    LinearLayout trackingHeader;
    MenuItem toggle;
    Menu menu;
    TextView trackingStatus;
    static ListView trackingList;
    String checkedName;
    int position;
   public interface OnCheckValidEndListener {
        void onCheckValidEnd(String checkedName);
    }
    @Override
    protected void onRestart() {
        updateStatus();
        super.onRestart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trackingHeader = (LinearLayout) View.inflate(MainActivity.this, R.layout.header_layout, null);
        trackingList = (ListView) findViewById(R.id.trackingList);
        trackingList.addHeaderView(trackingHeader, null, false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RiotAPI.setLoadPolicy(LoadPolicy.UPFRONT); //set up API
        RiotAPI.setRateLimit(new RateLimit(3000, 10), new RateLimit(180000, 600));
        RiotAPI.setAPIKey(MainActivity.apiKey);
        RiotAPI.setRegion(Region.NA);

        summonerNames = loadArray(getBaseContext()); //loads tracked summoners from shared prefs

        trackingStatus = (TextView) findViewById(R.id.trackingStatus);
        listAdapter = new SummonerAdapter(MainActivity.this, summonerNames); //set list adapter
        trackingList.setAdapter(listAdapter);
        registerForContextMenu(trackingList);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //opens dialog to add new summoner to tracking list
                AddSummerDialogFragment dialog = new AddSummerDialogFragment();
                dialog.show(getFragmentManager(), "track_dialog");
            }
        });
        findViewById(R.id.start_service).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { //begin or restart track
                scheduleAlarm(getApplicationContext());
                Snackbar.make(v, "Tracking Begin", Snackbar.LENGTH_SHORT).show();
                Log.i(TAG, "Tracking begin");
                updateStatus();
            }
        });

        findViewById(R.id.stop_service).setOnClickListener(new View.OnClickListener() { //cease tracking
            @Override
            public void onClick(View v) {
                cancelAlarm(getApplicationContext());
                Snackbar.make(v, "Tracking End", Snackbar.LENGTH_SHORT).show();
                updateStatus();
            }
        });
        updateStatus();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater  = getMenuInflater();
        inflater.inflate(R.menu.list_context_menu, menu);
        this.menu = menu;
        toggle = menu.findItem(R.id.postGameNotif);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int listPosition = info.position -1;
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("summoner_prefs", MODE_PRIVATE);
        boolean postNotif = prefs.getBoolean(summonerNames.get(listPosition) + "_postNotif",false);
        Log.i(TAG, "onCreateContextMenu  " + summonerNames.get(listPosition) + "_postNotif" + ": " + postNotif);
        if (postNotif){
            toggle.setTitle("Disable post-game notifications");
        }
        else
            toggle.setTitle("Enable post-game notifications");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        position = info.position-1;
        switch (item.getItemId()){
           case R.id.deleteSummoner: {
               AlertDialog.Builder builder = new AlertDialog.Builder(this);
               builder.setTitle("Delete Summoner");
               builder.setMessage("Are you sure you want to remove this summoner from tracking?");
               builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int which) {
                       Toast.makeText(getApplicationContext(), summonerNames.get(position), Toast.LENGTH_SHORT).show();
                       summonerNames.remove(position);
                       saveArray(MainActivity.summonerNames, getApplicationContext());
                       Collections.sort(summonerNames, String.CASE_INSENSITIVE_ORDER);
                       listAdapter.notifyDataSetChanged();
                       if (alarmActive()) { //if alarm active, re-set alarm
                           scheduleAlarm(getApplicationContext());
                       }
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
               break;
           }
            case R.id.postGameNotif:{
                invalidateOptionsMenu();
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("summoner_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor =  getApplicationContext().getSharedPreferences("summoner_prefs", MODE_PRIVATE).edit();
                boolean postNotif = prefs.getBoolean(summonerNames.get(position)+"_postNotif",false);
                if (postNotif) {
                    Log.i(TAG, "Disabled");
                    editor.putBoolean(summonerNames.get(position) + "_postNotif", false);
                    editor.commit();
                    listAdapter.notifyDataSetChanged();

                    Toast.makeText(MainActivity.this, "Post-game notifications disabled for " + summonerNames.get(position), Toast.LENGTH_SHORT).show();

                }
                else {
                    Log.i(TAG, "Enabled");
                    editor.putBoolean(summonerNames.get(position) + "_postNotif", true);
                    editor.commit();
                    listAdapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "Post-game notifications enabled for " + summonerNames.get(position), Toast.LENGTH_SHORT).show();

                }
                Log.i(TAG, "onContextItemSelected  "+ summonerNames.get(position)+ "_postNotif" + ": " + prefs.getBoolean(summonerNames.get(position)+"_postNotif",false));

                break;
            }

       }
        return super.onContextItemSelected(item);
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

    public static boolean saveArray(ArrayList<String> array, Context mContext) { //save array to sharedprefs
        SharedPreferences prefs = mContext.getSharedPreferences("summoner_names", 0);
        SharedPreferences.Editor editor = prefs.edit();
        Set<String> set = new HashSet<>();
        set.addAll(array);
        editor.putStringSet("summoner_names", set);
        return editor.commit();
    }

    public ArrayList<String> loadArray(Context mContext) { //load array from sharedprefs
        SharedPreferences prefs = mContext.getSharedPreferences("summoner_names", 0);
        Set<String> setNames = prefs.getStringSet("summoner_names", null);
        ArrayList<String> listNames = new ArrayList<>();
        if (setNames != null)
        listNames.addAll(setNames);
        return listNames;
    }

    public static void scheduleAlarm(Context context) { //schedule regular alarm to check summoner ingame/out of game
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putStringArrayListExtra("summName", summonerNames);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES / 5, pIntent);
    }
    public void cancelAlarm(Context context) { //cancel alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
        pIntent.cancel();
    }

    public boolean alarmActive() { //check if alarm is active
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        return ((PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE)) != null);
    }

    public void updateStatus() {
        if (alarmActive()) {
            trackingStatus.setText("TRACKER ACTIVE");
            trackingStatus.setTextColor(Color.parseColor("#2196F3"));
        } else {
            trackingStatus.setText("TRACKER INACTIVE");
            trackingStatus.setTextColor(Color.parseColor("#707070"));

        }
    }

    public void checkUser(String name, OnCheckValidEndListener listener) {
        checkedName = name;
        checkValidSummoner check = new checkValidSummoner(listener);
        check.execute();

    }
    private class checkValidSummoner extends AsyncTask<String, Void, Void> {
        private final OnCheckValidEndListener listener;
        checkValidSummoner(OnCheckValidEndListener listener) {
            this.listener = listener;
        }
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... strings) {
            try {
                checkedName = RiotAPI.getSummonerByName(checkedName).toString();
            } catch (APIException e) {
                checkedName = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.onCheckValidEnd(checkedName);
        }
    }


    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(getBaseContext().INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) { //adding summoner dialog
        String user = ((TextView) dialog.getDialog().findViewById(R.id.username)).getText().toString();
        checkUser(user, new OnCheckValidEndListener() {
            @Override
            public void onCheckValidEnd(String checkedName) {
                if (checkedName == null) {
                    Log.i(TAG, "Summoner does not exist");
                    Toast.makeText(MainActivity.this, "Summoner does not exist", Toast.LENGTH_SHORT).show();

                } else {
                    Log.i(TAG, checkedName + " does exist");
                    if (summonerNames.contains(checkedName)){
                        Toast.makeText(MainActivity.this, checkedName + " is already being tracked", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        summonerNames.add(checkedName); //add summoner to list of trackers
                        saveArray(summonerNames, getApplicationContext());
                        Toast.makeText(MainActivity.this, checkedName + " has been added", Toast.LENGTH_SHORT).show();
                        Collections.sort(summonerNames, String.CASE_INSENSITIVE_ORDER);
                        listAdapter.notifyDataSetChanged(); //update listview
                        if (alarmActive()) { //if alarm active, re-set alarm
                            scheduleAlarm(getApplicationContext());
                        }
                    }
                }
            }
        });

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
    }
}

