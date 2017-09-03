package com.matejdro.taskerspotifystarter.config;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.SpotifyConstants;
import com.matejdro.taskerspotifystarter.TaskerKeys;
import com.matejdro.taskerspotifystarter.common.ui.TaskerSetupActivity;
import com.matejdro.taskerspotifystarter.common.ui.UriReceiver;
import com.matejdro.taskerspotifystarter.config.rootcheck.RootCheckFragment;
import com.matejdro.taskerspotifystarter.config.userlibrary.LibraryItemTypeSelectorFragment;
import com.matejdro.taskerspotifystarter.config.userlibrary.LibraryListFragment;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyListType;
import com.matejdro.taskerspotifystarter.tasker.LocaleConstants;
import com.matejdro.taskerspotifystarter.tasker.TaskerPlugin;

public class StartPlaybackSetupActivity extends TaskerSetupActivity {
    private static final String INSTANCE_MEDIA_TITLE = "MediaTitle";
    private static final String INSTANCE_MEDIA_URI = "MediaUri";
    private static final String INSTANCE_MEDIA_SHUFFLE = "MediaShuffle";

    private Fragment currentFragment;

    private String currentMediaTitle = null;
    private String currentMediaUri = null;
    private boolean currentlyShuffleEnabled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentMediaTitle = savedInstanceState.getString(INSTANCE_MEDIA_TITLE);
            currentMediaUri = savedInstanceState.getString(INSTANCE_MEDIA_URI);
            currentlyShuffleEnabled = savedInstanceState.getBoolean(INSTANCE_MEDIA_SHUFFLE, false);
        }

        setContentView(R.layout.activity_tasker_config);

        if (savedInstanceState == null) {
            swapFragment(new RootCheckFragment(), false);
        }
    }

    @Override
    protected boolean onPreviousTaskerOptionsLoaded(Bundle taskerOptions) {
        currentMediaUri = taskerOptions.getString(TaskerKeys.KEY_MEDIA_URI);
        currentlyShuffleEnabled = taskerOptions.getBoolean(TaskerKeys.KEY_SHUFFLE, false);

        return currentMediaUri != null;
    }

    @Override
    protected void onFreshTaskerSetup() {

    }

    private void saveIntoTasker() {
        Intent resultIntent = new Intent();

        Bundle dataBundle = new Bundle();
        dataBundle.putString(TaskerKeys.KEY_MEDIA_URI, currentMediaUri);
        dataBundle.putBoolean(TaskerKeys.KEY_SHUFFLE, currentlyShuffleEnabled);
        TaskerPlugin.Setting.setVariableReplaceKeys(dataBundle, new String[]{TaskerKeys.KEY_MEDIA_URI});

        resultIntent.putExtra(LocaleConstants.EXTRA_STRING_BLURB, currentMediaTitle);
        resultIntent.putExtra(LocaleConstants.EXTRA_BUNDLE, dataBundle);
        TaskerPlugin.Setting.requestTimeoutMS(resultIntent, 5000);

        setResult(RESULT_OK, resultIntent);

        finish();
    }

    public void onRootCheckSuccessful() {
        swapFragment(new PickerTypeSelectorFragment(), false);
    }

    public void displayUriMediaPicker() {
        swapFragment(UriPickerFragment.create(currentMediaUri), true);
    }

    public void displayLibraryMediaPicker() {
        swapFragment(new LibraryItemTypeSelectorFragment(), true);
    }

    public void displayLibraryList(SpotifyListType listType) {
        swapFragment(LibraryListFragment.newInstance(listType), true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getData() != null) {
            if (currentFragment != null && currentFragment instanceof UriReceiver) {
                ((UriReceiver) currentFragment).onUriReceived(intent.getData());
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_MEDIA_TITLE, currentMediaTitle);
        outState.putString(INSTANCE_MEDIA_URI, currentMediaUri);
        outState.putBoolean(INSTANCE_MEDIA_SHUFFLE, currentlyShuffleEnabled);

        super.onSaveInstanceState(outState);
    }

    public void onMediaPicked(String mediaUri, String description) {
        this.currentMediaUri = mediaUri;
        this.currentMediaTitle = description;

        swapFragment(PlayModeFragment.create(description, currentlyShuffleEnabled), true);
    }

    public void onPlayModePicked(boolean shuffle) {
        this.currentlyShuffleEnabled = shuffle;

        if (isSpotifyBroadcastStatusEnabled()) {
            saveIntoTasker();
        } else {
            displaySpotifyBroadcastStatusNotice();
        }
    }

    public void errorToPickerTypeSelector(String errorDescription) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(errorDescription)
                .setPositiveButton(android.R.string.ok, null)
                .setOnCancelListener(dialogInterface -> onRootCheckSuccessful())
                .setOnDismissListener(dialogInterface -> onRootCheckSuccessful())
                .show();
    }

    public void exitError(String errorDescription) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(errorDescription)
                .setPositiveButton(android.R.string.ok, null)
                .setOnCancelListener(dialogInterface -> exitError())
                .setOnDismissListener(dialogInterface -> exitError())
                .show();
    }

    public void exitError() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void displaySpotifyBroadcastStatusNotice() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.notice)
                .setMessage(R.string.notice_enable_broadcast_status)
                .setPositiveButton(android.R.string.ok, null)
                .setOnCancelListener(dialogInterface -> saveIntoTasker())
                .setOnDismissListener(dialogInterface -> saveIntoTasker())
                .show();

    }

    private void swapFragment(Fragment fragment, boolean backStack) {
        @SuppressLint("CommitTransaction")
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (backStack) {
            transaction.addToBackStack(fragment.getClass().getName());
        }

        transaction.commit();

        this.currentFragment = fragment;
    }

    private boolean isSpotifyBroadcastStatusEnabled() {
        IntentFilter intentFilter = new IntentFilter(SpotifyConstants.ACTION_SPOTIFY_PLAYBACK_STATE_INTENT);
        return registerReceiver(null, intentFilter) != null;
    }
}
