package com.fivesoft.umap.template;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents entry of a {@link MapTemplate}.<br>
 * A mapping is a pair of a key and a template where:
 * <ul>
 *     <li>key - contains value access information such as name, detailLevel and optional flag</li>
 *     <li>template - contains value type information</li>
 * </ul>
 * Also stores a default value for the key.
 */
public final class Mapping {

    private final @NotNull Key key;
    private final @NotNull Template template;
    private final @Nullable Object defaultValue;

    /**
     * Creates a new mapping. The key and template must not be null.
     * @param key the key of the mapping
     * @param template the template mapped to the key
     * @param defaultValue the default value of the mapping
     * @throws NullPointerException if the key or template is null
     * @throws IllegalArgumentException if defaultValue does not match the template
     */
    public Mapping(@NotNull Key key, @NotNull Template template, @Nullable Object defaultValue) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(template);
        this.key = key;
        this.template = template;
        if (defaultValue == null || template.matchesValue(defaultValue)) {
            this.defaultValue = defaultValue;
        } else {
            throw new IllegalArgumentException("Default value does not match the template");
        }
    }

    /**
     * Creates a new mapping. The key and template must not be null.
     * @param key the key of the mapping
     * @param template the template mapped to the key
     */
    public Mapping(@NotNull Key key, @NotNull Template template) {
        this(key, template, null);
    }

    /**
     * Constructor with manual key creation. The name must not be null.
     * @param name the name of the key
     * @param detailLevel the detail level of the key
     * @param optional true if the key is optional
     * @param template the template mapped to the key
     */
    public Mapping(@NotNull String name, int detailLevel, boolean optional,
                   @NotNull Template template) {
        this(new Key(name, detailLevel, optional), template);
    }

    /**
     * Gets the key of this mapping
     * @return the key of this mapping
     */
    public @NotNull Key getKey() {
        return key;
    }

    /**
     * Gets the template mapped to the key
     * @return the template mapped to the key
     */
    public @NotNull Template getTemplate() {
        return template;
    }

    /**
     * Gets a default value for this mapping.<br>
     * Default value is only used for optional keys, when the key is not present in the data.
     * @return default value for this mapping or null if no default value is set
     */
    public @Nullable Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Delegate method to {@link Key#isOptional()}
     * @return true if the key is optional
     */
    public boolean isOptional() {
        return key.isOptional();
    }

    /**
     * Delegate method to {@link Key#getDetailLevel()}
     * @return the detail level of the key
     */
    public int getDetailLevel() {
        return key.getDetailLevel();
    }

    /**
     * Delegate method to {@link Key#getName()}
     * @return the name of the key
     */
    @NotNull
    public String getName() {
        return key.getName();
    }

    /**
     * Delegate method to {@link Template#getComplexity()}
     * @return the complexity of the template
     */
    public long getComplexity() {
        return template.getComplexity();
    }

    /**
     * Checks equality of this mapping key and template with another object.
     * @param obj the object to compare with
     * @return true if the given object is equal to this mapping
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Mapping) obj;
        return Objects.equals(this.key, that.key) &&
                Objects.equals(this.template, that.template);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, template);
    }

    @Override
    public String toString() {
        return "Mapping[" +
                "key=" + key + ", " +
                "template=" + template + ']';
    }
}
