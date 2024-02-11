package com.fivesoft.umap.format;

import java.util.HashMap;
import java.util.Map;

public class FormatContext {

    private final Map<String, Object> extras = new HashMap<>();
    private int depth;

    public void putExtra(String key, Object value) {
        extras.put(key, value);
    }

    public void removeExtra(String key) {
        extras.remove(key);
    }

    public <T>T getExtra(String key) {
        //noinspection unchecked
        return (T) extras.get(key);
    }

    public int getDepth() {
        return depth;
    }

    void incrementDepth() {
        depth++;
    }

    void decrementDepth() {
        depth--;
    }

    void resetDepth() {
        depth = 0;
    }

}
