package com.matejdro.taskerspotifystarter.spotifydata;

public class SpotifyPlaybackItem {
    private final String title;
    private final String imageUrl;
    private final String contentUri;

    public SpotifyPlaybackItem(String title, String imageUrl, String contentUri) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.contentUri = contentUri;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getContentUri() {
        return contentUri;
    }

    @Override
    public String toString() {
        return "SpotifyPlaybackItem{" +
                "title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", contentUri='" + contentUri + '\'' +
                '}';
    }
}
