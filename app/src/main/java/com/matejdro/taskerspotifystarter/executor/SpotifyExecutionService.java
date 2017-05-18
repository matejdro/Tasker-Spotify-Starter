package com.matejdro.taskerspotifystarter.executor;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
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

import com.matejdro.taskerspotifystarter.TaskerKeys;

import java.util.List;

public class SpotifyExecutionService extends Service {
    public static final String TAG = "SpotifyExecutionService";

    public static final ComponentName SPOTIFY_BROWSER_COMPONENT = new ComponentName("com.spotify.music", "com.spotify.mobile.android.spotlets.androidauto.SpotifyMediaBrowserService");
    public static final String EXTRA_TASKER_INTENT = "TaskerIntent";

    private Intent taskerIntent;

    private MediaBrowserCompat mediaBrowser;
    private MediaControllerCompat mediaController;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        taskerIntent = intent.getParcelableExtra(EXTRA_TASKER_INTENT);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaBrowser = new MediaBrowserCompat(this, SpotifyExecutionService.SPOTIFY_BROWSER_COMPONENT, new BrowserConnectionCallback(), null);
        mediaBrowser.connect();
    }

    @Override
    public void onDestroy() {
        mediaBrowser.disconnect();

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startPlayback() {
        MediaControllerCompat.TransportControls transportControls = mediaController.getTransportControls();

        try {
            Bundle taskerBundle = taskerIntent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");

            int shuffleMode = taskerBundle.getInt(TaskerKeys.KEY_SHUFFLE);
            switch (shuffleMode) {
                case TaskerKeys.SHUFFLE_DISABLE:
                    transportControls.sendCustomAction("TURN_SHUFFLE_OFF", null);
                    break;
                case TaskerKeys.SHUFFLE_ENABLE:
                    transportControls.sendCustomAction("TURN_SHUFFLE_ON", null);
                    break;
            }

            int playbackAction = taskerBundle.getInt(TaskerKeys.KEY_ACTION);
            if (playbackAction == TaskerKeys.ACTION_PLAY_FROM_YOUR_MUSIC) {
                String mediaId = taskerBundle.getString(TaskerKeys.KEY_MEDIA_ID);
                transportControls.playFromMediaId(mediaId, null);
            } else if (playbackAction == TaskerKeys.ACTION_PLAY_FROM_SEARCH) {
                String searchTerm = taskerBundle.getString(TaskerKeys.KEY_SEARCH_TERM);
                transportControls.playFromSearch(searchTerm, null);
            }

            int repeatMode = taskerBundle.getInt(TaskerKeys.KEY_REPEAT);

            switch (repeatMode) {
                case TaskerKeys.REPEAT_DISABLE:
                    transportControls.sendCustomAction("TURN_REPEAT_ONE_OFF", null);
                    transportControls.sendCustomAction("TURN_REPEAT_ALL_OFF", null);
                    break;
                case TaskerKeys.REPEAT_ONE:
                    transportControls.sendCustomAction("TURN_REPEAT_ONE_ON", null);
                    break;
                case TaskerKeys.REPEAT_ALL:
                    transportControls.sendCustomAction("TURN_REPEAT_ALL_ON", null);
                    break;
            }

            finish(true);
        } catch (Exception e) {
            e.printStackTrace();
            finish(false);
        }
    }

    private void finish(boolean success) {
        int result = success ? TaskerPlugin.Setting.RESULT_CODE_OK : TaskerPlugin.Setting.RESULT_CODE_FAILED;
        TaskerPlugin.Setting.signalFinish(this, taskerIntent, result, null);
        stopSelf();
    }

    private class BrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            mediaBrowser.subscribe(mediaBrowser.getRoot(), new RootReceiveCallback());
        }

        @Override
        public void onConnectionFailed() {
            Log.e(TAG, "Connection Failed");
            finish(false);
        }
    }

    private class RootReceiveCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
            try {
                mediaController = new MediaControllerCompat(SpotifyExecutionService.this, mediaBrowser.getSessionToken());
                startPlayback();
            } catch (RemoteException e) {
                Log.e(TAG, "Controller connection failed", e);
                finish(false);
            }
        }

        @Override
        public void onError(@NonNull String parentId) {
            finish(false);
        }
    }

}
