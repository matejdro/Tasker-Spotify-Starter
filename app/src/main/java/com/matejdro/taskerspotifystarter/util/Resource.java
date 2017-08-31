package com.matejdro.taskerspotifystarter.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

//a generic class that describes a data with a status
public class Resource<T> {
    @NonNull
    public final Status status;
    @Nullable
    public final T data;
    @Nullable
    public final Exception error;

    private Resource(@NonNull Status status, @Nullable T data, @Nullable Exception error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> Resource<T> success(@NonNull T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }

    public static <T> Resource<T> error(Exception error) {
        return new Resource<>(Status.ERROR, null, error);
    }

    public static <T> Resource<T> loading(@Nullable T data) {
        return new Resource<>(Status.LOADING, data, null);
    }

    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    @Override
    public String toString() {
        return "Resource{" +
                "status=" + status +
                ", data=" + data +
                ", error=" + error +
                '}';
    }
}


