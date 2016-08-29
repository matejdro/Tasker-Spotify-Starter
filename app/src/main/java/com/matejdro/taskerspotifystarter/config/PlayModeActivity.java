package com.matejdro.taskerspotifystarter.config;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.matejdro.taskerspotifystarter.R;

public abstract class PlayModeActivity extends AppCompatActivity {
    protected AppCompatSpinner shuffleSpinner;
    protected AppCompatSpinner repeatSpinner;

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        shuffleSpinner = (AppCompatSpinner) findViewById(R.id.shuffle_spinner);
        shuffleSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.shuffle_modes)));
        repeatSpinner = (AppCompatSpinner) findViewById(R.id.repeat_spinner);
        repeatSpinner.setAdapter(new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, getResources().getStringArray(R.array.repeat_modes)));
    }
}
