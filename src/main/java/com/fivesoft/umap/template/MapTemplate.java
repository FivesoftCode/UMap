package com.fivesoft.umap.template;

import com.fivesoft.umap.data.Validator;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.format.Format;
import com.fivesoft.umap.format.FormatException;
import com.fivesoft.umap.format.FormatReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class MapTemplate extends Template
        implements Iterable<Mapping> {

    @Unmodifiable
    private final @NotNull Map<@NotNull String, @NotNull Mapping> mappings;

    //Cache of the longest key length. May be used for formatting
    private final int maxKeyLength;
    private final long complexity;

    //Private constructor. Use Builder instead.
    private MapTemplate(
            @NotNull LinkedHashMap<@NotNull String, //LinkedHashMap to preserve order IMPORTANT!!!!
            @NotNull Mapping> type) {

        //Sort the map by:
        // 1. Complexity of the template
        // 2. Detail level of the key
        // 3. Name of the key
        AtomicInteger max = new AtomicInteger();
        AtomicLong complexity = new AtomicLong();
        type = type.entrySet().stream()
                .sorted((o1, o2) -> {
                    int byCplx = Long.compare(o1.getValue().getTemplate().getComplexity(),
                            o2.getValue().getTemplate().getComplexity()); //Sort by complexity
                    if (byCplx == 0) {
                        int byDL = Integer.compare(o1.getValue().getKey().getDetailLevel(),
                                o2.getValue().getKey().getDetailLevel()); //Then by detail level
                        if (byDL == 0) {
                            return o1.getKey().compareTo(o2.getKey()); //Then by name
                        }
                        return byDL;
                    }
                    return byCplx;
                })
                .collect(LinkedHashMap::new, (m, e) -> {
                    String s = e.getKey();
                    m.put(s, e.getValue());
                    if (s.length() > max.get()) //Compute max key length also at once
                        max.set(s.length());
                    complexity.addAndGet(e.getValue()
                            .getTemplate().getComplexity()); //Compute complexity at once
                }, Map::putAll);

        //Prevent modification of the map
        this.mappings = Collections.unmodifiableMap(
                Objects.requireNonNull(type, "Type cannot be null")
        );
        this.maxKeyLength = max.get();
        this.complexity = complexity.get();
    }

    /**
     * Gets the mapping of the template as a map of String to Template.<br>
     * The map is unmodifiable. Any attempt to modify the map will result in an {@link UnsupportedOperationException}.<br>
     * This map will persist the order of insertion and is guaranteed not to contain null keys or values.
     *
     * @return the mapping of the template
     */
    @Unmodifiable
    public @NotNull Map<@NotNull String, @NotNull Mapping> getMappings() {
        return mappings;
    }

    /**
     * Gets the maximum length of the keys in the map. (In characters)
     *
     * @return the maximum length of the keys in the map
     */
    public int getMaxKeyLength() {
        return maxKeyLength;
    }

    /**
     * Gets the complexity of the template.<br>
     * Complexity is the sum of all template complexities in the map.
     * @return the complexity of the template
     */
    @Override
    public long getComplexity() {
        return complexity;
    }

    /**
     * Delegate method for {@link Map#size()}.<br>
     * (equivalent to {@code getMappings().size()})
     *
     * @return the number of mappings in the template
     */
    public int size() {
        return getMappings().size();
    }

    /**
     * Delegate method for {@link Map#isEmpty()}.<br>
     * (equivalent to {@code getMappings().isEmpty()})
     *
     * @return true if the template is empty, false otherwise
     */
    public boolean isEmpty() {
        return getMappings().isEmpty();
    }

    /**
     * Gets the template at a specified key.
     *
     * @param key of the template
     * @return the template at the specified key or null if there is no such key
     */
    @Nullable
    public Mapping get(@NotNull String key) {
        return getMappings().get(key);
    }

    /**
     * Checks if the template contains a template with the specified key.
     *
     * @param key to check for
     * @return true if the template contains a template at the specified key, false otherwise
     */
    public boolean containsKey(@NotNull String key) {
        return getMappings().containsKey(key);
    }

    /**
     * Returns iterable of mappings filtered by detail level.
     * @param maxDetailLevel maximum detail level of the mappings to return
     * @return iterable of mappings filtered by detail level
     */
    public Iterable<Mapping> limitDetailLevel(int maxDetailLevel){
        return () -> new DLFilterIterator(maxDetailLevel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UMap readFormat(@NotNull InputStream in,
                           @NotNull Format format,
                           @Nullable FormatReader.Options options) throws IOException, FormatException {

        options = FormatReader.Options.getOrDefault(options);
        FormatReader reader = format.createReader(in, options);
        return FormatReader.readFormat(in, reader, this, options);
    }

    @Override
    public UMap readFormat(byte @NotNull [] data, @NotNull Format format,
                           FormatReader.@Nullable Options options) throws FormatException {
        return (UMap) super.readFormat(data, format, options);
    }

    @Override
    public UMap readFormat(@NotNull String data, @NotNull Format format,
                           FormatReader.@Nullable Options options) throws FormatException {
        return (UMap) super.readFormat(data, format, options);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesTemplate(@NotNull Template another) {
        if (another instanceof MapTemplate t) {
            if (t.size() != size()) return false;
            for (Map.Entry<String, Mapping> e : getMappings().entrySet()) {
                Mapping m = t.get(e.getKey());
                if (m == null) return false; //Key isn't found
                if (!e.getValue().getTemplate()
                        .matchesTemplate(m.getTemplate())) return false;
            }
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean matchesValue(@Nullable Object value) {
        if (value instanceof UMap umap) {
            return umap.getTemplate().matchesTemplate(this);
        }
        return false;
    }



    @NotNull
    @Override
    public Iterator<Mapping> iterator() {
        return getMappings().values().iterator();
    }

    /**
     * Builder for defining MapTemplate objects.<br>
     * Takes care of all necessary checks and ensures that the resulting MapTemplate is valid.
     */
    public static final class Builder {

        private final LinkedHashMap<@NotNull String, @NotNull Mapping> type = new LinkedHashMap<>();

        /**
         * Adds new mapping entry to the template.
         *
         * @param mapping to add to the template
         * @return this builder for chaining
         */

        public Builder add(@NotNull Mapping mapping) {
            type.put(mapping.getName(), mapping);
            return this;
        }

        /**
         * Adds new mapping entry to the template.
         *
         * @param key      of the mapping to add to the template
         * @param template of the mapping to add to the template
         * @return this builder for chaining
         */
        public Builder add(@NotNull Key key, @NotNull Template template,
                           @Nullable Object defaultValue) {
            return add(new Mapping(key, template, defaultValue));
        }

        /**
         * Adds new mapping entry to the template.
         *
         * @param name         of the mapping to add to the template
         * @param detailLevel  of the mapping to add to the template
         * @param optional     whether the mapping is optional
         * @param type         of the {@link PrimitiveTemplate} to add to the template
         * @param defaultValue of the mapping to add to the template
         * @param validator    of the mapping to add to the template
         * @return this builder for chaining
         */
        public <T> Builder add(@NotNull String name, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel, boolean optional,
                               @NotNull Class<T> type, @Nullable T defaultValue, @Nullable Validator validator) {
            return add(
                    new Key(name, detailLevel, optional),
                    new PrimitiveTemplate(type, validator),
                    defaultValue
            );
        }

        /**
         * Adds new mapping entry to the template with no validator.
         *
         * @param name         of the mapping to add to the template
         * @param detailLevel  of the mapping to add to the template
         * @param optional     whether the mapping is optional
         * @param type         of the {@link PrimitiveTemplate} to add to the template
         * @param defaultValue of the mapping to add to the template
         * @return this builder for chaining
         */
        public <T> Builder add(@NotNull String name, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel, boolean optional,
                               @NotNull Class<T> type, @Nullable T defaultValue) {
            return add(name, detailLevel, optional, type, defaultValue, null);
        }

        /**
         * Adds new required mapping entry to the template with no validator.
         *
         * @param name        of the mapping to add to the template
         * @param detailLevel of the mapping to add to the template
         * @param type        of the {@link PrimitiveTemplate} to add to the template
         * @return this builder for chaining
         */
        public <T> Builder addRequired(@NotNull String name, @NotNull Class<T> type, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel) {
            return add(name, detailLevel, false, type, null);
        }

        /**
         * Adds required mapping with given name and type.
         * Detail level will be set to default value of 0.
         * validator will be set to null. (no validation)
         *
         * @param name of the field
         * @param type of the field
         * @return this builder for chaining
         */
        public <T> Builder addRequired(@NotNull String name, @NotNull Class<T> type) {
            return addRequired(name, type, 0);
        }

        /**
         * Adds required mapping with given name and template.
         * Detail level will be set to default value of 0.
         * @param name of the field
         * @param type of the field
         * @return this builder for chaining
         */
        public Builder addRequired(@NotNull String name, @NotNull Template type) {
            return add(new Key(name, 0, false), type, null);
        }

        /**
         * Adds new optional mapping entry to the template without default value.
         * @param name of the mapping to add to the template
         * @param template of the field to add
         * @return this builder for chaining
         */
        public Builder addOptional(@NotNull String name, @NotNull Template template) {
            return add(new Key(name, 0, true), template, null);
        }

        public <T> Builder addOptional(@NotNull String name, @NotNull Class<T> type, @Nullable T defaultValue) {
            return add(name, 0, true, type, defaultValue);
        }

        public Builder addOptional(@NotNull String name, @NotNull Class<?> type) {
            return addOptional(name, type, null);
        }

        /**
         * Adds new {@link ArrayTemplate} mapping to the template.
         *
         * @param name          of the mapping to add to the template
         * @param detailLevel   of the mapping to add to the template
         * @param optional      whether the mapping is optional
         * @param entryTemplate of the {@link ArrayTemplate} to add to the template
         * @return this builder for chaining
         * @see ArrayTemplate#getEntryTemplate()
         */
        public Builder addArray(@NotNull String name, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel, boolean optional,
                                @NotNull Template entryTemplate, Object defaultValue) {
            return add(
                    new Key(name, detailLevel, optional),
                    new ArrayTemplate(entryTemplate),
                    defaultValue
            );
        }

        /**
         * Adds new {@link ArrayTemplate} mapping to the template with no default value.
         *
         * @param name          of the mapping to add to the template
         * @param detailLevel   of the mapping to add to the template
         * @param optional      whether the mapping is optional
         * @param entryTemplate of the {@link ArrayTemplate} to add to the template
         * @return this builder for chaining
         * @see ArrayTemplate#getEntryTemplate()
         */

        public Builder addArray(@NotNull String name, @Range(from = 0, to = Integer.MAX_VALUE) int detailLevel, boolean optional,
                                @NotNull Template entryTemplate) {
            return addArray(name, detailLevel, optional, entryTemplate, null);
        }

        /**
         * Creates MapTemplate from the builder.
         *
         * @return the created MapTemplate
         */
        public MapTemplate build() {
            return new MapTemplate(type);
        }

    }

    private class DLFilterIterator implements Iterator<Mapping> {

        private final int maxDetailLevel;
        private final Iterator<Mapping> iterator = MapTemplate.this.iterator();
        private Mapping next;

        public DLFilterIterator(int detailLevel) {
            this.maxDetailLevel = detailLevel;
        }

        @Override
        public boolean hasNext() {
            if(next != null)
                return true;
            if(iterator.hasNext()){
                Mapping temp = iterator.next();
                boolean ok = temp.getKey().getDetailLevel() <= maxDetailLevel;
                if(ok){
                    next = temp;
                    return true;
                }
            }
            return false;
        }

        @Override
        public Mapping next() {
            if(hasNext()){
                try {
                    return next;
                } finally {
                    next = null;
                }
            }
            throw new NoSuchElementException();
        }
    }

}
