package com.matejdro.taskerspotifystarter.spotifydata.providers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import io.reactivex.Single;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.PlaylistSimple;

import java.util.List;
import java.util.Map;

public class SpotifyPlaylistProvider extends SpotifyListProvider<PlaylistSimple> {
    private int nextOffset = 0;

    public SpotifyPlaylistProvider(Context context) {
        super(context);
    }

    @Override
    protected Single<List<PlaylistSimple>> provideListStart() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            Pager<PlaylistSimple> playlistSimplePager = spotifyApi.getService().getMyPlaylists(parameters);
            List<PlaylistSimple> items = playlistSimplePager.items;

            this.nextOffset = playlistSimplePager.offset + items.size();

            return items;
        });
    }

    @Override
    protected Single<List<PlaylistSimple>> provideListContinuation() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            parameters.put("offset", nextOffset);
            Pager<PlaylistSimple> playlistSimplePager = spotifyApi.getService().getMyPlaylists(parameters);
            List<PlaylistSimple> items = playlistSimplePager.items;


            this.nextOffset = playlistSimplePager.offset + items.size();

            return items;
        });
    }

    @Override
    protected SpotifyPlaybackItem toPlaybackItem(PlaylistSimple item) {
        return new SpotifyPlaybackItem(
                item.name,
                extractImageUrl(item.images),
                "https://open.spotify.com/user/" + item.owner.id + "/playlist/" + item.id
        );
    }
}
