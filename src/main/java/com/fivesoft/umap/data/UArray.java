package com.fivesoft.umap.data;

import com.fivesoft.umap.exception.FieldException;
import com.fivesoft.umap.template.ArrayTemplate;
import com.fivesoft.umap.template.PrimitiveTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.fivesoft.umap.data.UMap.isSupportedObject;

public final class UArray extends UObject<ArrayTemplate>
        implements Iterable<Object> {

    private final List<Object> data;

    //Private constructor. Use Builder instead.
    private UArray(@NotNull ArrayTemplate template,
                   @NotNull List<Object> src) {
        super(template);
        this.data = Collections.unmodifiableList(src);
    }

    /**
     * Returns the element at the specified position in this array.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *                                   ({@code index < 0 || index >= size()})
     */
    public <T> T get(int index) {
        Object o = data.get(index);
        //noinspection unchecked
        return (T) o;
    }

    /**
     * Returns the number of elements in this array.
     *
     * @return the number of elements in this array
     */

    @Override
    public int size() {
        return data.size();
    }

    /**
     * Returns the index of the first occurrence of the specified element
     *
     * @param o The element to search for
     * @return the index of the first occurrence of the specified element
     */
    public int indexOf(Object o) {
        return data.indexOf(o);
    }

    /**
     * Returns the index of the last occurrence of the specified element
     *
     * @param o The element to search for
     * @return the index of the last occurrence of the specified element
     */

    public int lastIndexOf(Object o) {
        return data.lastIndexOf(o);
    }

    /**
     * Returns {@code true} if this array contains the specified element.
     *
     * @param o element whose presence in this array is to be tested
     * @return {@code true} if this array contains the specified element
     */
    public boolean contains(Object o) {
        return data.contains(o);
    }

    /**
     * Returns a view of the portion of this array between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * @param fromIndex low endpoint (inclusive) of the sub-array
     */

    @NotNull
    public UArray subArray(int fromIndex) {
        return new UArray(getTemplate(), data.subList(fromIndex, data.size()));
    }

    /**
     * Returns a view of the portion of this array between the specified
     * {@code fromIndex}, inclusive, and {@code toIndex}, exclusive.
     *
     * @param fromIndex low endpoint (inclusive) of the sub-array
     * @param toIndex   high endpoint (exclusive) of the sub-array
     */

    @NotNull
    public UArray subArray(int fromIndex, int toIndex) {
        return new UArray(getTemplate(), data.subList(fromIndex, toIndex));
    }

    /**
     * Returns an iterator over the elements in this array in proper sequence.
     *
     * @return an iterator over the elements in this array in proper sequence
     */

    @NotNull
    @Override
    public Iterator<Object> iterator() {
        return data.iterator();
    }

    /**
     * Builder for creating UArray objects.<br>
     * This is the only official way to create UArray objects.
     */
    public static final class Builder extends UObject.Builder<ArrayTemplate> {
        private final List<Object> data = new ArrayList<>();

        //Cache if the array has builders, to avoid unnecessary list iteration when building
        private boolean hasBuilders = false;

        /**
         * Creates UArray builder based on the given template.
         *
         * @param template The template to use.
         * @throws NullPointerException If the given template is null.
         */
        public Builder(@NotNull ArrayTemplate template) {
            super(template);
        }

        /**
         * Adds the given value to the array.<br>
         * If the given value does not match the template, an exception will be thrown.<br>
         *
         * @param value The value to add.
         * @return This builder for chaining.
         * @throws FieldException       If the given value does not match the template.
         * @throws NullPointerException If the given value is null.
         */

        public Builder add(@NotNull Object value) {
            //TODO Support logging keys in exceptions
            //UMap does not support null values in arrays
            Objects.requireNonNull(value, "Array value cannot be null.");
            if (!isSupportedObject(value) && !(value instanceof UObject.Builder<?>))
                throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, null, value, "unsupported array entry type");

            if (template.getEntryTemplate() instanceof PrimitiveTemplate) {
                //Primitive template must match the value
                value = ((PrimitiveTemplate) template.getEntryTemplate()).parseValue(value);
            } else if (value instanceof UObject.Builder<?> b) {
                //Builder template must match the entry template
                if (!template.getEntryTemplate().matchesTemplate(b.getTemplate())) {
                    throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, null, value);
                }
                hasBuilders = true;
            } else if (!template.getEntryTemplate().matchesValue(value)) {
                //Value must match the entry template
                throw new FieldException(FieldException.Reason.VALUE_TYPE_MISMATCH, null, value);
            }
            data.add(value);
            return this;
        }

        /**
         * Builds the UArray object.
         *
         * @return The built UArray object.
         */

        public @NotNull UArray build() throws FieldException {
            //Convert all builders to objects
            if (hasBuilders) {
                for (int i = 0; i < data.size(); i++) {
                    Object o = data.get(i);
                    if (o instanceof UObject.Builder<?> b) {
                        data.set(i, b.build());
                    }
                }
            }
            return new UArray(template, data);
        }

    }

}
