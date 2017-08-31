package com.matejdro.taskerspotifystarter.config.userlibrary;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.common.ui.SpotifyLoginFragment;
import com.matejdro.taskerspotifystarter.config.StartPlaybackSetupActivity;
import com.matejdro.taskerspotifystarter.databinding.FragmentLibraryItemTypeBinding;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyListType;

public class LibraryItemTypeSelectorFragment extends SpotifyLoginFragment {
    private FragmentLibraryItemTypeBinding binding;
    private StartPlaybackSetupActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_library_item_type, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSongs.setOnClickListener(button -> activity.displayLibraryList(SpotifyListType.TRACKS));
        binding.buttonAlbums.setOnClickListener(button -> activity.displayLibraryList(SpotifyListType.ALBUMS));
        binding.buttonPlaylists.setOnClickListener(button -> activity.displayLibraryList(SpotifyListType.PLAYLIST));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.activity = (StartPlaybackSetupActivity) context;
    }

    @Override
    protected void onLoginSuccess() {
        binding.loginIndicator.setVisibility(View.GONE);
        binding.userItems.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onLoginCancelled() {
        activity.errorToPickerTypeSelector(getString(R.string.error_login_cancelled));
    }

    @Override
    protected void onLoginFailed() {
        activity.errorToPickerTypeSelector(getString(R.string.error_login_failed));
    }
}
