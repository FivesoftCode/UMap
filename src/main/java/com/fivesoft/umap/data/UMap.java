package com.fivesoft.umap.data;

import com.fivesoft.umap.exception.FieldException;
import com.fivesoft.umap.object.UMapObject;
import com.fivesoft.umap.template.MapTemplate;
import com.fivesoft.umap.template.Mapping;
import com.fivesoft.umap.template.PrimitiveTemplate;
import com.fivesoft.umap.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;

public abstract class UMap extends UObject<MapTemplate>
        implements Iterable<Map.Entry<String, Object>> {

    public static final Pattern VALID_KEY_REGEX = Pattern.compile("^[a-z_][a-zA-Z0-9_]{0,63}$");

    /**
     * List of supported non-primitive types by UMap.
     */
    public static List<Class<?>> SUPPORTED_NON_PRIMITIVE_TYPES =
            Arrays.asList(
                    String.class,
                    Integer.class,
                    Long.class,
                    Double.class,
                    Float.class,
                    Boolean.class,
                    Byte.class,
                    Short.class,
                    Character.class,
                    UMap.class,
                    UArray.class
            );

    protected UMap(@NotNull MapTemplate template) {
        super(template);
    }

    /**
     * Gets the value associated with the given, non-optional key.<br>
     * Guaranteed to return a non-null value of the type specified in the template for the given key.
     *
     * @param key The key to get the value of.
     * @param <T> type of the value
     * @return The value associated with the given key.
     * @throws IllegalArgumentException If there is no such key or the key is optional.
     * @throws NullPointerException     If the key is null
     */
    @NotNull
    public abstract <T> T getRequired(@NotNull String key) throws IllegalArgumentException;

    /**
     * Gets the value associated with the given, optional key or default
     * value if there is no value present for this key. May return null when default value is null.<br>
     * <b>Important note: </b> key must be optional, if the key is required, an exception will be thrown.
     *
     * @param key The key to get the value of.
     * @param <T> type of the value
     * @return The value associated with the given key or default value if there is no value present for this key.
     * @throws IllegalArgumentException If there is no such key or the key is not optional.
     * @throws NullPointerException     If the key is null
     */
    public abstract <T> T getOptional(@NotNull String key) throws IllegalArgumentException;

    /**
     * Checks whether the given type is supported by UMap.
     *
     * @param type type to check
     * @return true if the given type is supported by UMap
     */

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSupportedType(@NotNull Class<?> type) {
        return type.isPrimitive() || type.isEnum() ||
                SUPPORTED_NON_PRIMITIVE_TYPES.contains(type) ||
                UMap.class.isAssignableFrom(type) ||
                UArray.class.isAssignableFrom(type);
    }

    /**
     * Checks whether the given object is supported by UMap.<br>
     *
     * @param obj object to check
     * @return true if the given object is supported by UMap
     */
    public static boolean isSupportedObject(@Nullable Object obj) {
        if (obj == null) return true;
        if (!isSupportedType(obj.getClass())) return false;
        if (obj instanceof Double && !Double.isFinite((double) obj)) return false;
        return !(obj instanceof Float) || !(Float.isFinite((float) obj));
    }

    /**
     * Checks whether the given key is valid for UMap.<br>
     * Must meet the following requirements:<br>
     * <ul>
     *     <li>Must start with a lowercase letter or underscore</li>
     *     <li>May contain English letters, digits, and underscores</li>
     *     <li>Length must be between 1 and 64 characters</li>
     *     <li>Cannot be equal to: <code>"_"</code></li>
     * </ul>
     * @param key key to check
     * @throws NullPointerException if the key is null
     * @return true if the given key is valid for UMap
     */
    public static boolean isValidKey(@NotNull String key) {
        Objects.requireNonNull(key, "key cannot be null");
        // Check if the key matches the regex and is not exactly "_"
        return !key.equals("_") &&
                VALID_KEY_REGEX.matcher(key).matches();
    }

    @NotNull
    public static UMap wrap(@NotNull UMapObject obj) {
        return new ObjectUMapImpl(obj);
    }

    /**
     * Checks whether the given key exists in this UMap.
     *
     * @param key The key to check.
     * @return Whether the key exists.
     */
    public boolean containsKey(@NotNull String key) {
        return getTemplate().containsKey(key);
    }

    /**
     * Builder for creating UMap objects.<br>
     * This is the only official way to create UMap objects.
     */
    public static final class Builder extends UObject.Builder<MapTemplate> {

        private final LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        private final LinkedHashSet<String> missingKeys; //Keys that are not yet set. Must be linked to preserve order.

        //Cache if the map has builders, to avoid unnecessary map iteration when building
        private boolean hasBuilders = false;

        /**
         * Whether to ignore unknown keys while building the map.<br>
         */
        private final boolean ignoreUnknownKeys;

        private final boolean ignoreDuplicateKeys;

        /**
         * Creates UMap builder based on the given template.
         *
         * @param template            The template to use.
         * @param ignoreUnknownKeys   Whether to ignore unknown keys while building the map.<br>
         *                            When disabled, an exception will be thrown if an unknown key is encountered.
         * @param ignoreDuplicateKeys Whether to ignore duplicate keys while building the map.<br>
         * @throws NullPointerException If the template is null.
         */
        public Builder(@NotNull MapTemplate template,
                       boolean ignoreUnknownKeys, boolean ignoreDuplicateKeys) {
            super(template);
            this.ignoreUnknownKeys = ignoreUnknownKeys;
            this.ignoreDuplicateKeys = ignoreDuplicateKeys;
            this.missingKeys = new LinkedHashSet<>(template.getMappings().keySet());
        }

        /**
         * Creates UMap builder based on the given template. Ignore duplicate keys are disabled by default.
         *
         * @param template          The template to use.
         * @param ignoreUnknownKeys Whether to ignore unknown keys while building the map.<br>
         * @throws NullPointerException If the template is null.
         */
        public Builder(@NotNull MapTemplate template, boolean ignoreUnknownKeys) {
            this(template, ignoreUnknownKeys, false);
        }

        /**
         * Creates UMap builder based on the given template. Ignore unknown
         * keys and duplicate keys are disabled by default.
         *
         * @param template The template to use.
         * @throws NullPointerException If the template is null.
         */

        public Builder(@NotNull MapTemplate template) {
            this(template, false, false);
        }

        /**
         * Assigns the given value to the given key.<br>
         * If the given key is not present in the template, an exception will be thrown.<br>
         *
         * @param key   The key to assign the value to.
         * @param value The value to assign. Must have an exact type as specified in the template.<br>
         * @return This builder for chaining.
         * @throws FieldException If any field error occurs.
         */
        public Builder set(@NotNull String key, @Nullable Object value) throws FieldException {
            if (!isSupportedObject(value) && !(value instanceof UObject.Builder<?>))
                throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, key, value);

            Mapping m = template.get(key);
            if (m == null) {
                if (ignoreUnknownKeys) return this;
                else throw new FieldException(FieldException.Reason.UNKNOWN_KEY, key);
            }
            Template t = m.getTemplate();

            if (value == null) {
                if (!m.isOptional()) //Nulls only allowed for optional fields
                    throw new FieldException(FieldException.Reason.MISSING_VALUE, key);
                //Do not add nulls to the map, optional fields may not be present
                //Remove the key from missing keys
                missingKeys.remove(key);
                return this;
            }

            if (t instanceof PrimitiveTemplate) {
                //Try parsing the value to the expected type when lenient typing is enabled
                value = ((PrimitiveTemplate) t).parseValue(value, key);
            } else if (value instanceof UObject.Builder<?> b) {
                if(!t.matchesValue(b.getTemplate()))
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, key, value);
                hasBuilders = true;
            } else if (!t.matchesValue(value)) {
                //Value does not match the expected type
                throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, key, value);
            }

            missingKeys.remove(key);
            if (data.put(key, value) != null && !ignoreDuplicateKeys) {
                throw new FieldException(FieldException.Reason.DUPLICATED_KEY, key);
            }
            return this;
        }

        /**
         * Builds the UMap with the given data.<br>
         * If any required fields specified in the template are not present, an exception will be thrown.
         *
         * @return The built UMap.
         * @throws FieldException If any required field is missing.
         */
        public @NotNull UMap build() throws FieldException {
            //Build objects from builders
            if (hasBuilders) {
                for (Map.Entry<String, Object> e : data.entrySet()) {
                    if (e.getValue() instanceof UObject.Builder<?> b) {
                        data.put(e.getKey(), b.build());
                    }
                }
            }
            //Check if all required fields are present
            for (String key : missingKeys) {
                Mapping e = template.get(key);
                if (e == null)
                    throw new RuntimeException("Internal error: missing key in template. Please report this issue.");
                if (!e.isOptional() && !data.containsKey(key))
                    throw new FieldException(FieldException.Reason.MISSING_KEY, key);
            }
            return new LinkedUMapImpl(template, data);
        }

        /**
         * Returns first missing key in the template or null if all keys are set.
         * @return first missing key in the template or null if all keys are set
         */
        @Nullable
        public String nextKey() {
            return missingKeys.isEmpty() ? null : missingKeys.iterator().next();
        }

    }

    private static class LinkedUMapImpl extends UMap {
        private final Map<String, Object> data;

        private LinkedUMapImpl(@NotNull MapTemplate template,
                               LinkedHashMap<String, Object> src) { //LinkedHashMap preserves order of keys IMPORTANT!!!
            super(template);
            this.data = Collections.unmodifiableMap(src);
        }

        /**
         * {@inheritDoc}
         */
        @NotNull
        public <T> T getRequired(@NotNull String key) throws IllegalArgumentException {
            Objects.requireNonNull(key, "key cannot be null");
            Mapping m = getTemplate().get(key);
            if (m == null) throw new IllegalArgumentException("No such key: " + key);
            if (m.isOptional())
                throw new IllegalArgumentException("Key is optional: " + key);
            Object o = data.get(key);
            if (o == null) //Should not happen, null checks are done in the builder
                throw new RuntimeException("Internal error: null value for non-optional key. Please report this issue.");
            //noinspection unchecked
            return (T) o;
        }

        /**
         * {@inheritDoc}
         */
        public <T> T getOptional(@NotNull String key) throws IllegalArgumentException {
            Objects.requireNonNull(key, "key cannot be null");
            Mapping m = getTemplate().get(key);
            if (m == null) throw new IllegalArgumentException("No such key: " + key);
            if (!m.isOptional()) throw new IllegalArgumentException("Key is not optional: " + key);
            Object o = data.get(key);
            //noinspection unchecked
            return (T) (o == null ? m.getDefaultValue() : o);
        }

        /**
         * {@inheritDoc}
         */
        public int size() {
            return data.size();
        }

        /**
         * {@inheritDoc}
         */
        @NotNull
        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return data.entrySet().iterator();
        }

    }

    private static class ObjectUMapImpl extends UMap {

        private final UMapObject obj;

        protected ObjectUMapImpl(@NotNull UMapObject obj) {
            super(Objects.requireNonNull(obj).getTemplate());
            this.obj = obj;
        }

        @Override
        public <T> @NotNull T getRequired(@NotNull String key) throws IllegalArgumentException {
            return null;
        }

        @Override
        public <T> T getOptional(@NotNull String key) throws IllegalArgumentException {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @NotNull
        @Override
        public Iterator<Map.Entry<String, Object>> iterator() {
            return null;
        }
    }

}
