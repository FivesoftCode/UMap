package com.fivesoft.umap.format;

import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.exception.FieldException;
import com.fivesoft.umap.template.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class FormatReader implements AutoCloseable {

    /**
     * Reads the start of the root object from the input stream.<br>
     * This will always be called once while single reading operation as the first method.
     *
     * @param in      the input stream to read from
     * @param context for the current reading operation. Implementation may use this to access options,
     *                other data and store and retrieve custom temporary extras.
     * @throws IOException     if an I/O error occurs while reading from the input stream
     * @throws FormatException if the root start token cannot be read since it is not in the expected format
     */
    public abstract void readRootStart(@NotNull InputStream in, @NotNull ReaderContext context) throws IOException, FormatException;

    /**
     * Advances the input stream to the next key token and returns it.<br>
     *
     * @param in          the input stream to read from
     * @param context     for the current reading operation. Implementation may use this to access options,
     *                    other data and store and retrieve custom temporary extras.
     * @param expectedKey the key that is expected to be read next.
     *                    Typically used by formats, which does not store exact
     *                    key names and relies on templates. (for example, binary formats)
     * @return the key read from the input stream or null, when there are no more keys to read
     * @throws IOException     if an I/O error occurs while reading from the input stream
     * @throws FormatException if the key token cannot be read since it is not in the expected format
     */
    @Nullable
    public abstract String nextKeyToken(@NotNull InputStream in, @NotNull ReaderContext context,
                                        @NotNull String expectedKey) throws IOException, FormatException;

    /**
     * Advances the input stream to the next value token and returns it.<br>
     * The returned token may be a value token, or a special token indicating the start of a map or array.<br>
     * When returning token with value, it must be a {@link PrimitiveTemplate} value, NOT complex UMap object like {@link UMap} or {@link UArray}.<br>
     * The returned value may have a type different from the expected type, but it must be convertible to it.<br>
     * For example:
     * <ul>
     *     <li>When expectedType is <code>Integer.class</code> returned value may be <code>(int) 5</code>, but also can be <code>(String) "5"</code> or (double) 5.0</li>
     *     <li>When expectedType is <code>Integer.class</code> returned value cannot be <code>(String) "dog"</code>, because it's not convertible to integer.<br>
     *     In such case, {@link FieldException} will be thrown with reason: {@link FieldException.Reason#VALUE_TYPE_MISMATCH} by {@link FormatReader}.</li>
     * </ul>
     *
     * @param in        the input stream to read from
     * @param context   for the current reading operation.
     *                  Implementation may use this to access options,
     *                  other data and store and retrieve custom temporary extras.
     * @param key       of the current mapping to read value token for
     * @param valueType type of the value to read. Will be one of supported by UMap types.
     *                  (see {@link UMap#isSupportedType(Class)})
     * @param optional  whether mapping at the <code>key</code> is optional.
     *                  Keep in mind that optional mappings may take null values.
     * @return the value token read from the input stream or one of the following special tokens:
     * <ul>
     *     <li>{@link ValueToken#map(boolean)} if the value is a map, and its start token has just been read</li>
     *     <li>{@link ValueToken#array(boolean)} if the value is an array, and its start token has just been read</li>
     * </ul>
     * <b>Note: </b> returned token will be null if there are no more values to read.
     * @throws IOException     if an I/O error occurs while reading from the input stream
     * @throws FormatException if the value token cannot be read since it is not in the expected format
     */

    @Nullable
    public abstract ValueToken nextValueToken(@NotNull InputStream in, @NotNull ReaderContext context,
                                              String key, @NotNull Class<?> valueType, boolean optional,
                                          boolean inArray) throws IOException, FormatException;

    /**
     * Reads the end of the root object from the input stream.<br>
     * This will always be called once while single reading operation as the last, closing method.
     *
     * @param in      the input stream to read from
     * @param context for the current reading operation. Implementation may use this to access options,
     *                other data and store and retrieve custom temporary extras.
     * @throws IOException     if an I/O error occurs while reading from the input stream
     * @throws FormatException if the root end token cannot be read since it is not in the expected format
     */
    public abstract void readRootEnd(@NotNull InputStream in, @NotNull ReaderContext context) throws IOException, FormatException;


    @NotNull
    public static UMap readFormat(@NotNull InputStream in,
                                  @NotNull FormatReader reader,
                                  @NotNull MapTemplate template,
                                  @NotNull Options options) throws IOException, FormatException {
        Objects.requireNonNull(in, "Input stream cannot be null");
        Objects.requireNonNull(reader, "Format reader cannot be null");
        Objects.requireNonNull(template, "Template cannot be null");
        Objects.requireNonNull(options, "Options cannot be null");
        return readFormat(in, reader, template, new ReaderContext(options), true);
    }

    @NotNull
    private static UMap readFormat(@NotNull InputStream in,
                                   @NotNull FormatReader reader,
                                   @NotNull MapTemplate template,
                                   @NotNull ReaderContext context,
                                   boolean isRoot) throws IOException, FormatException {

        if (isRoot) {
            reader.readRootStart(in, context);
        }
        context.incrementDepth();
        int minDepth = context.getDepth();

        UMap.Builder b = new UMap.Builder(template);
        String expectedKey;
        String currentKey;
        Mapping m;
        Template t;
        while ((expectedKey = b.nextKey()) != null &&
                (currentKey = reader.nextKeyToken(in, context, expectedKey)) != null) {
            System.out.println("currentKey: " + currentKey);
            m = template.get(currentKey);
            //Make sure mapping exists, if not it seems to be an internal UMap error
            if (m == null) {
                throw new RuntimeException("Internal error: Mapping not found for key: " +
                        currentKey + ". Please report this bug.");
            }
            t = m.getTemplate();

            //Read value token
            ValueToken token = reader.nextValueToken(in, context, currentKey,
                    getExpectedValueType(m.getTemplate()), m.isOptional(), false);

            System.out.println("currentValueToken: " + token);

            if(token == null)
                return b.build();

            if (token.isMap()) {
                //Read a nested map
                if (t instanceof MapTemplate mt) {
                    b.set(currentKey, readFormat(in, reader, mt, context, false));
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, currentKey);
                }
            } else if (token.isArray()) {
                //Read a nested array
                if (t instanceof ArrayTemplate at) {
                    b.set(currentKey, readArray(in, reader, at, context, false));
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, currentKey);
                }
            } else if(UMap.isSupportedObject(token.value)){
                //Read primitive value
                if (t instanceof PrimitiveTemplate pt) {
                    if (token.value != null) {
                        b.set(currentKey, pt.parseValue(token.value, currentKey));
                    } else if (m.isOptional()){
                        b.set(currentKey, null);
                    } else {
                        throw new FieldException(FieldException.Reason.MISSING_VALUE, currentKey);
                    }
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, currentKey);
                }
            } else {
                throw new FormatImplException("Unsupported value token: '" + token +
                        "' returned by format: " + reader.getClass());
            }
            if (context.getDepth() < minDepth || token.isLast)
                return b.build();
        }
        context.decrementDepth();

        if (isRoot) {
            reader.readRootEnd(in, context);
        }
        return b.build();
    }

    private static UArray readArray(@NotNull InputStream in,
                                    FormatReader reader,
                                    ArrayTemplate template,
                                    ReaderContext context,
                                    boolean isRoot) throws IOException, FormatException {

        if (isRoot) {
            reader.readRootStart(in, context);
        }
        context.incrementDepth();
        int minDepth = context.getDepth();

        UArray.Builder b = new UArray.Builder(template);
        Template et = template.getEntryTemplate();

        int i = 0;
        ValueToken ct;
        while ((ct = reader.nextValueToken(in, context, null, getExpectedValueType(et),
                false, //UMap does not support null array values
                true)) != null) {
            System.out.println("currentValueToken arr: " + ct);
            if (ct.isMap()) {
                if(et instanceof MapTemplate mt) {
                    b.add(readFormat(in, reader, mt, context, false));
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, "#" + i);
                }
            } else if (ct.isArray()) {
                if (et instanceof ArrayTemplate at) {
                    b.add(readArray(in, reader, at, context, false));
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, "#" + i);
                }
            } else if (ct.value == null) {
                throw new FieldException(FieldException.Reason.INVALID_VALUE, "#" + i, "null entry in array");
            } else if (UMap.isSupportedObject(ct.value)) {
                if(et instanceof PrimitiveTemplate pt) {
                    b.add(pt.parseValue(ct.value, "#" + i));
                } else {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, "#" + i);
                }
            } else {
                throw new FormatImplException("Unsupported value token: '" + ct +
                        "' returned by format: " + reader.getClass());
            }
            if (context.getDepth() < minDepth || ct.isLast)
                return b.build();
        }

        context.decrementDepth();
        if (isRoot) {
            reader.readRootEnd(in, context);
        }
        return b.build();
    }


    private static Class<?> getExpectedValueType(Template t) {
        if (t instanceof PrimitiveTemplate pt) {
            return pt.getType();
        } else if (t instanceof MapTemplate) {
            return UMap.class;
        } else if (t instanceof ArrayTemplate) {
            return UArray.class;
        } else {
            throw new RuntimeException("Internal error: Unsupported template type: " + t.getClass().getName() +
                    ". Please report this bug.");
        }
    }

    public static class Options {

        @NotNull
        public final String encoding;
        public final boolean ignoreUnknownKeys;

        public Options(@NotNull String encoding, boolean ignoreUnknownKeys) {
            this.encoding = Objects.requireNonNull(encoding);
            this.ignoreUnknownKeys = ignoreUnknownKeys;
        }

        public Options(boolean ignoreUnknownKeys) {
            this("UTF-8", ignoreUnknownKeys);
        }

        public static FormatReader.Options getOrDefault(FormatReader.Options options) {
            return options != null ? options : new FormatReader.Options();
        }

        public Options() {
            this(false);
        }

    }

    public static final class MapStart {
        private MapStart() {
        }
    }

    public static final class ArrayStart {
        private ArrayStart() {
        }
    }

    public static final class ArrayEnd {
        private ArrayEnd() {
        }
    }

    public static final class RootStart {
        private RootStart() {
        }
    }

    public static final class RootEnd {
        private RootEnd() {
        }
    }

}
