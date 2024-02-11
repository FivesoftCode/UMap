package com.fivesoft.umap.template;

import com.fivesoft.umap.format.Format;
import com.fivesoft.umap.format.FormatException;
import com.fivesoft.umap.format.FormatReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public abstract class Template {

    public abstract Object readFormat(@NotNull InputStream in,
                                      @NotNull Format format,
                                      @Nullable FormatReader.Options options) throws IOException, FormatException;

    public Object readFormat(byte @NotNull [] data,
                                   @NotNull Format format,
                                   @Nullable FormatReader.Options options) throws FormatException {
        try {
            return readFormat(new ByteArrayInputStream(Objects.requireNonNull(data)),
                    format, options);
        } catch (IOException e) {
            //This should never happen
            throw new RuntimeException(e);
        }
    }

    public Object readFormat(@NotNull String data,
                                   @NotNull Format format,
                                   @Nullable FormatReader.Options options) throws FormatException {
        return readFormat(Objects.requireNonNull(data).getBytes(),
                format, options);
    }

    /**
     * Checks if this template matches another template.<br>
     * Matching templates are templates that can be used to read the same data structure.<br>
     * @param another The template to check.
     * @return true if this template matches the specified template, false otherwise.
     */
    public abstract boolean matchesTemplate(@NotNull Template another);

    /**
     * Checks if this template matches a value.<br>
     * Matching a value means that the value can be read using this template.<br>
     * @param value The value to check.
     * @return true if this template matches the specified value, false otherwise.
     */
    public abstract boolean matchesValue(@NotNull Object value);

    /**
     * Gets the complexity of this template.<br>
     * Complexity is basically an estimated total number of primitive values stored in an object following this template.<br>
     * For example, a template for a map with 3 primitive keys has complexity of 3.<br>
     * Complexity is mainly used for ordering mappings while displaying them in a human-readable format.<br>
     * Showing single-value mappings first and complex multi-value mappings last is a big advantage for human readability.
     * @return The complexity of this template.
     */
    public abstract long getComplexity();

    /**
     * Creates a new {@link ArrayTemplate} with this template as an entry template.
     * @return the new array template.
     */
    public final ArrayTemplate asArray() {
        return new ArrayTemplate(this);
    }

}
