package com.matejdro.taskerspotifystarter.config;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.matejdro.taskerspotifystarter.util.DividerItemDecoration;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.TaskerKeys;
import com.matejdro.taskerspotifystarter.executor.SpotifyExecutionService;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class YourMusicItemPickerActivity extends AppCompatActivity {
    private static final String TAG = "SpotifyPicker";

    private ProgressBar loadingBar;
    private RecyclerView recycler;
    private View xposedError;
    private View noItemsNotice;

    private MediaBrowserCompat mediaBrowser;
    private List<MediaBrowserCompat.MediaItem> mediaItems = Collections.emptyList();
    private SpotifyItemsAdapter itemsAdapter;

    private Stack<String> itemsStack = new Stack<>();
    private String currentItemId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_music_item_picker);

        loadingBar = (ProgressBar) findViewById(R.id.progress);
        xposedError = findViewById(R.id.xposed_error);
        noItemsNotice = findViewById(R.id.no_items_notice);

        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.addItemDecoration(new DividerItemDecoration(this));
        itemsAdapter = new SpotifyItemsAdapter();
        recycler.setAdapter(itemsAdapter);

        if (savedInstanceState != null) {
            currentItemId = savedInstanceState.getString("CurrentList");

            String[] backStackArray = savedInstanceState.getStringArray("BackStack");
            for (String item : backStackArray)
                itemsStack.push(item);

        } else {
            currentItemId = "com.matejdro.taskerspotifystarter---your_music";
        }

        mediaBrowser = new MediaBrowserCompat(this, SpotifyExecutionService.SPOTIFY_BROWSER_COMPONENT, new ConnectionCallback(), null);
        mediaBrowser.connect();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putStringArray("BackStack", itemsStack.toArray(new String[itemsStack.size()]));
        outState.putString("CurrentList", currentItemId);
    }

    @Override
    protected void onDestroy() {
        mediaBrowser.disconnect();

        super.onDestroy();
    }

    private void showItems() {
        loadingBar.setVisibility(View.GONE);

        if (mediaItems.isEmpty()) {
            recycler.setVisibility(View.GONE);
            noItemsNotice.setVisibility(View.VISIBLE);
        } else {
            recycler.setVisibility(View.VISIBLE);
            noItemsNotice.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        loadingBar.setVisibility(View.VISIBLE);
        recycler.setVisibility(View.GONE);
        noItemsNotice.setVisibility(View.GONE);
        xposedError.setVisibility(View.GONE);
    }

    private void showError() {
        loadingBar.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);
        noItemsNotice.setVisibility(View.GONE);
        xposedError.setVisibility(View.VISIBLE);
    }

    private void loadItems(String listId) {
        if (!mediaBrowser.isConnected()) {
            return;
        }

        currentItemId = listId;
        mediaBrowser.subscribe(listId, new ItemsCallback());
        showLoading();
    }

    private void itemSelected(int position) {
        MediaBrowserCompat.MediaItem selectedItem = mediaItems.get(position);

        if (selectedItem.isBrowsable()) {
            itemsStack.add(currentItemId);
            loadItems(selectedItem.getMediaId());
        }
        else if (selectedItem.isPlayable()) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(TaskerKeys.KEY_MEDIA_ID, selectedItem.getMediaId());
            resultIntent.putExtra(TaskerKeys.KEY_MEDIA_TITLE, selectedItem.getDescription().getTitle());

            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (itemsStack.isEmpty()) {
            super.onBackPressed();
        } else {
            loadItems(itemsStack.pop());
        }
    }

    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            loadItems(currentItemId);
        }

        @Override
        public void onConnectionFailed() {
            Log.e(TAG, "Connection Failed");

            showError();
        }
    }

    private class ItemsCallback extends MediaBrowserCompat.SubscriptionCallback {
        @Override
        public void onChildrenLoaded(@NonNull String parentId, List<MediaBrowserCompat.MediaItem> children) {
            mediaBrowser.unsubscribe(parentId);

            Log.d(TAG, "Loaded " + parentId);

            mediaItems = children;
            itemsAdapter.notifyDataSetChanged();
            showItems();
        }

        @Override
        public void onError(@NonNull String parentId) {
            Log.e(TAG, "Parent Error");

            mediaBrowser.unsubscribe(parentId);
            showError();
        }
    }

    private class SpotifyItemsAdapter extends RecyclerView.Adapter<SpotifyItemHolder> {
        @Override
        public SpotifyItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_music_browser, parent, false);
            return new SpotifyItemHolder(view);
        }

        @Override
        public void onBindViewHolder(SpotifyItemHolder holder, int position) {
            MediaBrowserCompat.MediaItem mediaItem = mediaItems.get(position);
            MediaDescriptionCompat mediaItemDescription = mediaItem.getDescription();

            holder.title.setText(mediaItemDescription.getTitle());
            Picasso.with(YourMusicItemPickerActivity.this).load(mediaItemDescription.getIconUri()).into(holder.icon);

            CharSequence subtitle = mediaItemDescription.getSubtitle();
            holder.subtitle.setText(subtitle);
            holder.subtitle.setVisibility(TextUtils.isEmpty(subtitle) ? View.GONE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mediaItems.size();
        }
    }

    private class SpotifyItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView icon;
        public TextView title;
        public TextView subtitle;

        public SpotifyItemHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.item_image);
            title = (TextView) itemView.findViewById(R.id.item_title);
            subtitle = (TextView) itemView.findViewById(R.id.item_subtitle);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            itemSelected(getAdapterPosition());
        }
    }
}
