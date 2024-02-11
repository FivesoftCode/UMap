package com.fivesoft.umap.formats;

import com.fivesoft.umap.format.WriterContext;
import com.fivesoft.umap.format.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.*;

public class JSONFormat implements Format {

    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String NAME = "JSON";
    public static final String MIME_TYPE = "application/json";
    public static final String[] EXTENSIONS = new String[]{"json"};

    @Override
    public @NotNull FormatReader createReader(@NotNull InputStream in, FormatReader.@Nullable Options options) {
        return new JsonFormatReader(options != null ? options.encoding : DEFAULT_ENCODING);
    }

    @Override
    public @NotNull FormatWriter createWriter(@NotNull OutputStream out, FormatWriter.@Nullable Options options) {
        return new JsonFormatWriter(options != null ? options.encoding : DEFAULT_ENCODING);
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

    private static class JsonFormatWriter extends TextFormatWriter {

        /**
         * Creates a new instance of TextFormatWriter with the specified encoding.
         *
         * @param encoding The encoding to use. For example, "UTF-8".
         * @throws IllegalArgumentException If the specified encoding is not supported or invalid.
         */
        public JsonFormatWriter(@NotNull String encoding) {
            super(encoding);
        }

        @Override
        public void writeRootStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            // No root start in JSON
        }

        @Override
        public void writeRootEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException, FormatException {
            // No root end in JSON
        }

        @Override
        public void writeMappingPrefix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) throws IOException {
            writeNewLineIfPretty(out, context);
            writeIndentationIfPretty(out, context);
            out.write('"' + encodeString(key) + "\":");
            writeSpaceIfPretty(out, context);
        }

        @Override
        public void writeMappingSuffix(@NotNull Writer out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) throws IOException {
            if(!last){
                out.write(',');
            } else {
                writeNewLineIfPretty(out, context);
            }
        }

        @Override
        public void writeEntryPrefix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException {
            writeNewLineIfPretty(out, context);
            writeIndentationIfPretty(out, context);
        }

        @Override
        public void writeEntrySuffix(@NotNull Writer out, @NotNull WriterContext context, int index, boolean last) throws IOException {
            if(!last){
                out.write(',');
            } else {
                writeNewLineIfPretty(out, context);
            }
        }

        @Override
        public void writeValueString(@NotNull Writer out, @NotNull WriterContext context, @NotNull String value) throws IOException {
            out.write('"' + encodeString(value) + '"');
        }

        @Override
        public void writeValueBoolean(@NotNull Writer out, @NotNull WriterContext context, boolean value) throws IOException {
            out.write(value ? "true" : "false");
        }

        @Override
        public void writeValueByte(@NotNull Writer out, @NotNull WriterContext context, byte value) throws IOException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueShort(@NotNull Writer out, @NotNull WriterContext context, short value) throws IOException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueInt(@NotNull Writer out, @NotNull WriterContext context, int value) throws IOException {
            writeValueLong(out, context, value);
        }

        @Override
        public void writeValueLong(@NotNull Writer out, @NotNull WriterContext context, long value) throws IOException {
            out.write(String.valueOf(value));
        }

        @Override
        public void writeValueFloat(@NotNull Writer out, @NotNull WriterContext context, float value) throws IOException {
            writeValueDouble(out, context, value);
        }

        @Override
        public void writeValueDouble(@NotNull Writer out, @NotNull WriterContext context, double value) throws IOException {
            if (Double.isFinite(value)) {
                out.write(String.valueOf(value));
            } else {
                writeValueString(out, context, String.valueOf(value));
            }
        }

        @Override
        public void writeValueChar(@NotNull Writer out, @NotNull WriterContext context, char value) throws IOException {
            writeValueString(out, context, String.valueOf(value));
        }

        @Override
        public void writeValueNull(@NotNull Writer out, @NotNull WriterContext context) throws IOException {
            out.write("null");
        }

        @Override
        public void writeMapStart(@NotNull Writer out, @NotNull WriterContext context) throws IOException {
            out.write('{');
        }

        @Override
        public void writeMapEnd(@NotNull Writer out, @NotNull WriterContext context) throws IOException {
            writeIndentationIfPretty(out, context);
            out.write('}');
        }

        @Override
        public void writeArrayStart(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException {
            out.write('[');
        }

        @Override
        public void writeArrayEnd(@NotNull Writer out, @NotNull WriterContext context, int length) throws IOException {
            writeIndentationIfPretty(out, context);
            out.write(']');
        }

        private static String encodeString(@NotNull String s){
            StringBuilder sb = new StringBuilder();
            for (int i = 0, length = s.length(); i < length; i++) {
                char c = s.charAt(i);
                /*
                 * From RFC 4627, "All Unicode characters may be placed within the
                 * quotation marks except for the characters that must be escaped:
                 * quotation mark, reverse solidus, and the control characters
                 * (U+0000 through U+001F)."
                 */
                switch (c) {
                    case '"':
                    case '\\':
                    case '/':
                        sb.append('\\').append(c);
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        if (c <= 0x1F) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                        break;
                }
            }
            return sb.toString();
        }

    }

