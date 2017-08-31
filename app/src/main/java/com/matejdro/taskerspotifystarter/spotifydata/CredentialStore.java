package com.matejdro.taskerspotifystarter.spotifydata;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

public class CredentialStore {
    private static final String PREFERENCES_NAME = "credentials";
    private static final String PREFERENCE_TOKEN = "token";

    private static CredentialStore instance;

    public static CredentialStore getInstance(Context context) {
        if (instance == null) {
            instance = new CredentialStore(context.getApplicationContext());
        }

        return instance;
    }

    private SharedPreferences preferences;

    private CredentialStore(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public @Nullable
    String getSpotifyToken() {
        return preferences.getString(PREFERENCE_TOKEN, null);
    }

    public void setSpotifyToken(String newToken) {
        preferences.edit().putString(PREFERENCE_TOKEN, newToken).apply();
    }
}
