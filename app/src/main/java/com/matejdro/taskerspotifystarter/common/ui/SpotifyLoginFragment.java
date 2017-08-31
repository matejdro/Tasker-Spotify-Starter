package com.matejdro.taskerspotifystarter.common.ui;

import android.arch.lifecycle.LifecycleFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.view.View;
import com.matejdro.taskerspotifystarter.BuildConfig;
import com.matejdro.taskerspotifystarter.spotifydata.CredentialStore;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;


public abstract class SpotifyLoginFragment extends LifecycleFragment implements UriReceiver {
    private static final int SPOTIFY_LOGIN_REQUEST_CODE = 1;
    private static final String REDIRECT_URI = "startify://login";

    private boolean loggedIn = false;

    protected abstract void onLoginSuccess();

    protected abstract void onLoginCancelled();

    protected abstract void onLoginFailed();

    @Override
    @CallSuper
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null && !loggedIn) {
            forceRelogin();
        } else {
            onLoginSuccess();
        }
    }

    protected void forceRelogin() {
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(BuildConfig.SPOTIFY_API_KEY, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
                        .setShowDialog(true);

        builder.setScopes(new String[]{"playlist-read-private", "user-library-read"});
        AuthenticationRequest request = builder.build();

        startActivityForResult(
                AuthenticationClient.createLoginActivityIntent(getActivity(), request),
                SPOTIFY_LOGIN_REQUEST_CODE);
    }

    protected void logout() {
        CredentialStore.getInstance(getContext()).setSpotifyToken(null);
    }

    private void receivedAuthenticationResponse(AuthenticationResponse response) {
        if (response.getType() == AuthenticationResponse.Type.TOKEN) {
            CredentialStore.getInstance(getContext()).setSpotifyToken(response.getAccessToken());
            loggedIn = true;
            onLoginSuccess();
        } else {
            onLoginFailed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPOTIFY_LOGIN_REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            receivedAuthenticationResponse(response);
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onUriReceived(Uri uri) {
        receivedAuthenticationResponse(AuthenticationResponse.fromUri(uri));
    }
}
