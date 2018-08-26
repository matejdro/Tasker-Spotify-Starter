package com.matejdro.taskerspotifystarter.executor

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.matejdro.taskerspotifystarter.R
import com.matejdro.taskerspotifystarter.SpotifyConstants
import com.matejdro.taskerspotifystarter.TaskerKeys
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyUriConverter
import com.matejdro.taskerspotifystarter.tasker.TaskerPlugin
import com.matejdro.taskerspotifystarter.util.ExceptionUtils

class SpotifyExecutionService : IntentService(TAG) {

    private var taskerIntent: Intent? = null

    @Volatile
    private var spotifyPlaying = false

    private val spotifyStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            spotifyPlaying = intent.getBooleanExtra("playstate", false)
        }
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val foregroundNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SERVICE)
                .setContentTitle(getString(R.string.spotify_execution))
                .setContentText(getString(R.string.starting_your_content))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

        startForeground(1000, foregroundNotification)

        registerReceiver(spotifyStateReceiver, IntentFilter(SpotifyConstants.ACTION_SPOTIFY_PLAYBACK_STATE_INTENT))
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(spotifyStateReceiver)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_SERVICE,
                getString(R.string.spotify_execution),
                NotificationManager.IMPORTANCE_MIN
        )

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null) {
            return
        }

        this.taskerIntent = intent.getParcelableExtra(EXTRA_TASKER_INTENT)
        startPlayback()
    }

    private fun startPlayback() {
        try {
            val taskerBundle = taskerIntent!!.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")

            var uri = taskerBundle.getString(TaskerKeys.KEY_MEDIA_URI)
            if (uri == null) {
                finishError(getString(R.string.error_invalid_action))
                return
            }

            uri = SpotifyUriConverter.toContentUri(uri)
            if (uri == null) {
                finishError(getString(R.string.error_invalid_spotify_uri))
                return
            }

            val suCmdBuilder = StringBuilder()
            suCmdBuilder.append("am startservice -a com.spotify.mobile.android.service.action.player.PLAY_CONTENT ")
            suCmdBuilder.append("-d ")
            suCmdBuilder.append(uri)
            suCmdBuilder.append(' ')

            val shuffleMode = taskerBundle.getBoolean(TaskerKeys.KEY_SHUFFLE)
            if (shuffleMode) {
                suCmdBuilder.append("--ez shuffle true ")
            } else {
                suCmdBuilder.append("--ez shuffle false ")
            }

            suCmdBuilder.append("com.spotify.music/com.spotify.mobile.android.service.SpotifyService")

            val suCommandLine = suCmdBuilder.toString()

            var args = arrayOf("su", "-c", suCommandLine)
            var process = Runtime.getRuntime().exec(args)
            process.waitFor()

            Thread.sleep(2000)

            if (!spotifyPlaying) {
                // When spotify is not running, it can take two tries to restart it back up

                args = arrayOf("su", "-c", suCommandLine)
                process = Runtime.getRuntime().exec(args)
                process.waitFor()
            }

            val putInForegroundCmdLine = "am startservice -a " +
                    "com.spotify.mobile.android.service.action.client.FOREGROUND " +
                    "com.spotify.music/com.spotify.mobile.android.service.SpotifyService"

            args = arrayOf("su", "-c", putInForegroundCmdLine)
            process = Runtime.getRuntime().exec(args)
            process.waitFor()

            finishOK()
        } catch (e: Exception) {
            e.printStackTrace()
            finishError(e)
        }

    }

    private fun finishError(exception: Exception) {
        finishError(ExceptionUtils.getNestedExceptionMessages(exception))
    }

    private fun finishError(errorMessage: String) {
        val vars = Bundle()
        vars.putString(TaskerPlugin.Setting.VARNAME_ERROR_MESSAGE, errorMessage)

        TaskerPlugin.Setting.signalFinish(this, taskerIntent, TaskerPlugin.Setting.RESULT_CODE_FAILED, vars)
    }

    private fun finishOK() {
        TaskerPlugin.Setting.signalFinish(this, taskerIntent, TaskerPlugin.Setting.RESULT_CODE_OK, null)
    }

    companion object {
        val TAG = "SpotifyExecutionService"

        val EXTRA_TASKER_INTENT = "TaskerIntent"

        private val NOTIFICATION_CHANNEL_SERVICE = "ExecutionService"
    }
}
