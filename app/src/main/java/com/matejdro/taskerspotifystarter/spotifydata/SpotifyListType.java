package com.matejdro.taskerspotifystarter.spotifydata;

import android.support.annotation.StringRes;
import com.matejdro.taskerspotifystarter.R;

public enum SpotifyListType {
    PLAYLIST(R.string.playlist_description),
    TRACKS(R.string.tracks_description),
    ALBUMS(R.string.albums_description);

    @StringRes
    private final int descriptionResource;

    SpotifyListType(@StringRes int descriptionResource) {
        this.descriptionResource = descriptionResource;
    }

    public @StringRes
    int getDescriptionResource() {
        return descriptionResource;
    }
}
