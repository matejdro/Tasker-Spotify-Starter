package com.matejdro.taskerspotifystarter.config.userlibrary;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.matejdro.taskerspotifystarter.R;
import com.matejdro.taskerspotifystarter.common.ui.BottomLoadingAdapter;
import com.matejdro.taskerspotifystarter.common.ui.BottomScrollListener;
import com.matejdro.taskerspotifystarter.config.StartPlaybackSetupFragment;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyListType;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import com.matejdro.taskerspotifystarter.util.DividerItemDecoration;
import com.matejdro.taskerspotifystarter.util.RecyclerUtils;
import com.matejdro.taskerspotifystarter.util.Resource;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

public class LibraryListFragment extends StartPlaybackSetupFragment implements BottomScrollListener.BottomScrollCallback {

    private static final String EXTRA_LIST_TYPE = "ListType";

    private SpotifyListType listType;
    private LibraryListViewModel viewModel;
    private RecyclerView recycler;
    private ItemAdapter adapter;

    private List<SpotifyPlaybackItem> items = Collections.emptyList();

    public static LibraryListFragment newInstance(SpotifyListType listType) {
        Bundle arguments = new Bundle();
        arguments.putSerializable(EXTRA_LIST_TYPE, listType);

        LibraryListFragment fragment = new LibraryListFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listType = (SpotifyListType) getArguments().getSerializable(EXTRA_LIST_TYPE);
        ViewModelProvider.Factory vmFactory = new LibraryListViewModel.Factory(
                listType, getActivity().getApplication());
        viewModel = ViewModelProviders.of(this, vmFactory).get(LibraryListViewModel.class);

        viewModel.getItemsLiveData().observe(this, listObserver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recycler = view.findViewById(R.id.recycler);

        adapter = new ItemAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recycler.setAdapter(adapter);
        recycler.addItemDecoration(new DividerItemDecoration(getContext()));
        recycler.addOnScrollListener(new BottomScrollListener(this));
    }

    private void acceptItem(SpotifyPlaybackItem playbackItem) {
        String description = getString(listType.getDescriptionResource(), playbackItem.getTitle());

        activity.onMediaPicked(playbackItem.getContentUri(), description);
    }

    private Observer<Resource<List<SpotifyPlaybackItem>>> listObserver = listResource -> {
        if (listResource == null) {
            return;
        }

        if (listResource.status == Resource.Status.LOADING) {
            adapter.setLoadingDisplayed(true);
        } else if (listResource.status == Resource.Status.ERROR) {
            assert listResource.error != null;
            activity.errorToPickerTypeSelector(listResource.error.getMessage());
        } else {
            adapter.setLoadingDisplayed(false);
            items = listResource.data;
            adapter.notifyDataSetChanged();

            if (RecyclerUtils.isAtBottom(recycler)) {
                onRecyclerReachedBottom();
            }
        }
    };

    @Override
    public void onRecyclerReachedBottom() {
        if (!adapter.isLoadingDisplayed()) {
            viewModel.loadNextPage();
        }
    }

    private class ItemAdapter extends BottomLoadingAdapter<ItemViewHolder> {
        public ItemAdapter() {
            super(getContext());
        }

        @Override
        protected ItemViewHolder onCreateContentViewHolder(ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.item_playback, parent, false);
            return new ItemViewHolder(view);
        }

        @Override
        protected void onBindContentViewHolder(ItemViewHolder viewHolder, int position) {
            SpotifyPlaybackItem playbackItem = items.get(position);

            viewHolder.textView.setText(playbackItem.getTitle());
            if (TextUtils.isEmpty(playbackItem.getImageUrl())) {
                Picasso.with(getContext()).cancelRequest(viewHolder.imageView);
                viewHolder.imageView.setImageResource(R.drawable.ic_playlist);
            } else {
                Picasso.with(getContext())
                        .load(Uri.parse(playbackItem.getImageUrl()))
                        .placeholder(R.drawable.ic_playlist)
                        .into(viewHolder.imageView);
            }
        }

        @Override
        protected int getContentItemCount() {
            return items.size();
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;
        private TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.icon);
            textView = itemView.findViewById(R.id.title);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            acceptItem(items.get(getAdapterPosition()));
        }
    }
}
