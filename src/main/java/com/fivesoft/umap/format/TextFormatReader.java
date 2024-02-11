package com.fivesoft.umap.format;

import java.io.*;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A base class for text-based format readers. For example, JSON, YAML, and XML readers.
 */
public abstract class TextFormatReader extends FormatReader {

    private final String encoding;
    private InputStreamReader _cachedReader;
    private int _cacheHashCode;
    private boolean closed;

    public TextFormatReader(@NotNull String encoding){
        this.encoding = Objects.requireNonNull(encoding);
        //Check if the encoding is supported
        try {
            //Maybe there is a more elegant way to do this??
            new InputStreamReader(new ByteArrayInputStream(new byte[1]), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding: " + encoding);
        }
    }

    /**
     * Gets the encoding used by this reader. (Set in the constructor)
     * @return The encoding used by this reader. For example, "UTF-8".
     */
    @NotNull
    public String getEncoding() {
        return encoding;
    }

    public abstract void readRootStart(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException;

    public abstract @Nullable String nextKey(@NotNull Reader in, @NotNull ReaderContext context,
                                             @NotNull String expectedKey) throws IOException, FormatException;

    public abstract @Nullable ValueToken nextValueToken(@NotNull Reader in, @NotNull ReaderContext context, @NotNull String key,
                                                    @NotNull Class<?> valueType, boolean optional, boolean inArray) throws IOException, FormatException;

    public abstract void readRootEnd(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException;


    @Override
    public void readRootStart(@NotNull InputStream in, @NotNull ReaderContext context) throws IOException, FormatException {
        readRootStart(getReader(in), context);
    }

    @Override
    public @Nullable String nextKeyToken(@NotNull InputStream in, @NotNull ReaderContext context, @NotNull String expectedKey) throws IOException, FormatException {
        return nextKey(getReader(in), context, expectedKey);
    }

    @Override
    public @Nullable ValueToken nextValueToken(@NotNull InputStream in, @NotNull ReaderContext context, @NotNull String key,
                                           @NotNull Class<?> valueType, boolean optional, boolean inArray) throws IOException, FormatException {
        return nextValueToken(getReader(in), context, key, valueType, optional, inArray);
    }

    @Override
    public void readRootEnd(@NotNull InputStream in, @NotNull ReaderContext context) throws IOException, FormatException {
        readRootEnd(getReader(in), context);
    }

    @Override
    public void close() {
        closed = true;
        _cachedReader = null;
        _cacheHashCode = 0;
    }

    private Reader getReader(@NotNull InputStream in) throws IOException {
        Objects.requireNonNull(in);
        if(closed){
            throw new IOException("Reader is closed");
        }
        synchronized (encoding) {
            if(_cachedReader == null || _cacheHashCode != in.hashCode()){
                _cachedReader = new InputStreamReader(in);
                _cacheHashCode = in.hashCode();
            }
            return _cachedReader;
        }
    }

}
