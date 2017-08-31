package com.matejdro.taskerspotifystarter.common.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import com.matejdro.taskerspotifystarter.tasker.LocaleConstants;

public abstract class TaskerSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (!loadIntent()) {
            onFreshTaskerSetup();
        }

        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean loadIntent() {
        Intent intent = getIntent();

        if (intent == null)
            return false;

        Bundle bundle = intent.getBundleExtra(LocaleConstants.EXTRA_BUNDLE);
        if (bundle == null)
            return false;

        return onPreviousTaskerOptionsLoaded(bundle);
    }

    protected abstract boolean onPreviousTaskerOptionsLoaded(Bundle taskerOptions);

    protected void onFreshTaskerSetup() {

    }
}
