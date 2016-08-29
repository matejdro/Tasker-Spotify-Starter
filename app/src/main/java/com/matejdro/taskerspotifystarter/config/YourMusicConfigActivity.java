package com.matejdro.taskerspotifystarter.config;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.TaskerKeys;
import com.matejdro.taskerspotifystarter.executor.TaskerPlugin;

public class YourMusicConfigActivity extends PlayModeActivity {
    private TextView pickedMediaTitleTextView;

    private int REQUEST_ID_PICK_MEDIA_ITEM = 0;

    private String pickedMediaItemTitle;
    private String pickedMediaItemId;

    private boolean itemPickRequired = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_your_music_configuration);

        pickedMediaTitleTextView = (TextView) findViewById(R.id.picked_media_item);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        loadTaskerIntent();
        if (itemPickRequired) {
            pickMediaItem();
        }
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

        pickedMediaItemTitle = taskerBundle.getString(TaskerKeys.KEY_MEDIA_TITLE);
        pickedMediaItemId = taskerBundle.getString(TaskerKeys.KEY_MEDIA_ID);
        repeatSpinner.setSelection(taskerBundle.getInt(TaskerKeys.KEY_REPEAT));
        shuffleSpinner.setSelection(taskerBundle.getInt(TaskerKeys.KEY_SHUFFLE));
        pickedMediaTitleTextView.setText(pickedMediaItemTitle);
        itemPickRequired = false;
    }

    private void setTaskerOkResult()
    {
        Bundle taskerBundle = new Bundle();
        taskerBundle.putInt(TaskerKeys.KEY_ACTION, TaskerKeys.ACTION_PLAY_FROM_YOUR_MUSIC);
        taskerBundle.putString(TaskerKeys.KEY_MEDIA_TITLE, pickedMediaItemTitle);
        taskerBundle.putString(TaskerKeys.KEY_MEDIA_ID, pickedMediaItemId);
        taskerBundle.putInt(TaskerKeys.KEY_REPEAT, repeatSpinner.getSelectedItemPosition());
        taskerBundle.putInt(TaskerKeys.KEY_SHUFFLE, shuffleSpinner.getSelectedItemPosition());

        String description = getString(R.string.play_description, pickedMediaItemTitle);

        Intent resultIntent = new Intent();
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BLURB", description);
        resultIntent.putExtra("com.twofortyfouram.locale.intent.extra.BUNDLE", taskerBundle);
        TaskerPlugin.Setting.requestTimeoutMS(resultIntent, 3000);

        setResult(RESULT_OK, resultIntent);
    }

    private void pickMediaItem()
    {
        startActivityForResult(new Intent(this, YourMusicItemPickerActivity.class), REQUEST_ID_PICK_MEDIA_ITEM);
    }

    public void changeMediaItem(View view) {
        pickMediaItem();
    }

    @Override
    public void onBackPressed() {
        setTaskerOkResult();
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ID_PICK_MEDIA_ITEM) {
            if (resultCode == RESULT_OK) {
                pickedMediaItemTitle = data.getStringExtra(TaskerKeys.KEY_MEDIA_TITLE);
                pickedMediaItemId = data.getStringExtra(TaskerKeys.KEY_MEDIA_ID);

                pickedMediaTitleTextView.setText(pickedMediaItemTitle);
                itemPickRequired = false;
            }
            else if (itemPickRequired) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
