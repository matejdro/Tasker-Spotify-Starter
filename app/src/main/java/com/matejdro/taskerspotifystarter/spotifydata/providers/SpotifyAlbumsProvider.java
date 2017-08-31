package com.matejdro.taskerspotifystarter.spotifydata.providers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import io.reactivex.Single;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedAlbum;

import java.util.List;
import java.util.Map;

public class SpotifyAlbumsProvider extends SpotifyListProvider<SavedAlbum> {
    private int nextOffset = 0;

    public SpotifyAlbumsProvider(Context context) {
        super(context);
    }

    @Override
    protected Single<List<SavedAlbum>> provideListStart() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            Pager<SavedAlbum> albumPager = spotifyApi.getService().getMySavedAlbums(parameters);
            List<SavedAlbum> items = albumPager.items;

            this.nextOffset = albumPager.offset + items.size();

            return items;
        });
    }

    @Override
    protected Single<List<SavedAlbum>> provideListContinuation() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            parameters.put("offset", nextOffset);
            Pager<SavedAlbum> albumPager = spotifyApi.getService().getMySavedAlbums(parameters);
            List<SavedAlbum> items = albumPager.items;


            this.nextOffset = albumPager.offset + items.size();

            return items;
        });
    }

    @Override
    protected SpotifyPlaybackItem toPlaybackItem(SavedAlbum item) {
        return new SpotifyPlaybackItem(
                item.album.name,
                extractImageUrl(item.album.images),
                "https://open.spotify.com/album/" + item.album.id
        );
    }
}
