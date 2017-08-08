package com.matejdro.taskerspotifystarter.executor;

import android.app.IntentService;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.matejdro.taskerspotifystarter.TaskerKeys;

import java.util.ArrayList;
import java.util.List;

public class SpotifyExecutionService extends IntentService {
    public static final String TAG = "SpotifyExecutionService";

    public static final ComponentName SPOTIFY_BROWSER_COMPONENT = new ComponentName("com.spotify.music", "com.spotify.mobile.android.spotlets.androidauto.SpotifyMediaBrowserService");
    public static final String EXTRA_TASKER_INTENT = "TaskerIntent";

    public static final String ACTION_SPOTIFY_PLAYBACK_STATE_INTENT = "com.spotify.music.playbackstatechanged";

    private Intent taskerIntent;

    private volatile boolean spotifyPlaying = false;

    public SpotifyExecutionService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(spotifyStateReceiver, new IntentFilter(ACTION_SPOTIFY_PLAYBACK_STATE_INTENT));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(spotifyStateReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        this.taskerIntent = intent.getParcelableExtra(EXTRA_TASKER_INTENT);
        startPlayback();
    }

    private void startPlayback() {
        //TODO Search support
        //TODO Repeat support

        try {
            Bundle taskerBundle = taskerIntent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");

            String uri = taskerBundle.getString(TaskerKeys.KEY_MEDIA_ID);
            if (uri == null) {
                finish(false);
                return;
            }



            StringBuilder suCmdBuilder = new StringBuilder();
            suCmdBuilder.append("am startservice -a com.spotify.mobile.android.service.action.player.PLAY_CONTENT ");
            suCmdBuilder.append("-d ");
            suCmdBuilder.append(uri);
            suCmdBuilder.append(' ');

            int shuffleMode = taskerBundle.getInt(TaskerKeys.KEY_SHUFFLE);
            switch (shuffleMode) {
                case TaskerKeys.SHUFFLE_DISABLE:
                    suCmdBuilder.append("--ez shuffle false ");
                    break;
                case TaskerKeys.SHUFFLE_ENABLE:
                    suCmdBuilder.append("--ez shuffle true ");
                    break;
            }

            suCmdBuilder.append("com.spotify.music/com.spotify.mobile.android.service.SpotifyService");

            String suCommandLine = suCmdBuilder.toString();

            String[] args = new String[] {"su", "-c", suCommandLine };
            Process process = Runtime.getRuntime().exec(args);
            process.waitFor();

            Thread.sleep(2000);

            if (!spotifyPlaying) {
                // When spotify is not running, it can take two tries to restart it back up

                args = new String[] {"su", "-c", suCommandLine };
                process = Runtime.getRuntime().exec(args);
                process.waitFor();
            }

            String putInForegroundCmdLine = "am startservice -a " +
                    "com.spotify.mobile.android.service.action.client.FOREGROUND " +
                    "com.spotify.music/com.spotify.mobile.android.service.SpotifyService";

            args = new String[] {"su", "-c", putInForegroundCmdLine };
            process = Runtime.getRuntime().exec(args);
            process.waitFor();

            finish(true);
        } catch (Exception e) {
            e.printStackTrace();
            finish(false);
        }
    }

    private void finish(boolean success) {
        int result = success ? TaskerPlugin.Setting.RESULT_CODE_OK : TaskerPlugin.Setting.RESULT_CODE_FAILED;
        TaskerPlugin.Setting.signalFinish(this, taskerIntent, result, null);
    }

    private BroadcastReceiver spotifyStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            spotifyPlaying = intent.getBooleanExtra("playstate", false);
        }
    };
}
