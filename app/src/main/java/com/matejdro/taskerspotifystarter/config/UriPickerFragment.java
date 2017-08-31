package com.matejdro.taskerspotifystarter.config;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.databinding.FragmentUriPickerBinding;

public class UriPickerFragment extends StartPlaybackSetupFragment implements TextWatcher {
    private static final String ARG_SELECTED_MEDIA = "SelectedMedia";

    private String selectedMedia;

    public static UriPickerFragment create(String selectedMedia) {
        Bundle arguments = new Bundle();
        arguments.putString(ARG_SELECTED_MEDIA, selectedMedia);

        UriPickerFragment fragment = new UriPickerFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    private FragmentUriPickerBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        selectedMedia = getArguments().getString(ARG_SELECTED_MEDIA);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_uri_picker, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.confirmButton.setOnClickListener(button -> confirm());
        binding.uriBox.addTextChangedListener(this);

        binding.uriBox.setText(selectedMedia);
    }

    private void confirm() {
        String description = getString(R.string.uri_description);
        activity.onMediaPicked(binding.uriBox.getText().toString(), description);
    }


    @Override
    public void afterTextChanged(Editable editable) {
        binding.confirmButton.setEnabled(!editable.toString().trim().isEmpty());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }
}
