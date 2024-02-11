package com.fivesoft.umap.format;

import org.jetbrains.annotations.Nullable;

public class WriterContext extends FormatContext {
    private final FormatWriter.Options options;
    private int maxKeyLength = -1;

    public WriterContext(@Nullable FormatWriter.Options options) {
        this.options = FormatWriter.Options.getOrDefault(options);
    }

    public FormatWriter.Options getOptions() {
        return options;
    }

    public int getMaxKeyLength() {
        return maxKeyLength;
    }

    void setMaxKeyLength(int maxKeyLength) {
        this.maxKeyLength = maxKeyLength;
    }

}
