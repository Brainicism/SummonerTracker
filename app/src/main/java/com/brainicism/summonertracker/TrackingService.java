package com.brainicism.summonertracker;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.robrua.orianna.api.core.RiotAPI;
import com.robrua.orianna.type.api.LoadPolicy;
import com.robrua.orianna.type.api.RateLimit;
import com.robrua.orianna.type.core.common.Region;
import com.robrua.orianna.type.core.currentgame.CurrentGame;
import com.robrua.orianna.type.core.summoner.Summoner;
import com.robrua.orianna.type.exception.APIException;
import com.robrua.orianna.type.exception.OriannaException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class TrackingService extends IntentService {
    private static final String TAG = "TrackingService";
    private static final int APIDownNotifID = 441;
    private static final int noConnectionNotifID = 442;

    Summoner summoner;
    com.robrua.orianna.type.core.currentgame.Participant participant;
    CurrentGame game;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    NotificationManager mNotificationManager;
    PowerManager pm;
    PowerManager.WakeLock wl;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        RiotAPI.setLoadPolicy(LoadPolicy.UPFRONT); //set up API
        RiotAPI.setRateLimit(new RateLimit(3000, 10), new RateLimit(180000, 600));
        RiotAPI.setAPIKey(MainActivity.apiKey);
        RiotAPI.setRegion(Region.NA);
        prefs = getApplicationContext().getSharedPreferences("summoner_prefs", MODE_PRIVATE);
        editor = prefs.edit();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "checkStatus"); //acquire wakelock
        wl.acquire();
        super.onCreate();
    }

    public TrackingService() {
        super("intent-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<String> summonerName = intent.getStringArrayListExtra("summName"); //retrieve array list of summoner namers

        for (int i = 0; i < summonerName.size(); i++) { //loop through each summoner name
            int count = 0;
            int maxConnectAttempt = 3;
            while (true) {
                try {
                    summoner = RiotAPI.getSummonerByName(summonerName.get(i));
                    game = RiotAPI.getCurrentGame(summoner);
                    break;
                } catch (APIException e) { //if problem with API server
                    summoner = null;
                    if (String.valueOf(e.getStatus()).equals("SERVICE_UNAVAILABLE") || String.valueOf(e.getStatus()).equals("INTERNAL_SERVER_ERROR")) {
                        buildNotificationError(e.getStatus().toString(), summonerName.get(i) + new Date().toString(), APIDownNotifID);
                        try {
                            Thread.sleep(60000); //wait one minute before attempting again
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        if (count++ == maxConnectAttempt) { //stop after max attempts
                            buildNotificationError("Maximum Attempts: " + e.getStatus().toString(), summonerName.get(i) + new Date().toString(), APIDownNotifID);
                            break;
                        }
                    }
                } catch (OriannaException e2) { //if no internet connection
                    if (e2.getLocalizedMessage().equals("Request to Riot server failed! Report this to the Orianna team.")) {
                        buildNotificationError("No internet connection: " + count, e2.getLocalizedMessage(), noConnectionNotifID);
                        try {
                            Thread.sleep(60000); //wait one minute before attempting again
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        if (count++ == maxConnectAttempt) { //stop after max attempts
                            buildNotificationError("Maximum Attempts: No internet connection", summonerName.get(i) + new Date().toString(), noConnectionNotifID);
                            break;
                        }
                    }
                }
            }
            if (summoner != null) { //if API call was successful
                mNotificationManager.cancel(APIDownNotifID);
                mNotificationManager.cancel(noConnectionNotifID);

                boolean postNotif = MainActivity.getPostNotifPref(getApplicationContext(), summoner.getName());
                Log.i(TAG, summoner.getName() + "_postNotif " + " preference: " + postNotif);
                if (game != null) { //if there is a current game
                    long prevGameID = prefs.getLong(summoner.getName() + "_prevGame", 0); //retrieve previous game from sharedprefs
                    List<com.robrua.orianna.type.core.currentgame.Participant> listParticipant = game.getParticipants();
                    for (com.robrua.orianna.type.core.currentgame.Participant gameParticipant : listParticipant) { //find specific summoner from list of participants
                        if (gameParticipant.getSummonerID() == summoner.getID())
                            participant = gameParticipant; //find Participant object of summoner
                    }
                    long currGameTime = game.getLength() + 180; //add three minutes compensation for spectator delay
                    if (game.getID() != prevGameID) { //new game has started
                        Log.i(TAG, summoner.getName() + " has started a new game: " + game.getID());
                        buildNotificationInGame(summoner.getName(), participant.getChampion().toString());
                    }
                    Log.i(TAG, summoner.getName() + " is currently in game as " + participant.getChampion() + " (" + formatTime(currGameTime) + " minutes in game)" + " " + new Date().toString());

                    editor.putLong(summoner.getName() + "_prevGame", game.getID()); //update previous game; key = summoner name, value = game ID
                    editor.commit();
                } else { //summoner not in game
                    Log.i(TAG, summoner.getName() + " is not currently in game " + new Date().toString());
                    int notifID = prefs.getInt(summoner.getName() + "_notifID", 0);
                    long lastRunID = prefs.getLong(summoner.getName() + "_lastRunID", 0);
                    mNotificationManager.cancel(notifID); //cancel current notification if game is over
                    if (lastRunID != 0) { //if they were in a game, but no longer in game

                        String queueType = prefs.getString(summoner.getName() + "_lastRunQueueType", "");
                        String champ = prefs.getString(summoner.getName() + "_lastRunChamp", "");
                        if (postNotif) { //only show post game if selected preference is selected for summoner
                            buildNotificationFinishGame(summoner.getName(), lastRunID, queueType, champ);
                            Log.i(TAG, summoner.getName() + " wanted post notifications");
                        } else {
                            Log.i(TAG, summoner.getName() + " did not want post notifications");
                        }
                    }
                }
                if (game != null) { //updates previous current game data
                    editor.putLong(summoner.getName() + "_lastRunID", game.getID());
                    editor.putString(summoner.getName() + "_lastRunQueueType", MiscMethods.normalizeQueueType(game.getQueueType().toString()));
                    editor.putString(summoner.getName() + "_lastRunChamp", participant.getChampion().toString());
                    editor.commit();
                } else {
                    editor.putLong(summoner.getName() + "_lastRunID", 0);
                    editor.commit();
                }
            }

        }
        if (wl.isHeld()) { //if wakelock is currently being held
            wl.release();
        }
    }


    public void buildNotificationInGame(String summonerName, String champ) { //build notification for currently ingame
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(bm)
                        .setPriority(1)
                        .setContentTitle("'" + summonerName + "' ingame!")
                        .setContentText("Playing as " + champ);
        mBuilder.setVibrate(new long[]{0, 250, 200, 250});
        mBuilder.setLights(Color.GREEN, 3000, 3000);
        int notifID = (int) System.currentTimeMillis(); //use current time as unique notification ID
        editor.putInt(summonerName + "_notifID", notifID);
        editor.commit();
        mNotificationManager.notify(notifID, mBuilder.build());
    }

    public void buildNotificationFinishGame(String summonerName, long gameID, String queueType, String champ) { //build notification for finished game
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(bm)
                        .setPriority(-1)
                        .setContentTitle("'" + summonerName + "' finished!");

        mBuilder.setContentText(gameID + "(" + queueType + ") " + champ);
        mBuilder.setVibrate(new long[]{0, 250, 200, 250});
        mBuilder.setLights(Color.RED, 3000, 3000);
        int notifID = (int) System.currentTimeMillis(); //use current time as unique notification ID
        editor.putInt(summonerName + "_notifIDf", notifID);
        editor.commit();
        mNotificationManager.notify(notifID, mBuilder.build());
    }

    public void buildNotificationError(String errorType, String extra, int notifID) { //build notification for finished game
        Bitmap bm = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setLargeIcon(bm)
                        .setPriority(-1)
                        .setContentTitle("Error: " + errorType);

        mBuilder.setContentText(extra);
        mBuilder.setLights(Color.RED, 3000, 3000);
        mNotificationManager.notify(notifID, mBuilder.build());
    }

    public static String formatTime(long currTime) { //format seconds to mm:ss
        int minutes = (int) currTime / 60;
        int seconds = ((int) currTime % 60);
        return String.format("%d:%02d", minutes, seconds);
    }
}
