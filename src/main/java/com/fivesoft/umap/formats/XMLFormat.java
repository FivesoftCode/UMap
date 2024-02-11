package com.fivesoft.umap.formats;

import com.fivesoft.umap.format.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class XMLFormat implements Format {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String XML_HEADER_TAG = "<?xml version=\"1.0\" encoding=\"%s\"?>";
    public static final String XML_ROOT_TAG = "<root>";
    public static final String XML_ROOT_END_TAG = "</root>";

    public static final String ITEM_TAG = "<item n=\"%d\">";
    public static final String ITEM_END_TAG = "</item>";

    @Override
    public @NotNull FormatReader createReader(@NotNull InputStream in, FormatReader.@Nullable Options options) {
        return new XMLFormatReader(options != null ? options.encoding : DEFAULT_ENCODING);
    }

    @Override
    public @NotNull FormatWriter createWriter(@NotNull OutputStream out, FormatWriter.@Nullable Options options) {
        return new XMLFormatWriter(options != null ? options.encoding : DEFAULT_ENCODING);
    }

    @Override
    public @Nullable String getName() {
        return "XML";
    }

    @Override
    public @NotNull String[] getExtensions() {
        return new String[]{"xml"};
    }

    @Override
    public @NotNull String getMimeType() {
        return "application/xml";
    }

    private static class XMLFormatWriter extends TextFormatWriter {

        public XMLFormatWriter(@NotNull String encoding) {
            super(encoding);
        }

        @Override
        public void writeRootStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            out.write(String.format(XML_HEADER_TAG, getEncoding()));
            writeNewLineIfPretty(out, context);
            out.write(XML_ROOT_TAG);
        }

        @Override
        public void writeRootEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            out.write(XML_ROOT_END_TAG);
        }

        @Override
        public void writeMappingPrefix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException {
            if (valueAssigned) {
                writeNewLineIfPretty(out, context);
                writeIndentationIfPretty(out, context);
                out.write('<');
                out.write(key);
                out.write('>');
            }
        }

        @Override
        public void writeMappingSuffix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key, boolean optional, boolean valueAssigned,
                                       int index, boolean last) throws IOException, FormatException {
            if (valueAssigned) {
                out.write("</");
                out.write(key);
                out.write('>');
                if(last) {
                    writeNewLineIfPretty(out, context);
                }
            }
        }

        @Override
        public void writeEntryPrefix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
            writeNewLineIfPretty(out, context);
            writeIndentationIfPretty(out, context);
            out.write(String.format(ITEM_TAG, index));
        }

        @Override
        public void writeEntrySuffix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException, FormatException {
            out.write(ITEM_END_TAG);
            if(last) {
                writeNewLineIfPretty(out, context);
            }
        }

        @Override
        public void writeValueString(@NotNull Writer out, @NotNull WriterContext context, @NotNull String value) throws IOException, FormatException {
            out.write(value);
        }

        @Override
        public void writeValueBoolean(@NotNull Writer out, @NotNull WriterContext context, boolean value) throws IOException, FormatException {
            writeValueString(out, context, Boolean.toString(value));
        }

        @Override
        public void writeValueByte(@NotNull Writer out, @NotNull WriterContext context, byte value) throws IOException, FormatException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueShort(@NotNull Writer out, @NotNull WriterContext context, short value) throws IOException, FormatException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueInt(@NotNull Writer out, @NotNull WriterContext context, int value) throws IOException, FormatException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueLong(@NotNull Writer out, @NotNull WriterContext context, long value) throws IOException, FormatException {
            writeValueString(out, context, Long.toString(value));
        }

        @Override
        public void writeValueFloat(@NotNull Writer out, @NotNull WriterContext context, float value) throws IOException, FormatException {
            writeValueDouble(out, context, value);
        }

        @Override
        public void writeValueDouble(@NotNull Writer out, @NotNull WriterContext context, double value) throws IOException, FormatException {
            writeValueString(out, context, Double.toString(value));
        }

        @Override
        public void writeValueChar(@NotNull Writer out, @NotNull WriterContext context, char value) throws IOException, FormatException {
            writeValueString(out, context, Character.toString(value));
        }

        @Override
        public void writeValueNull(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            //XML does not support null values. For null optional fields, the field is omitted.
        }

        @Override
        public void writeMapStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {

        }

        @Override
        public void writeMapEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            writeIndentationIfPretty(out, context);
        }

        @Override
        public void writeArrayStart(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException {

        }

        @Override
        public void writeArrayEnd(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException, FormatException {
            writeIndentationIfPretty(out, context);
        }


    }

    private static class XMLFormatReader extends TextFormatReader {

        public XMLFormatReader(@NotNull String encoding) {
            super(encoding);
        }

        @Override
        public void readRootStart(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException {

        }

        @Override
        public @Nullable String nextKey(@NotNull Reader in, @NotNull ReaderContext context, @NotNull String expectedKey) throws IOException, FormatException {
            return null;
        }

        @Override
        public @Nullable ValueToken nextValueToken(@NotNull Reader in, @NotNull ReaderContext context, @NotNull String key, @NotNull Class<?> valueType, boolean optional, boolean inArray) throws IOException, FormatException {
            return null;
        }

        @Override
        public void readRootEnd(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException {

        }



    }

}
