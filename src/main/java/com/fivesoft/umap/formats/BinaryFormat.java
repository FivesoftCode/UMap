package com.fivesoft.umap.formats;

import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.format.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BinaryFormat implements Format {

    public static final String NAME = "UMap Binary";
    public static final String MIME_TYPE = "application/umap-binary";
    public static final String[] EXTENSIONS = new String[]{"ubin"};
    public static final byte PREFIX_NULL = 0;
    public static final byte PREFIX_VAL = 1;

    @Override
    public @NotNull FormatReader createReader(@NotNull InputStream in, FormatReader.@Nullable Options options) {
        return BINARY_FORMAT_READER;
    }

    @Override
    public @NotNull FormatWriter createWriter(@NotNull OutputStream out, FormatWriter.@Nullable Options options) {
        return BINARY_FORMAT_WRITER;
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

    private static final FormatWriter BINARY_FORMAT_WRITER = new FormatWriter() {

        @Override
        public void writeMappingPrefix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) throws IOException {
            if(optional){
                out.write(valueAssigned ? PREFIX_VAL : PREFIX_NULL);
            }
        }

        @Override
        public void writeValueString(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String value) throws IOException, FormatException {
            byte[] bytes = value.getBytes();
            writeValueInt(out, context, bytes.length);
            out.write(bytes);
        }

        @Override
        public void writeValueBoolean(@NotNull OutputStream out, @NotNull WriterContext context, boolean value) throws IOException {
            out.write(value ? 1 : 0);
        }

        @Override
        public void writeValueByte(@NotNull OutputStream out, @NotNull WriterContext context, byte value) throws IOException {
            out.write(value);
        }

        @Override
        public void writeValueShort(@NotNull OutputStream out, @NotNull WriterContext context, short value) throws IOException {
            out.write(value >>> 8);
            out.write(value);
        }

        @Override
        public void writeValueInt(@NotNull OutputStream out, @NotNull WriterContext context, int value) throws IOException {
            out.write(value >>> 24);
            out.write(value >>> 16);
            out.write(value >>> 8);
            out.write(value);
        }

        @Override
        public void writeValueLong(@NotNull OutputStream out, @NotNull WriterContext context, long value) throws IOException {
            out.write((int)(value >>> 56));
            out.write((int)(value >>> 48));
            out.write((int)(value >>> 40));
            out.write((int)(value >>> 32));
            out.write((int)(value >>> 24));
            out.write((int)(value >>> 16));
            out.write((int)(value >>> 8));
            out.write((int)value);
        }

        @Override
        public void writeValueFloat(@NotNull OutputStream out, @NotNull WriterContext context, float value) throws IOException, FormatException {
            writeValueInt(out, context, Float.floatToIntBits(value));
        }

        @Override
        public void writeValueDouble(@NotNull OutputStream out, @NotNull WriterContext context, double value) throws IOException, FormatException {
            writeValueLong(out, context, Double.doubleToLongBits(value));
        }

        @Override
        public void writeValueChar(@NotNull OutputStream out, @NotNull WriterContext context, char value) throws IOException {
            out.write(value >>> 8);
            out.write(value);
        }

        @Override
        public void writeArrayStart(@NotNull OutputStream out, @NotNull WriterContext context, int length) throws IOException, FormatException {
            writeValueInt(out, context, length);
        }

        @Override
        public void writeValueNull(@NotNull OutputStream out, @NotNull WriterContext context) {}
        @Override
        public void writeMapStart(@NotNull OutputStream out, @NotNull WriterContext context) {}
        @Override
        public void writeMapEnd(@NotNull OutputStream out, @NotNull WriterContext context) {}
        @Override
        public void writeArrayEnd(@NotNull OutputStream out, @NotNull WriterContext context, int length) {}
        @Override
        public void writeMappingSuffix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key,
                                       boolean optional, boolean valueAssigned, int index, boolean last) {}
        @Override
        public void writeEntryPrefix(@NotNull OutputStream out, @NotNull WriterContext context, int index, boolean last) {}
        @Override
        public void writeEntrySuffix(@NotNull OutputStream out, @NotNull WriterContext context, int index, boolean last) {}
        @Override
        public void writeRootStart(@NotNull OutputStream out, @NotNull WriterContext context) {}
        @Override
        public void writeRootEnd(@NotNull OutputStream out, @NotNull WriterContext context) {}


        @Override
        public void flush() {}

        @Override
        public void close() {}

    };

    private static final FormatReader BINARY_FORMAT_READER = new FormatReader() {

        public static final String EXTRA_ARR_LENGTH_PREFIX = "extra_arr_length$";
        public static final String EXTRA_ARR_INDEX_PREFIX = "extra_arr_index$";

        @Override
        public @NotNull String nextKeyToken(@NotNull InputStream in, @NotNull ReaderContext context, @NotNull String expectedKey) {
            return expectedKey;
        }

        @Override
        public @Nullable ValueToken nextValueToken(@NotNull InputStream in, @NotNull ReaderContext context, String key,
                                                   @NotNull Class<?> valueType, boolean optional, boolean inArray) throws IOException, FormatException {
            boolean isLast;
            if(inArray){
                int depth = context.getDepth();
                final String extraArrLengthKey = EXTRA_ARR_LENGTH_PREFIX + depth;
                final String extraArrIndexKey = EXTRA_ARR_INDEX_PREFIX + depth;

                Integer length = context.getExtra(extraArrLengthKey);
                Integer index = context.getExtra(extraArrIndexKey);
                if(index == null){
                    index = 0;
                    length = readInt(in);
                    context.putExtra(extraArrLengthKey, length);
                    context.putExtra(extraArrIndexKey, index);
                } else {
                    index++;
                    context.putExtra(extraArrIndexKey, index);
                }
                if(index >= length){
                    return null;
                }
                isLast = index == length - 1;
                if(isLast){
                    context.removeExtra(extraArrLengthKey);
                    context.removeExtra(extraArrIndexKey);
                }
            } else {
                isLast = false;
            }

            if(optional){
                if(readByte(in) == PREFIX_NULL){
                    return new ValueToken(null, isLast);
                }
            }

            //Read value based on a type
            if(valueType == String.class){
                return new ValueToken(readString(in), isLast);
            } else if(valueType == Boolean.class){
                return new ValueToken(readBoolean(in), isLast);
            } else if(valueType == Byte.class){
                return new ValueToken(readByte(in), isLast);
            } else if(valueType == Short.class){
                return new ValueToken(readShort(in), isLast);
            } else if(valueType == Integer.class){
                return new ValueToken(readInt(in), isLast);
            } else if(valueType == Long.class){
                return new ValueToken(readLong(in), isLast);
            } else if(valueType == Float.class){
                return new ValueToken(readFloat(in), isLast);
            } else if(valueType == Double.class){
                return new ValueToken(readDouble(in), isLast);
            } else if(valueType == Character.class){
                return new ValueToken(readChar(in), isLast);
            } else if(UMap.class.isAssignableFrom(valueType)){
                return ValueToken.map(isLast);
            } else if(UArray.class.isAssignableFrom(valueType)){
                return ValueToken.array(isLast);
            } else {
                throw new FormatException("Unsupported value type: " + valueType);
            }
        }

        @Override
        public void readRootStart(@NotNull InputStream in, @NotNull ReaderContext context) {}
        @Override
        public void readRootEnd(@NotNull InputStream in, @NotNull ReaderContext context) {}

        @Override
        public void close() {}

        private int readInt(@NotNull InputStream in) throws IOException, FormatException {
            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            int b4 = in.read();
            if(b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1){
                throw new FormatException("Unexpected end of stream");
            }
            return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
        }
        private long readLong(@NotNull InputStream in) throws IOException, FormatException {
            long b1 = in.read();
            long b2 = in.read();
            long b3 = in.read();
            long b4 = in.read();
            long b5 = in.read();
            long b6 = in.read();
            long b7 = in.read();
            long b8 = in.read();
            if(b1 == -1 || b2 == -1 || b3 == -1 || b4 == -1 || b5 == -1 || b6 == -1 || b7 == -1 || b8 == -1){
                throw new FormatException("Unexpected end of stream");
            }
            return (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
        }
        private short readShort(@NotNull InputStream in) throws IOException, FormatException {
            int b1 = in.read();
            int b2 = in.read();
            if(b1 == -1 || b2 == -1){
                throw new FormatException("Unexpected end of stream");
            }
            return (short)((b1 << 8) | b2);
        }
        private char readChar(@NotNull InputStream in) throws IOException, FormatException {
            int b1 = in.read();
            int b2 = in.read();
            if(b1 == -1 || b2 == -1){
                throw new FormatException("Unexpected end of stream");
            }
            return (char)((b1 << 8) | b2);
        }
        private byte readByte(@NotNull InputStream in) throws IOException, FormatException {
            int b = in.read();
            if(b == -1){
                throw new FormatException("Unexpected end of stream");
            }
            return (byte)b;
        }
        private float readFloat(@NotNull InputStream in) throws IOException, FormatException {
            return Float.intBitsToFloat(readInt(in));
        }
        private double readDouble(@NotNull InputStream in) throws IOException, FormatException {
            return Double.longBitsToDouble(readLong(in));
        }
        private boolean readBoolean(@NotNull InputStream in) throws IOException, FormatException {
            return readByte(in) != 0;
        }

        private String readString(@NotNull InputStream in) throws IOException, FormatException {
            int length = readInt(in);
            byte[] bytes = new byte[length];
            int read = in.read(bytes);
            if(read == -1){
                throw new FormatException("Unexpected end of stream");
            }
            if(read != length){
                throw new FormatException("Unexpected end of stream");
            }
            return new String(bytes);
        }

    };


}
