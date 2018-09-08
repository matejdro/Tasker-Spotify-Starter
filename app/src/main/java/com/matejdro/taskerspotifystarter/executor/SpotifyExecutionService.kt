package com.matejdro.taskerspotifystarter.executor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.matejdro.taskerspotifystarter.BuildConfig
import com.matejdro.taskerspotifystarter.R
import com.matejdro.taskerspotifystarter.SpotifyConstants
import com.matejdro.taskerspotifystarter.TaskerKeys
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyUriConverter
import com.matejdro.taskerspotifystarter.tasker.TaskerPlugin
import com.matejdro.taskerspotifystarter.util.ExceptionUtils
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch


class SpotifyExecutionService : Service() {
    private lateinit var taskerIntent: Intent

    private val parentJob = Job()

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        val foregroundNotification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_SERVICE)
                .setContentTitle(getString(R.string.spotify_execution))
                .setContentText(getString(R.string.starting_your_content))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

        startForeground(1000, foregroundNotification)
    }

    override fun onDestroy() {
        super.onDestroy()

        parentJob.cancel()
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        this.taskerIntent = intent.getParcelableExtra(EXTRA_TASKER_INTENT)

        launch(UI, parent = parentJob) {
            startPlayback()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun startPlayback() {
        try {
            val taskerBundle = taskerIntent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE")
            println()

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

val connectionParams = ConnectionParams.Builder(BuildConfig.SPOTIFY_API_KEY)
        .setRedirectUri(SpotifyConstants.REDIRECT_URI)
        .showAuthView(true)
        .build()

            println("Connect $uri")

            SpotifyAppRemote.CONNECTOR.connectAndAwait(this,
                    connectionParams).use { spotifyRemote ->
                with(spotifyRemote.playerApi) {
                    println("Shuffle")
                    setShuffle(taskerBundle.getBoolean(TaskerKeys.KEY_SHUFFLE)).awaitSuspending()
                    println("Play")
                    play(uri).awaitSuspending()
                }
            }


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
