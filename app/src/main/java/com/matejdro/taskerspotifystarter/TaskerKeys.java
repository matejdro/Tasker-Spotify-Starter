package com.matejdro.taskerspotifystarter;

public interface TaskerKeys {
    public static final String KEY_MEDIA_ID = "MediaID";
    public static final String KEY_MEDIA_TITLE = "MediaTitle";
    public static final String KEY_SEARCH_TERM = "SearchTerm";
    public static final String KEY_REPEAT = "Repeat";
    public static final String KEY_SHUFFLE = "Shuffle";
    public static final String KEY_ACTION = "Action";

    public static final int SHUFFLE_NO_CHANGE = 0;
    public static final int SHUFFLE_ENABLE = 1;
    public static final int SHUFFLE_DISABLE = 2;

    public static final int REPEAT_NO_CHANGE = 0;
    public static final int REPEAT_DISABLE = 1;
    public static final int REPEAT_ONE = 2;
    public static final int REPEAT_ALL = 3;

    public static final int ACTION_PLAY_FROM_YOUR_MUSIC = 0;
    public static final int ACTION_PLAY_FROM_SEARCH = 1;
}
