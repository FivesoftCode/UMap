package com.fivesoft.umap.formats;

import com.fivesoft.umap.format.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;

public class YAMLFormat implements Format {

    public static final String NAME = "YAML";
    public static final String[] EXTENSIONS = new String[]{"yaml", "yml"};
    public static final String MIME_TYPE = "application/yaml";

    @Override
    public @NotNull FormatReader createReader(@NotNull InputStream in, FormatReader.@Nullable Options options) {
        return null;
    }

    @Override
    public @NotNull FormatWriter createWriter(@NotNull OutputStream out, FormatWriter.@Nullable Options options) {
        return options == null ? new YAMLWriter("UTF-8") : new YAMLWriter(options.encoding);
    }

    @Override
    public @Nullable String getName() {
        return NAME;
    }

    @Override
    public @NotNull String[] getExtensions() {
        return EXTENSIONS;
    }

    @Override
    public @NotNull String getMimeType() {
        return MIME_TYPE;
    }

    private static class YAMLWriter extends TextFormatWriter {

        public YAMLWriter(@NotNull String encoding) {
            super(encoding);
        }

        @Override
        public void writeRootStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {

        }

        @Override
        public void writeRootEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {

        }

        @Override
        public void writeMappingPrefix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException {
            writeIndentation(out, context, -1);
            out.write(key + ": ");
            if(context.getOptions().pretty){
                writeSpaces(out, context.getMaxKeyLength() - key.length());
            }
        }

        @Override
        public void writeMappingSuffix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key, boolean optional,
                                       boolean valueAssigned, int index, boolean last) throws IOException, FormatException {
            if (!last) {
                writeNewLine(out);
            }
        }

        @Override
        public void writeEntryPrefix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
            writeIndentation(out, context, 0);
            out.write("- ");
        }

        @Override
        public void writeEntrySuffix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
            if (!last) {
                writeNewLine(out);
            }
        }

        @Override
        public void writeValueString(@NotNull Writer out,
                                     @NotNull WriterContext context,
                                     @NotNull String value) throws IOException, FormatException {
            out.write(value);
        }

        @Override
        public void writeValueBoolean(@NotNull Writer out, @NotNull WriterContext context, boolean value) throws IOException, FormatException {
            out.write(value ? "true" : "false");
        }

        @Override
        public void writeValueByte(@NotNull Writer out, @NotNull WriterContext context, byte value) throws IOException, FormatException {
            out.write(Byte.toString(value));
        }

        @Override
        public void writeValueShort(@NotNull Writer out, @NotNull WriterContext context, short value) throws IOException, FormatException {
            out.write(Short.toString(value));
        }

        @Override
        public void writeValueInt(@NotNull Writer out, @NotNull WriterContext context, int value) throws IOException, FormatException {
            out.write(Integer.toString(value));
        }

        @Override
        public void writeValueLong(@NotNull Writer out, @NotNull WriterContext context, long value) throws IOException, FormatException {
            out.write(Long.toString(value));
        }

        @Override
        public void writeValueFloat(@NotNull Writer out, @NotNull WriterContext context, float value) throws IOException, FormatException {
            out.write(Float.toString(value));
        }

        @Override
        public void writeValueDouble(@NotNull Writer out, @NotNull WriterContext context, double value) throws IOException, FormatException {
            out.write(Double.toString(value));
        }

        @Override
        public void writeValueChar(@NotNull Writer out, @NotNull WriterContext context, char value) throws IOException, FormatException {
            out.write(Character.toString(value));
        }

        @Override
        public void writeValueNull(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            out.write("null");
        }

        @Override
        public void writeMapStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            writeNewLine(out);
        }

        @Override
        public void writeMapEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {

        }

        @Override
        public void writeArrayStart(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException {
            writeNewLine(out);
        }

        @Override
        public void writeArrayEnd(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException {

        }
    }

}
