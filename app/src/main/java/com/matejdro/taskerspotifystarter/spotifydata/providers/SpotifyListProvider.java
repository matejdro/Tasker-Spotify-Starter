package com.matejdro.taskerspotifystarter.spotifydata.providers;

import android.content.Context;
import android.support.annotation.Nullable;

import com.jakewharton.rxrelay2.PublishRelay;
import com.matejdro.taskerspotifystarter.spotifydata.CredentialStore;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyListType;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import com.matejdro.taskerspotifystarter.util.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Image;

public abstract class SpotifyListProvider<T> {
    protected SpotifyApi spotifyApi;

    private PublishRelay<Resource<List<SpotifyPlaybackItem>>> resultRelay;
    private List<SpotifyPlaybackItem> items = new ArrayList<>();


    public SpotifyListProvider(Context context) {
        this.spotifyApi = new SpotifyApi();
        spotifyApi.setAccessToken(CredentialStore.getInstance(context).getSpotifyToken());

        this.resultRelay = PublishRelay.create();
    }

    public void requestListStart() {
        resultRelay.accept(Resource.loading(items));

        provideListStart()
                .subscribeOn(Schedulers.io())
                .map(this::mapList)
                .doOnSuccess(spotifyPlaybackItems -> this.items = spotifyPlaybackItems)
                .compose(this::subscribeToRelay)
                .subscribe();
    }

    public void requestNextPage() {
        resultRelay.accept(Resource.loading(items));

        provideListContinuation()
                .subscribeOn(Schedulers.io())
                .map(this::mapList)
                .map(spotifyPlaybackItems -> {
                    this.items.addAll(spotifyPlaybackItems);
                    return this.items;
                })
                .compose(this::subscribeToRelay)
                .subscribe();
    }

    public Observable<Resource<List<SpotifyPlaybackItem>>> getListObservable() {
        return resultRelay;
    }

    private List<SpotifyPlaybackItem> mapList(List<T> list) {
        ArrayList<SpotifyPlaybackItem> newList = new ArrayList<>(list.size());
        for (T item : list) {
            newList.add(toPlaybackItem(item));
        }

        return newList;
    }

    private Single<List<SpotifyPlaybackItem>> subscribeToRelay(Single<List<SpotifyPlaybackItem>> observable) {
        //noinspection RedundantCast
        return observable
                .doOnSuccess(spotifyPlaybackItems -> resultRelay.accept(Resource.success(spotifyPlaybackItems)))
                .doOnError(throwable -> resultRelay.accept(Resource.error((Exception) throwable)));
    }

    protected abstract Single<List<T>> provideListStart();

    protected abstract Single<List<T>> provideListContinuation();

    protected abstract SpotifyPlaybackItem toPlaybackItem(T item);

    public static SpotifyListProvider<?> create(Context context, SpotifyListType type) {
        switch (type) {
            case PLAYLIST:
                return new SpotifyPlaylistProvider(context);
            case ALBUMS:
                return new SpotifyAlbumsProvider(context);
            case TRACKS:
                return new SpotifySavedTracksProvider(context);
            default:
                throw new IllegalArgumentException("Unknown provider type: " + type);
        }
    }

    protected static @Nullable
    String extractImageUrl(List<Image> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }

        // Get the smallest image that is still at least 100px wide
        Collections.sort(images, (image1, image2) -> image1.width - image2.width);
        for (Image image : images) {
            if (image == null || image.width == null || image.url == null) {
                continue;
            }

            if (image.width >= 100) {
                return image.url;
            }
        }

        Image finalImage = images.get(images.size() - 1);
        if (finalImage != null && finalImage.url != null) {
            return finalImage.url;
        }

        return null;
    }
}
