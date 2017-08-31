package com.matejdro.taskerspotifystarter.config;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.databinding.FragmentPickerTypeBinding;

public class PickerTypeSelectorFragment extends StartPlaybackSetupFragment {
    private FragmentPickerTypeBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_picker_type, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.buttonFromUri.setOnClickListener(button -> activity.displayUriMediaPicker());
        binding.buttonFromLibrary.setOnClickListener(button -> activity.displayLibraryMediaPicker());
    }
}
