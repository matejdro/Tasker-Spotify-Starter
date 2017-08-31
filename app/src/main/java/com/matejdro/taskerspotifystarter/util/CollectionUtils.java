package com.matejdro.taskerspotifystarter.util;

import java.util.Collection;

public class CollectionUtils {
    public static <T> T firstOrNull(Collection<T> items) {
        if (items.isEmpty()) {
            return null;
        }

        return items.iterator().next();
    }
}
