package com.fivesoft.umap.data;

import com.fivesoft.umap.exception.FieldException;
import com.fivesoft.umap.format.FormatException;
import com.fivesoft.umap.format.Format;
import com.fivesoft.umap.format.FormatWriter;
import com.fivesoft.umap.format.WriterContext;
import com.fivesoft.umap.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public abstract class UObject<T extends Template> {

    private final T template;

    protected UObject(@NotNull T template) {
        this.template = Objects.requireNonNull(template);
    }

    /**
     * Gets the template which this object is based on.
     * @return The template which this object is based on.
     */

    @NotNull
    public T getTemplate() {
        return template;
    }

    /**
     * Returns the number of elements in this object.<br>
     * For array, this will return its length, for map, this will return count of its keys.
     * @return the number of elements in this object
     */
    public abstract int size();

    /**
     * Checks if this object is empty.
     * @return {@code true} if this object is empty, {@code false} otherwise
     */
    public final boolean isEmpty(){
        return size() == 0;
    }

    /**
     * Writes representation of this object to the
     * specified output stream using the specified format.<br>
     * @param out The output stream to write to
     * @param options The options to use, may be null to use default options
     * @param format The format to use
     * @param detailLevel maximum detail level of the field to include it in the output
     * @throws IOException If an I/O error occurs while writing to the output stream
     * @throws FormatException If the object cannot be formatted
     */
    public void format(@NotNull OutputStream out,
                                @Nullable FormatWriter.Options options,
                                @NotNull Format format, int detailLevel) throws IOException, FormatException {
        FormatWriter writer = format.createWriter(out, options);
        FormatWriter.format(this, writer,
                new WriterContext(options), out, detailLevel, true);
        writer.flush();
    }

    public static abstract class Builder<T extends Template> {

        protected final T template;

        protected Builder(@NotNull T template) {
            this.template = Objects.requireNonNull(template);
        }

        /**
         * Builds a new object based on the template of this builder.
         * @return the new object
         */

        @NotNull
        public abstract UObject<T> build() throws FieldException;

        public final T getTemplate() {
            return template;
        }

    }

}
