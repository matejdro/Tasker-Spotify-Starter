package com.matejdro.taskerspotifystarter.config.rootcheck;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.config.StartPlaybackSetupFragment;
import com.matejdro.taskerspotifystarter.util.Resource;

public class RootCheckFragment extends StartPlaybackSetupFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RootCheckViewModel viewModel = ViewModelProviders.of(this).get(RootCheckViewModel.class);
        viewModel.getRootStatusLivedata().observe(this, rootStatusObserver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_root_check, container, false);
    }


    private void completeRootCheck() {
        activity.onRootCheckSuccessful();
    }

    private Observer<Resource<Boolean>> rootStatusObserver = rootStatus -> {
        if (rootStatus == null) {
            return;
        }

        if (rootStatus.status == Resource.Status.ERROR) {
            assert rootStatus.error != null;
            activity.exitError(rootStatus.error.getMessage());
        } else if (rootStatus.status == Resource.Status.SUCCESS) {
            if (Boolean.TRUE.equals(rootStatus.data)) {
                completeRootCheck();
            } else {
                activity.exitError(getString(R.string.error_no_root));
            }
        }
    };
}
