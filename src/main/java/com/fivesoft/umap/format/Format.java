package com.fivesoft.umap.format;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;

public interface Format {

    @NotNull
    FormatReader createReader(@NotNull InputStream in,
                              @Nullable FormatReader.Options options);

    default @NotNull FormatReader createReader(@NotNull InputStream in) {
        return createReader(in, null);
    }

    @NotNull
    FormatWriter createWriter(@NotNull OutputStream out,
                              @Nullable FormatWriter.Options options);

    default @NotNull FormatWriter createWriter(@NotNull OutputStream out) {
        return createWriter(out, null);
    }

    @Nullable
    String getName();

    @NotNull
    String[] getExtensions();

    @NotNull
    String getMimeType();

    @NotNull
    default String getExtension() {
        return getExtensions()[0];
    }

}
