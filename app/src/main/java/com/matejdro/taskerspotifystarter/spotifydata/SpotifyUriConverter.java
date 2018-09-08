package com.matejdro.taskerspotifystarter.spotifydata;

import android.support.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyUriConverter {
    private static final Pattern USER_URL_PATTERN = Pattern.compile("open\\.spotify\\.com\\/user\\/([^/]+)\\/([^/]+)\\/([^/]+)");
    private static final Pattern GLOBAL_URL_PATTERN = Pattern.compile("open\\.spotify\\.com\\/([^/]+)\\/([^/]+)");

    public static @Nullable
    String toContentUri(String url) {
        if (url.trim().isEmpty()) {
            return null;
        }

        Matcher matcher = USER_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return generateContentUri(matcher.group(1), matcher.group(2), matcher.group(3));
        }

        matcher = GLOBAL_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            return generateContentUri(matcher.group(1), matcher.group(2));
        }

        return null;
    }

    private static String generateContentUri(String mediaType, String mediaId) {
        return "spotify:" + mediaType + ":" + mediaId;
    }

    private static String generateContentUri(String ownerUserId, String mediaType, String mediaId) {
        return "spotify:user:" + ownerUserId + ":" + mediaType + ":" + mediaId;
    }
}
