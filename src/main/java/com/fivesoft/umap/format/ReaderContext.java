package com.fivesoft.umap.format;

public class ReaderContext extends FormatContext {

    private final FormatReader.Options options;

    public ReaderContext(FormatReader.Options options) {
        this.options = FormatReader.Options.getOrDefault(options);
    }

    public FormatReader.Options getOptions() {
        return options;
    }

}
