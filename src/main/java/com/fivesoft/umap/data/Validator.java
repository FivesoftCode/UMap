package com.fivesoft.umap.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A validator for checking if an object is valid.
 * @param min The minimum value of the object for integer values. (inclusive)
 * @param max The maximum value of the object for integer values. (inclusive)
 * @param minF The minimum value of the object for float values. (inclusive)
 * @param maxF The maximum value of the object for float values. (inclusive)
 * @param minLength The minimum length of the object for string values. (inclusive)
 * @param maxLength The maximum length of the object for string values. (inclusive)
 * @param regex The regex the object must match for string values.
 * @param oneOf The values the object must be for string values.
 * @param caseSensitive Whether the regex and oneOf checks are case-sensitive.
 */
public record Validator(long min, long max, double minF, double maxF, long minLength, long maxLength,
                        @Nullable String regex, @Nullable String[] oneOf, boolean caseSensitive) {

    /**
     * Checks if an object is valid according to the validator rules.<br>
     * Supported types are:
     * <ul>
     *     <li>All primitives</li>
     *     <li>String</li>
     *     <li>All java primitive wrapper classes</li>
     * </ul>
     * When unsupported types are passed, they will be ignored.
     * @param value The object to check. Cannot be null.
     * @return A string describing the error, or null if there is no error.
     * @throws NullPointerException If the object is null.
     */

    @Nullable
    public String validate(@NotNull Object value) {
        Objects.requireNonNull(value, "Value cannot be null");

        if (value instanceof String) {
            //Check length, regex and oneOf
            String v = (String) value;
            if (v.length() < minLength || v.length() > maxLength)
                return "value length must be between " + minLength + " and " + maxLength;

            if (regex != null && !v.matches(regex))
                return "value does not match regex: " + regex;

            if (oneOf != null) {
                boolean found = false;
                for (String s : oneOf) {
                    if (Objects.equals(s, v)) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    return "value must be one of: " + String.join(", ", oneOf);
            }
        } else if (value instanceof Number) {
            Number v = (Number) value;
            if (v instanceof Float || v instanceof Double) {
                if (v.doubleValue() < minF || v.doubleValue() > maxF)
                    return "value must be between " + minF + " and " + maxF;
            } else {
                if (v.longValue() < min || v.longValue() > max)
                   return "value must be between " + min + " and " + max;
            }
        }
        return null; //No errors
    }

    /**
     * Equivalent to {@link #validate(Object)} with null checks for validator and value.<br>
     * When either validator or value is null, null will be returned (no error).
     * @param validator The validator to use.
     * @param value The value to validate.
     * @return A string describing the error, or null if there is no error.
     */

    @Nullable
    public static String validate(@Nullable Validator validator, @Nullable Object value) {
        if(validator == null || value == null)
            return null;
        return validator.validate(value);
    }

    /**
     * Creates a new validator for integer values.
     * @param min The minimum value of the object. (Inclusive)
     * @param max The maximum value of the object. (Inclusive)
     * @return The created validator.
     */
    public static Validator range(long min, long max) {
        return new Validator(min, max, 0, 0, 0, 0, null, null, false);
    }

    /**
     * Creates a new validator for float values.
     * @param min The minimum value of the object. (Inclusive)
     * @param max The maximum value of the object. (Inclusive)
     * @return The created validator.
     */
    public static Validator range(double min, double max) {
        return new Validator(0, 0, min, max, 0, 0, null, null, false);
    }

    /**
     * Creates a new validator for string values.
     * @param min The minimum length of the string. (Inclusive)
     * @param max The maximum length of the string. (Inclusive)
     * @return The created validator.
     */
    public static Validator length(long min, long max) {
        return new Validator(0, 0, 0, 0, min, max, null, null, false);
    }

    /**
     * Creates a new validator for string values with enabled case sensitivity.
     * @param regex The regex the string must match.
     * @return The created validator.
     */
    public static Validator regex(@NotNull String regex) {
        return new Validator(0, 0, 0, 0, 0, 0, regex, null, true);
    }

    /**
     * Creates a new validator for string values.
     * @param caseSensitive Whether the regex and oneOf checks are case-sensitive.
     * @param regex The regex the string must match.
     * @return The created validator.
     */
    public static Validator regex(boolean caseSensitive, @NotNull String regex) {
        return new Validator(0, 0, 0, 0, 0, 0, regex, null, caseSensitive);
    }

    /**
     * Creates a new validator for string values with enabled case sensitivity.
     * @param values The values the string must be one of.
     * @return The created validator.
     */
    public static Validator oneOf(@NotNull String... values) {
        return new Validator(0, 0, 0, 0, 0, 0, null, values, true);
    }

    /**
     * Creates a new validator for string values.
     * @param caseSensitive Whether oneOf checks are case-sensitive.
     * @param values The values the string must be one of.
     * @return The created validator.
     */
    public static Validator oneOf(boolean caseSensitive, @NotNull String... values) {
        return new Validator(0, 0, 0, 0, 0, 0, null, values, caseSensitive);
    }

    /**
     * Creates a new validator for enum values.<br>
     * Similar to {@link #oneOf(String...)} but uses enum constants as values.<br>
     * Case sensitivity is enabled.
     * @param enumClass The enum class to use.
     * @return The created validator.
     */
    public static Validator oneOf(@NotNull Class<? extends Enum<?>> enumClass) {
        Enum<?>[] constants = enumClass.getEnumConstants();
        String[] values = new String[constants.length];
        for (int i = 0; i < constants.length; i++) {
            values[i] = constants[i].name();
        }
        return new Validator(0, 0, 0, 0, 0, 0, null, values, true);
    }


}
