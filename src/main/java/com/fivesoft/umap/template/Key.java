package com.fivesoft.umap.template;

import com.fivesoft.umap.data.UMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * Describes access to a field in a UMap.<br>
 * Provides information about the field's name, detail level and whether it is optional.<br>
 * All type related information is stored in a {@link Template}.
 */
public final class Key {
    private final @NotNull String name;
    private final @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel;
    private final boolean optional;

    /**
     * Creates a new key with the specified name, detail level and optional flag.<br>
     * @param name the name of the key. Must be a valid key name. (see {@link UMap#isValidKey(String)})
     * @param detailLevel the detail level of the key
     * @param optional true if the key is optional
     * @throws NullPointerException if the name is null
     * @throws IllegalArgumentException if the detail level is negative or the key name is invalid
     */
    public Key(@NotNull String name, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel, boolean optional) {
        //noinspection ConstantValue
        if (detailLevel < 0)
            throw new IllegalArgumentException("Detail level cannot be negative");
        this.name = Objects.requireNonNull(name);
        if(!UMap.isValidKey(name))
            throw new IllegalArgumentException("Invalid key name: " + name);
        this.detailLevel = detailLevel;
        this.optional = optional;
    }

    /**
     * Gets the name of the key.
     * @return the name of the key
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets the detail level of the key.
     * @return the detail level of the key
     */
    @Range(from = 0, to = Integer.MAX_VALUE)
    public int getDetailLevel() {
        return detailLevel;
    }

    /**
     * Returns whether the key is optional.<br>
     * @return true if the key is optional
     */
    public boolean isOptional() {
        return optional;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Key) obj;
        return Objects.equals(this.name, that.name) &&
                this.detailLevel == that.detailLevel &&
                this.optional == that.optional;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, detailLevel, optional);
    }

    @Override
    public String toString() {
        return "Key[" +
                "name=" + name + ", " +
                "detailLevel=" + detailLevel + ", " +
                "optional=" + optional + ']';
    }

}
