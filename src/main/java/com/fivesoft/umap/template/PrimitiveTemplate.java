package com.fivesoft.umap.template;

import com.fivesoft.umap.TypeUtils;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.data.Validator;
import com.fivesoft.umap.exception.FieldException;
import com.fivesoft.umap.format.Format;
import com.fivesoft.umap.format.FormatException;
import com.fivesoft.umap.format.FormatReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


public final class PrimitiveTemplate extends Template {

    private final @NotNull Class<?> type;
    private final @Nullable Validator validator;

    /**
     * Creates a new primitive template with a given type and a default value.
     * @param type the type of the value must not be null
     * @param validator validator for the value or null if no specific requirements are set for the value
     * @throws IllegalArgumentException if the type is not supported by UMap,
     * or if the default value is rejected by the validator
     * @throws NullPointerException if the type is null
     */
    public PrimitiveTemplate(@NotNull Class<?> type,
                             @Nullable Validator validator) {
        Objects.requireNonNull(type, "Type cannot be null");

        if (!UMap.isSupportedType(type))
            throw new IllegalArgumentException("Unsupported type: " + type.getName());

        if(validator == null && type.isEnum()){
            //Default validator for enums
            //noinspection unchecked
            validator = Validator.oneOf((Class<? extends Enum<?>>) type);
        }

        this.type = type;
        this.validator = validator;
    }

    /**
     * Gets a java type of values that this template can hold.
     * @return type of values that this template can hold
     */
    @NotNull
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets a validator object for this template.<br>
     * Validator is used to check if the value meets the requirements of the template,
     * if null, no validation is performed.
     * @return validator object for this template or null if no specific requirements are set for the value
     */
    @Nullable
    public Validator getValidator() {
        return validator;
    }

    /**
     * Parses the given value to the type of this template and checks if it meets the requirements of the template.<br>
     * Parsing is performed according to the following rules:
     * <ul>
     *     <li>If the value is already of the type of this template, it is returned as is.</li>
     *     <li>If the value is a string, it is parsed to the type of this template.</li>
     *     <li>If the value is of any other type (also these unsupported by UMap), but other than template type,
     *     the value is converted to string and then parsed to expected type.</li>
     *     <li>If the value cannot be parsed to the expected type, {@link FieldException} is thrown.</li>
     * </ul>
     * @param value value to parse, must not be null
     * @param key the key name, where the value is located, may be null. Used for error messages, when thrown {@link FieldException}.
     * @return parsed value, never null
     * @throws FieldException when the value cannot be parsed to the expected type
     * or does not meet the requirements of the template validator.
     * @throws NullPointerException if the value is null
     */
    public Object parseValue(@NotNull Object value,
                             @Nullable String key) throws FieldException {
        Objects.requireNonNull(value);

        Class<?> objType = value.getClass();
        if(type.equals(objType)){
            String err = Validator.validate(validator, value);
            if(err != null)
                throw new FieldException(FieldException.Reason.INVALID_VALUE, key, err);
            return value;
        }

        if(objType.equals(String.class)) {
            //Normalize value
            value = ((String) value).replaceAll("\\s+", ""); //Remove all whitespaces
            if(type.isEnum()) {
                try {
                    //noinspection unchecked,rawtypes
                    return Enum.valueOf((Class<? extends Enum>) type, (String) value);
                } catch (IllegalArgumentException e) {
                    throw new FieldException(FieldException.Reason.INVALID_VALUE, key, value);
                }
            }
        }

        //Parse primitives
        try {
            if(type.equals(Integer.class) || type.equals(int.class)) {
                return TypeUtils.getInt(value);
            } else if(type.equals(Long.class) || type.equals(long.class)) {
                return TypeUtils.getLong(value);
            } else if(type.equals(Double.class) || type.equals(double.class)) {
                return TypeUtils.getDouble(value);
            } else if(type.equals(Float.class) || type.equals(float.class)) {
                return TypeUtils.getFloat(value);
            } else if(type.equals(Boolean.class) || type.equals(boolean.class)) {
                return TypeUtils.getBoolean(value);
            } else if(type.equals(Byte.class) || type.equals(byte.class)) {
                return TypeUtils.getByte(value);
            } else if(type.equals(Short.class) || type.equals(short.class)) {
                return TypeUtils.getShort(value);
            } else if(type.equals(Character.class) || type.equals(char.class)) {
                return TypeUtils.getChar(value);
            }
        } catch (TypeUtils.TypeException e) {
            //Ignore, will throw an exception below
        }
        throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, key, "Expected type: " + type.getName());
    }

    /**
     * Equivalent to {@code parseValue(value, null)}.<br>
     * See related method for more details.
     * @param value value to parse, must not be null
     * @return parsed value, never null
     * @throws FieldException when the value cannot be parsed to the expected type
     * @throws NullPointerException if the value is null
     * @see #parseValue(Object, String)
     */
    public Object parseValue(@NotNull Object value) throws FieldException {
        return parseValue(value, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object readFormat(@NotNull InputStream in,
                             @NotNull Format format,
                             @Nullable FormatReader.Options options) throws IOException, FormatException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesTemplate(@NotNull Template another) {
        if (another instanceof PrimitiveTemplate t) {
            return t.type.equals(type) &&
                    Objects.equals(t.validator, validator);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesValue(@NotNull Object value) {
        return type.isInstance(value) &&
                Validator.validate(validator, value) == null;
    }

    @Override
    public long getComplexity() {
        return 1; //Primitive templates have complexity of 1
    }

}
