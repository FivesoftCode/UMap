package com.fivesoft.umap.format;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

/**
 * A base class for text-based format writers. For example, JSON, YAML, and XML writers.
 */
public abstract class TextFormatWriter extends FormatWriter {

    private final String encoding;
    private OutputStreamWriter _cachedWriter;
    private int _cacheHashCode;
    private boolean closed;

    /**
     * Creates a new instance of TextFormatWriter with the specified encoding.
     * @param encoding The encoding to use. For example, "UTF-8".
     * @throws IllegalArgumentException If the specified encoding is not supported or invalid.
     */
    public TextFormatWriter(@NotNull String encoding) {
        this.encoding = Objects.requireNonNull(encoding);
        //Check if the encoding is supported
        try {
            //Maybe there is a more elegant way to do this??
            new OutputStreamWriter(new ByteArrayOutputStream(), encoding);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unsupported encoding: " + encoding);
        }
    }

    /**
     * Gets the encoding used by this writer. (Set in the constructor)
     * @return The encoding used by this writer. For example, "UTF-8".
     */
    @NotNull
    public String getEncoding() {
        return encoding;
    }

    public abstract void writeRootStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException;
    public abstract void writeRootEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException;

    public abstract void writeMappingPrefix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key, boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException;
    public abstract void writeMappingSuffix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key, boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException;

    public abstract void writeEntryPrefix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException;
    public abstract void writeEntrySuffix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException;