    private static class JsonFormatReader extends TextFormatReader {

        public JsonFormatReader(@NotNull String encoding) {
            super(encoding);
        }

        @Override
        public void readRootStart(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException {
            int c = getFirstNonWhitespaceChar(in);
            if(c != '{'){
                throw new FormatException("Missing root bracket.");
            }
        }

        @Override
        public @Nullable String nextKey(@NotNull Reader in,
                                        @NotNull ReaderContext context,
                                        @NotNull String expectedKey) throws IOException, FormatException {
            int c = getFirstNonWhitespaceChar(in);
            if(c == '}')
                return null; // End of map
            if(c == ',')
                c = getFirstNonWhitespaceChar(in); // Skip comma from previous entry if any

            if(c != '"'){
                throw new FormatException("Missing key quote.");
            }
            StringBuilder sb = new StringBuilder();
            while ((c = in.read()) != -1) {
                if(c == '"'){
                    c = getFirstNonWhitespaceChar(in);
                    if(c != ':'){
                        throw new FormatException("Missing colon after key.");
                    }
                    return sb.toString();
                }
                sb.append((char) c);
            }
            return null;
        }

        @Override
        public @Nullable ValueToken nextValueToken(@NotNull Reader in, @NotNull ReaderContext context, @NotNull String key,
                                               @NotNull Class<?> valueType, boolean optional, boolean inArray) throws IOException, FormatException {

            int c = getFirstNonWhitespaceChar(in);
            if(c == -1)
                throw new FormatException("Unexpected end of input.");
            if(c == ',' || c == ';')
                c = getFirstNonWhitespaceChar(in); // Skip comma from previous entry if any

            if(c == '{'){
                return ValueToken.map(false);
            } else if(c == '[') {
                return ValueToken.array(false);
            } else if(c == ']'){
                return null;
            }

            StringBuilder sb = new StringBuilder();
            String s = null;
            boolean isLast = false;
            if(c == '"'){
                //Beginning of string
                boolean escape = false;
                while ((c = in.read()) != -1) {
                    if(escape){
                        switch (c) {
                            case 'b':
                                sb.append('\b');
                                break;
                            case 'f':
                                sb.append('\f');
                                break;
                            case 'n':
                                sb.append('\n');
                                break;
                            case 'r':
                                sb.append('\r');
                                break;
                            case 't':
                                sb.append('\t');
                                break;
                            case 'u':
                                //Unicode escape
                                StringBuilder hex = new StringBuilder();
                                for (int i = 0; i < 4; i++) {
                                    c = in.read();
                                    if(c == -1){
                                        throw new FormatException("Unexpected end of input.");
                                    }
                                    hex.append((char) c);
                                }
                                try {
                                    sb.append((char) Integer.parseInt(hex.toString(), 16));
                                } catch (NumberFormatException e) {
                                    throw new FormatException("Invalid unicode escape sequence: \\u" + hex);
                                }
                                break;
                            default:
                                sb.append((char) c);
                                break;
                        }
                    } else {
                        if(c == '"'){
                            s = sb.toString();
                            break;
                        } else if(c == '\\'){
                            escape = true;
                        } else {
                            sb.append((char) c);
                        }
                    }
                }
                if (s == null) {
                    throw new FormatException("Unexpected end of input.");
                }

                c = getFirstNonWhitespaceChar(in);
                if(c == '}' || c == ']'){
                    isLast = true;
                } else if (c != ',' && c != ';' && c != -1) {
                    throw new FormatException("Unexpected character after string value: " + (char) c);
                }
            } else {
                //Another value type
                sb.append((char) c);
                while ((c = in.read()) != -1) {
                    if(c == ',' || c == ';'){
                        break;
                    }
                    if(Character.isWhitespace(c)){
                        c = getFirstNonWhitespaceChar(in);
                    }
                    if(c == '}' || c == ']'){
                        isLast = true;
                        break;
                    }
                    sb.append((char) c);
                }
                s = sb.toString();
                if(s.equalsIgnoreCase("null")){
                    s = null;
                }
            }
            return new ValueToken(s, isLast);
        }

        @Override
        public void readRootEnd(@NotNull Reader in, @NotNull ReaderContext context) throws IOException, FormatException {

        }

        private static int getFirstNonWhitespaceChar(@NotNull Reader in) throws IOException {
            int c;
            while ((c = in.read()) != -1) {
                if (!Character.isWhitespace(c)) {
                    return c;
                }
            }
            return -1;
        }

    }

}
