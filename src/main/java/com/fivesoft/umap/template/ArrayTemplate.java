package com.fivesoft.umap.template;

import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.format.Format;
import com.fivesoft.umap.format.FormatException;
import com.fivesoft.umap.format.FormatReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ArrayTemplate extends Template {

    @NotNull
    private final Template entryTemplate;

    /**
     * Creates a new array template with a given template of the array entries.
     * @param template template for the array entries
     * @throws NullPointerException if the template is null
     */
    public ArrayTemplate(@NotNull Template template) {
        this.entryTemplate = Objects.requireNonNull(template);
    }

    /**
     * Gets a template for the array entries.
     * @return template for the array entries
     */
    public @NotNull Template getEntryTemplate() {
        return entryTemplate;
    }

    @Override
    public UArray readFormat(@NotNull InputStream in,
                             @NotNull Format format,
                             @Nullable FormatReader.Options options) throws IOException, FormatException {

        return null;
    }

    @Override
    public boolean matchesTemplate(@NotNull Template another) {
        return another instanceof ArrayTemplate
                && entryTemplate.matchesTemplate(((ArrayTemplate) another)
                .getEntryTemplate());
    }

    @Override
    public boolean matchesValue(@Nullable Object value) {
        if (value instanceof UArray uArray) {
            return uArray.getTemplate().matchesTemplate(this);
        }
        return false;
    }

    @Override
    public long getComplexity() {
        //Array complexity is 10 times the complexity of its entry template
        return getEntryTemplate().getComplexity() * 10;
    }

}
