package com.fivesoft.umap.exception;

import com.fivesoft.umap.data.Null;
import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.data.UMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class FieldException extends RuntimeException {

    public static final int DEFAULT_STRING_LIMIT = 256;

    /**
     * The key of the field that caused the error.
     */
    @Nullable
    public String key;

    /**
     * The reason why the field caused the error.
     */
    @NotNull
    public final Reason reason;

    @Nullable
    public final String valueString;

    /**
     * A message that describes the error in more detail.
     */
    @Nullable
    public final String details;

    /**
     * Creates a new FieldError instance.
     * @param key of the field that caused the error
     * @param reason why the field caused the error
     * @param details of the error
     */
    public FieldException(@NotNull Reason reason, @Nullable String key,
                          @Nullable Object value, @Nullable String details) {
        this.key = key;
        this.reason = Objects.requireNonNull(reason);
        this.valueString = objToString(value, DEFAULT_STRING_LIMIT);
        this.details = details;
    }

    /**
     * Creates a new FieldError instance with no details.
     * @param key of the field that caused the error
     * @param reason why the field caused the error
     * @param value that caused the error
     */
    public FieldException(@NotNull Reason reason, @Nullable String key, @Nullable Object value) {
        this(reason, key, value, null);
    }

    /**
     * Creates a new FieldError instance with no details and no value.
     * @param key of the field that caused the error
     * @param reason why the field caused the error
     */
    public FieldException(@NotNull Reason reason, @Nullable String key) {
        this(reason, key, null);
    }

    /**
     * Creates a new FieldError instance with only the reason specified.
     * @param reason why the field caused the error
     */

    public FieldException(@NotNull Reason reason) {
        this(reason, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return "Field: " + objToString(key, 128) + "; " +
                "Reason: " + reason + ";" +
                (valueString != null ? " Value: " + valueString + ";" : "") +
                (details != null ? " Details: " + details + ";" : "");
    }

    private static String objToString(@Nullable Object value, int stringLimit) {
        if(value == null)
            return null;

        if(value instanceof Null)
            return "[null]";

        if(value instanceof String s){
            if(s.length() > stringLimit)
                return "\"" + s.substring(0, stringLimit) + "\"...";
            return "\"" + s + "\"";
        }

        if(value.getClass().isPrimitive() || value instanceof Number || value instanceof Boolean)
            return value.toString();

        if(value instanceof UMap)
            return "[Map]";

        if(value instanceof UArray)
            return "[Array]";

        return "[Unknown]";
    }

    public enum Reason {
        MISSING_KEY,
        DUPLICATED_KEY,
        MISSING_VALUE,
        UNKNOWN_KEY,
        VALUE_TYPE_MISMATCH,
        INVALID_VALUE
    }

}
