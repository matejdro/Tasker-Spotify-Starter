package com.matejdro.taskerspotifystarter.spotifydata.providers;

import android.content.Context;
import android.support.v4.util.ArrayMap;
import com.matejdro.taskerspotifystarter.spotifydata.SpotifyPlaybackItem;
import io.reactivex.Single;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.SavedTrack;

import java.util.List;
import java.util.Map;

public class SpotifySavedTracksProvider extends SpotifyListProvider<SavedTrack> {
    private int nextOffset = 0;

    public SpotifySavedTracksProvider(Context context) {
        super(context);
    }

    @Override
    protected Single<List<SavedTrack>> provideListStart() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            Pager<SavedTrack> tracksPager = spotifyApi.getService().getMySavedTracks(parameters);
            List<SavedTrack> items = tracksPager.items;

            this.nextOffset = tracksPager.offset + items.size();

            return items;
        });
    }

    @Override
    protected Single<List<SavedTrack>> provideListContinuation() {
        return Single.fromCallable(() -> {
            Map<String, Object> parameters = new ArrayMap<>();
            parameters.put("offset", nextOffset);
            Pager<SavedTrack> tracksPager = spotifyApi.getService().getMySavedTracks(parameters);
            List<SavedTrack> items = tracksPager.items;


            this.nextOffset = tracksPager.offset + items.size();

            return items;
        });
    }

    @Override
    protected SpotifyPlaybackItem toPlaybackItem(SavedTrack item) {
        return new SpotifyPlaybackItem(
                item.track.name,
                extractImageUrl(item.track.album.images),
                "https://open.spotify.com/track/" + item.track.id
        );
    }
}
