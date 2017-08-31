package com.matejdro.taskerspotifystarter.executor;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.SpotifyConstants;
import com.matejdro.taskerspotifystarter.TaskerKeys;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyUriConverter;
import com.matejdro.taskerspotifystarter.tasker.TaskerPlugin;
import com.matejdro.taskerspotifystarter.util.ExceptionUtils;

public class SpotifyExecutionService extends IntentService {
    public static final String TAG = "SpotifyExecutionService";

    public static final String EXTRA_TASKER_INTENT = "TaskerIntent";

    private Intent taskerIntent;

    private volatile boolean spotifyPlaying = false;

    public SpotifyExecutionService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        registerReceiver(spotifyStateReceiver, new IntentFilter(SpotifyConstants.ACTION_SPOTIFY_PLAYBACK_STATE_INTENT));
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
        try {
            Bundle taskerBundle = taskerIntent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");

            String uri = taskerBundle.getString(TaskerKeys.KEY_MEDIA_URI);
            if (uri == null) {
                finishError(getString(R.string.error_invalid_action));
                return;
            }

            uri = SpotifyUriConverter.toContentUri(uri);
            if (uri == null) {
                finishError(getString(R.string.error_invalid_spotify_uri));
                return;
            }

            StringBuilder suCmdBuilder = new StringBuilder();
            suCmdBuilder.append("am startservice -a com.spotify.mobile.android.service.action.player.PLAY_CONTENT ");
            suCmdBuilder.append("-d ");
            suCmdBuilder.append(uri);
            suCmdBuilder.append(' ');

            boolean shuffleMode = taskerBundle.getBoolean(TaskerKeys.KEY_SHUFFLE);
            if (shuffleMode) {
                suCmdBuilder.append("--ez shuffle true ");
            } else {
                suCmdBuilder.append("--ez shuffle false ");
            }

            suCmdBuilder.append("com.spotify.music/com.spotify.mobile.android.service.SpotifyService");

            String suCommandLine = suCmdBuilder.toString();

            String[] args = new String[]{"su", "-c", suCommandLine};
            Process process = Runtime.getRuntime().exec(args);
            process.waitFor();

            Thread.sleep(2000);

            if (!spotifyPlaying) {
                // When spotify is not running, it can take two tries to restart it back up

                args = new String[]{"su", "-c", suCommandLine};
                process = Runtime.getRuntime().exec(args);
                process.waitFor();
            }

            String putInForegroundCmdLine = "am startservice -a " +
                    "com.spotify.mobile.android.service.action.client.FOREGROUND " +
                    "com.spotify.music/com.spotify.mobile.android.service.SpotifyService";

            args = new String[]{"su", "-c", putInForegroundCmdLine};
            process = Runtime.getRuntime().exec(args);
            process.waitFor();

            finishOK();
        } catch (Exception e) {
            e.printStackTrace();
            finishError(e);
        }
    }

    private void finishError(Exception exception) {
        finishError(ExceptionUtils.getNestedExceptionMessages(exception));
    }

    private void finishError(String errorMessage) {
        Bundle vars = new Bundle();
        vars.putString(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE, errorMessage);

        TaskerPlugin.Setting.signalFinish(this, taskerIntent, TaskerPlugin.Setting.RESULT_CODE_FAILED, vars);
    }

    private void finishOK() {
        TaskerPlugin.Setting.signalFinish(this, taskerIntent, TaskerPlugin.Setting.RESULT_CODE_OK, null);
    }

    private BroadcastReceiver spotifyStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            spotifyPlaying = intent.getBooleanExtra("playstate", false);
        }
    };
}
