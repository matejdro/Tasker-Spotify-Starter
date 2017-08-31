
package com.matejdro.taskerspotifystarter.config;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.databinding.FragmentPlayModeBinding;

public class PlayModeFragment extends StartPlaybackSetupFragment {
    private static final String ARG_SELECTED_MEDIA = "SelectedMedia";
    private static final String ARG_SHUFFLE_ENABLED = "ShuffleEnabled";

    private FragmentPlayModeBinding binding;

    private String selectedMedia;
    private boolean shuffleEnabled;

    public static PlayModeFragment create(String selectedMedia, boolean shuffleInitialState) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_SELECTED_MEDIA, selectedMedia);
        arguments.putBoolean(ARG_SHUFFLE_ENABLED, shuffleInitialState);

        PlayModeFragment fragment = new PlayModeFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedMedia = getArguments().getString(ARG_SELECTED_MEDIA);
        shuffleEnabled = getArguments().getBoolean(ARG_SHUFFLE_ENABLED);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_play_mode, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.pickedMediaItem.setText(selectedMedia);
        binding.shuffleCheckbox.setChecked(shuffleEnabled);

        binding.saveButton.setOnClickListener(button -> activity.onPlayModePicked(binding.shuffleCheckbox.isChecked()));
    }
}