    public abstract void writeValueString(@NotNull Writer out, @NotNull WriterContext context, @NotNull String value) throws IOException, FormatException;
    public abstract void writeValueBoolean(@NotNull Writer out, @NotNull WriterContext context, boolean value) throws IOException, FormatException;
    public abstract void writeValueByte(@NotNull Writer out, @NotNull WriterContext context, byte value) throws IOException, FormatException;
    public abstract void writeValueShort(@NotNull Writer out, @NotNull WriterContext context, short value) throws IOException, FormatException;
    public abstract void writeValueInt(@NotNull Writer out, @NotNull WriterContext context, int value) throws IOException, FormatException;
    public abstract void writeValueLong(@NotNull Writer out, @NotNull WriterContext context, long value) throws IOException, FormatException;
    public abstract void writeValueFloat(@NotNull Writer out, @NotNull WriterContext context, float value) throws IOException, FormatException;
    public abstract void writeValueDouble(@NotNull Writer out, @NotNull WriterContext context, double value) throws IOException, FormatException;
    public abstract void writeValueChar(@NotNull Writer out, @NotNull WriterContext context, char value) throws IOException, FormatException;
    public abstract void writeValueNull(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException;
    public abstract void writeMapStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException;
    public abstract void writeMapEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException;
    public abstract void writeArrayStart(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException;
    public abstract void writeArrayEnd(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException;

    @Override
    public void writeRootStart(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException {
        writeRootStart(getWriter(out), context);
    }

    @Override
    public void writeRootEnd(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException {
        writeRootEnd(getWriter(out), context);
    }

    @Override
    public final void writeMappingPrefix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key, boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException {
        writeMappingPrefix(getWriter(out), context, key, optional, valueAssigned, index, last);
    }

    @Override
    public final void writeMappingSuffix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key, boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException {
        writeMappingSuffix(getWriter(out), context, key, optional, valueAssigned, index, last);
    }

    @Override
    public final void writeEntryPrefix(@NotNull OutputStream out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
        writeEntryPrefix(getWriter(out), context, index, last);
    }

    @Override
    public final void writeEntrySuffix(@NotNull OutputStream out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
        writeEntrySuffix(getWriter(out), context, index, last);
    }

    @Override
    public final void writeValueString(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String value) throws IOException, FormatException {
        writeValueString(getWriter(out), context, value);
    }

    @Override
    public final void writeValueBoolean(@NotNull OutputStream out, @NotNull WriterContext context, boolean value) throws IOException, FormatException {
        writeValueBoolean(getWriter(out), context, value);
    }

    @Override
    public final void writeValueByte(@NotNull OutputStream out, @NotNull WriterContext context, byte value) throws IOException, FormatException {
        writeValueByte(getWriter(out), context, value);
    }

    @Override
    public final void writeValueShort(@NotNull OutputStream out, @NotNull WriterContext context, short value) throws IOException, FormatException {
        writeValueShort(getWriter(out), context, value);
    }

    @Override
    public final void writeValueInt(@NotNull OutputStream out, @NotNull WriterContext context, int value) throws IOException, FormatException {
        writeValueInt(getWriter(out), context, value);
    }

    @Override
    public final void writeValueLong(@NotNull OutputStream out, @NotNull WriterContext context, long value) throws IOException, FormatException {
        writeValueLong(getWriter(out), context, value);
    }

    @Override
    public final void writeValueFloat(@NotNull OutputStream out, @NotNull WriterContext context, float value) throws IOException, FormatException {
        writeValueFloat(getWriter(out), context, value);
    }

    @Override
    public final void writeValueDouble(@NotNull OutputStream out, @NotNull WriterContext context, double value) throws IOException, FormatException {
        writeValueDouble(getWriter(out), context, value);
    }

    @Override
    public final void writeValueChar(@NotNull OutputStream out, @NotNull WriterContext context, char value) throws IOException, FormatException {
        writeValueChar(getWriter(out), context, value);
    }

    @Override
    public void writeValueNull(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException {
        writeValueNull(getWriter(out), context);
    }

    @Override
    public final void writeMapStart(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException {
        writeMapStart(getWriter(out), context);
    }

    @Override
    public final void writeMapEnd(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException {
        writeMapEnd(getWriter(out), context);
    }

    @Override
    public final void writeArrayStart(@NotNull OutputStream out, @NotNull WriterContext context, int length) throws IOException, FormatException {
        writeArrayStart(getWriter(out), context, length);
    }

    @Override
    public final void writeArrayEnd(@NotNull OutputStream out, @NotNull WriterContext context, int length) throws IOException, FormatException {
        writeArrayEnd(getWriter(out), context, length);
    }

    protected void writeSpace(@NotNull Writer writer) throws IOException {
        writer.write(' ');
    }

    protected void writeIndentation(@NotNull Writer writer,
                                    @NotNull WriterContext context,
                                    int shift) throws IOException {
        int total = context.getOptions().indentFactor * Math.max(0, context.getDepth() + shift);
        for (int i = 0; i < total; i++) {
            writeSpace(writer);
        }
    }

    protected void writeIndentation(@NotNull Writer writer,
                                    @NotNull WriterContext context) throws IOException {
        writeIndentation(writer, context, 0);
    }

    protected void writeNewLine(@NotNull Writer writer) throws IOException {
        writer.write('\n');
    }

    protected final void writeSpaces(@NotNull Writer writer, int count) throws IOException {
        for (int i = 0; i < count; i++) {
            writeSpace(writer);
        }
    }

    protected final void writeSpacesIfPretty(@NotNull Writer writer,
                                            @NotNull WriterContext context, int count) throws IOException {
        if(context.getOptions().pretty){
            writeSpaces(writer, count);
        }
    }

    protected final void writeSpaceIfPretty(@NotNull Writer writer,
                                            @NotNull WriterContext context) throws IOException {
        if(context.getOptions().pretty){
            writeSpace(writer);
        }
    }

    protected final void writeIndentationIfPretty(@NotNull Writer writer,
                                            @NotNull WriterContext context) throws IOException {
        writeIndentationIfPretty(writer, context, 0);
    }

    protected final void writeIndentationIfPretty(@NotNull Writer writer,
                                                  @NotNull WriterContext context,
                                                  int shift) throws IOException {
        if(context.getOptions().pretty){
            writeIndentation(writer, context, shift);
        }
    }

    protected final void writeNewLineIfPretty(@NotNull Writer writer,
                                      @NotNull WriterContext context) throws IOException {
        if(context.getOptions().pretty){
            writeNewLine(writer);
        }
    }

    @Override
    public void flush() throws IOException {
        synchronized (encoding) {
            if(_cachedWriter != null){
                _cachedWriter.flush();
            }
        }
    }

    @Override
    public void close() {
        closed = true;
        _cachedWriter = null;
        _cacheHashCode = 0;
    }

    private Writer getWriter(@NotNull OutputStream out) throws IOException {
        Objects.requireNonNull(out);
        if(closed){
            throw new IOException("Writer is closed");
        }
        synchronized (encoding) {
            if(_cachedWriter == null || _cacheHashCode != out.hashCode()){
                _cachedWriter = new OutputStreamWriter(out);
                _cacheHashCode = out.hashCode();
            }
            return _cachedWriter;
        }
    }

}
