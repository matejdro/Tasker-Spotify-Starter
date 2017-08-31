package com.matejdro.taskerspotifystarter.common.ui;

import android.support.v7.widget.RecyclerView;
import com.matejdro.taskerspotifystarter.util.RecyclerUtils;

public class BottomScrollListener extends RecyclerView.OnScrollListener {
    private BottomScrollCallback callback;

    public BottomScrollListener(BottomScrollCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (RecyclerUtils.isAtBottom(recyclerView)) {
            callback.onRecyclerReachedBottom();
        }
    }

    public interface BottomScrollCallback {
        void onRecyclerReachedBottom();
    }
}
