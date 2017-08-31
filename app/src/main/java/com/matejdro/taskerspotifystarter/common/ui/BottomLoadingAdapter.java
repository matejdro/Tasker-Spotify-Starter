package com.matejdro.taskerspotifystarter.common.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.matejdro.taskerspotifystarter.R;

/**
 * Adapter that displays loading animation on the bottom when required
 */
public abstract class BottomLoadingAdapter<VH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;

    public BottomLoadingAdapter(Context context) {
        this.context = context;
    }

    private static final int VIEW_TYPE_CONTENT = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private boolean loadingDisplayed = false;

    public boolean isLoadingDisplayed() {
        return loadingDisplayed;
    }

    public void setLoadingDisplayed(boolean loadingDisplayed) {
        if (this.loadingDisplayed == loadingDisplayed) {
            return;
        }

        this.loadingDisplayed = loadingDisplayed;

        if (loadingDisplayed) {
            notifyItemInserted(getContentItemCount());
        } else {
            notifyItemRemoved(getContentItemCount());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position >= getContentItemCount() ? VIEW_TYPE_LOADING : VIEW_TYPE_CONTENT;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_CONTENT) {
            //noinspection unchecked
            onBindContentViewHolder((VH) holder, position);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CONTENT) {
            //noinspection unchecked
            return onCreateContentViewHolder(parent);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public int getItemCount() {
        int count = getContentItemCount();
        if (loadingDisplayed) {
            count++;
        }

        return count;
    }

    protected abstract VH onCreateContentViewHolder(ViewGroup parent);

    protected abstract void onBindContentViewHolder(VH viewHolder, int position);

    protected abstract int getContentItemCount();

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }
}
