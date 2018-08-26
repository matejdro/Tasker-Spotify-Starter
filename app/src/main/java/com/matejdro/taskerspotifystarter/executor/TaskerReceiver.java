package com.matejdro.taskerspotifystarter.executor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;

import com.matejdro.taskerspotifystarter.tasker.TaskerPlugin;

public class TaskerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (isOrderedBroadcast()) {
            setResultCode(TaskerPlugin.Setting.RESULT_CODE_PENDING);
        }

        Intent serviceIntent = new Intent(context, SpotifyExecutionService.class);
        serviceIntent.putExtra(SpotifyExecutionService.Companion.getEXTRA_TASKER_INTENT(), intent);
        ContextCompat.startForegroundService(context, serviceIntent);
    }
}
