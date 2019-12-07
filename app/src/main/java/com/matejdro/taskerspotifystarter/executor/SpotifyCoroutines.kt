package com.matejdro.taskerspotifystarter.executor

import android.content.Context
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun connectToSpotifyAndAwait(context: Context, connectionParams: ConnectionParams): SpotifyAppRemote {
    return suspendCoroutine { continuation ->
        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onFailure(error: Throwable) {
                continuation.resumeWithException(error)
            }

            override fun onConnected(remote: SpotifyAppRemote) {
                continuation.resume(remote)
            }
        })
    }
}

suspend fun <T> CallResult<T>.awaitSuspending(): T {
    return suspendCancellableCoroutine { continuation ->
        setResultCallback {
            continuation.resume(it)
        }
        setErrorCallback {
            continuation.resumeWithException(it)
        }

        continuation.invokeOnCancellation {
            cancel()
        }
    }
}

inline fun <T> SpotifyAppRemote.use(block: (SpotifyAppRemote) -> T): T {
    try {
        return block(this)
    } finally {
        SpotifyAppRemote.disconnect(this)
    }
}