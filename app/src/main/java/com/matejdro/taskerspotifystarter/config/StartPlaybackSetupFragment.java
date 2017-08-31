package com.matejdro.taskerspotifystarter.config;

import android.arch.lifecycle.LifecycleFragment;
import android.content.Context;
import android.support.annotation.CallSuper;

public class StartPlaybackSetupFragment extends LifecycleFragment {
    protected StartPlaybackSetupActivity activity;

    @Override
    @CallSuper
    public void onAttach(Context context) {
        super.onAttach(context);

        this.activity = (StartPlaybackSetupActivity) context;
    }

}
