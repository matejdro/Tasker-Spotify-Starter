package com.matejdro.taskerspotifystarter.config;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.TaskerKeys;
import com.matejdro.taskerspotifystarter.executor.TaskerPlugin;

public class SearchConfigActivity extends PlayModeActivity {
    private EditText searchTermBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_configuration);
        searchTermBox = (EditText) findViewById(R.id.search_term);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadTaskerIntent();
    }

    private void loadTaskerIntent()
    {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        Bundle taskerBundle = intent.getBundleExtra("com.twofortyfouram.locale.intent.extra.BUNDLE");
        if (taskerBundle == null)
            return;

        searchTermBox.setText(taskerBundle.getString(TaskerKeys.KEY_SEARCH_TERM));
        repeatSpinner.setSelection(taskerBundle.getInt(TaskerKeys.KEY_REPEAT));
        shuffleSpinner.setSelection(taskerBundle.getInt(TaskerKeys.KEY_SHUFFLE));
    }

    private void setTaskerResult()
    {
        String searchTerm = searchTermBox.getText().toString();
        if (searchTerm.trim().isEmpty()) {
            setResult(RESULT_CANCELED);
            return;
        }

        Bundle taskerBundle = new Bundle();
        taskerBundle.putInt(TaskerKeys.KEY_ACTION, TaskerKeys.ACTION_PLAY_FROM_SEARCH);
        taskerBundle.putString(TaskerKeys.KEY_SEARCH_TERM, searchTerm);
        TaskerPlugin.Setting.setVariableReplaceKeys(taskerBundle, new String[] { TaskerKeys.KEY_SEARCH_TERM });
        taskerBundle.putInt(TaskerKeys.KEY_REPEAT, repeatSpinner.getSelectedItemPosition());
        taskerBundle.putInt(TaskerKeys.KEY_SHUFFLE, shuffleSpinner.getSelectedItemPosition());

        String description = getString(R.string.play_description, searchTerm);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", description);
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", taskerBundle);
        TaskerPlugin.Setting.requestTimeoutMS(resultIntent, 3000);

        setResult(RESULT_OK, resultIntent);
    }

    @Override
    public void onBackPressed() {
        setTaskerResult();
        finish();
    }
}
