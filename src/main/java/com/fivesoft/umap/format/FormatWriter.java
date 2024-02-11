package com.fivesoft.umap.format;

import com.fivesoft.umap.data.UArray;
import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.data.UObject;
import com.fivesoft.umap.template.ArrayTemplate;
import com.fivesoft.umap.template.MapTemplate;
import com.fivesoft.umap.template.Mapping;
import com.fivesoft.umap.template.Template;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Objects;

public abstract class FormatWriter implements AutoCloseable {


    public abstract void writeRootStart(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException;

    public abstract void writeRootEnd(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException;

    public abstract void writeMappingPrefix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key,
                                            boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException;
    public abstract void writeMappingSuffix(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String key,
                                            boolean optional, boolean valueAssigned, int index, boolean last) throws IOException, FormatException;

    public abstract void writeEntryPrefix(@NotNull OutputStream out, @NotNull WriterContext context,
                                          int index, boolean last) throws IOException, FormatException;
    public abstract void writeEntrySuffix(@NotNull OutputStream out, @NotNull WriterContext context,
                                          int index, boolean last) throws IOException, FormatException;
    public abstract void writeValueString(@NotNull OutputStream out, @NotNull WriterContext context, @NotNull String value) throws IOException, FormatException;
    public abstract void writeValueBoolean(@NotNull OutputStream out, @NotNull WriterContext context, boolean value) throws IOException, FormatException;
    public abstract void writeValueByte(@NotNull OutputStream out, @NotNull WriterContext context, byte value) throws IOException, FormatException;
    public abstract void writeValueShort(@NotNull OutputStream out, @NotNull WriterContext context, short value) throws IOException, FormatException;
    public abstract void writeValueInt(@NotNull OutputStream out, @NotNull WriterContext context, int value) throws IOException, FormatException;
    public abstract void writeValueLong(@NotNull OutputStream out, @NotNull WriterContext context, long value) throws IOException, FormatException;
    public abstract void writeValueFloat(@NotNull OutputStream out, @NotNull WriterContext context, float value) throws IOException, FormatException;
    public abstract void writeValueDouble(@NotNull OutputStream out, @NotNull WriterContext context, double value) throws IOException, FormatException;
    public abstract void writeValueChar(@NotNull OutputStream out, @NotNull WriterContext context, char value) throws IOException, FormatException;
    public abstract void writeValueNull(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException;

    public final void writePrimitiveValue(@NotNull OutputStream out,
                                          @NotNull WriterContext context,
                                          @NotNull Object value) throws IOException, FormatException {
        if(value instanceof String s){
            writeValueString(out, context, s);
        } else if(value instanceof Boolean b){
            writeValueBoolean(out, context, b);
        } else if(value instanceof Byte b){
            writeValueByte(out, context, b);
        } else if(value instanceof Short s){
            writeValueShort(out, context, s);
        } else if(value instanceof Integer i){
            writeValueInt(out, context, i);
        } else if(value instanceof Long l){
            writeValueLong(out, context, l);
        } else if(value instanceof Float f){
            writeValueFloat(out, context, f);
        } else if(value instanceof Double d){
            writeValueDouble(out, context, d);
        } else if(value instanceof Character c){
            writeValueChar(out, context, c);
        } else {
            throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
        }
    }

    public abstract void writeMapStart(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException;
    public abstract void writeMapEnd(@NotNull OutputStream out, @NotNull WriterContext context) throws IOException, FormatException;

    public abstract void writeArrayStart(@NotNull OutputStream out,
                                         @NotNull WriterContext context, int length) throws IOException, FormatException;
    public abstract void writeArrayEnd(@NotNull OutputStream out,
                                       @NotNull WriterContext context, int length) throws IOException, FormatException;



    public abstract void flush() throws IOException;


    public static void format(@NotNull UObject<?> object,
                              @NotNull FormatWriter writer,
                              @NotNull WriterContext context,
                              @NotNull OutputStream out,
                              int detailLevel,
                              boolean isRoot) throws IOException, FormatException {

        Template template = object.getTemplate();
        if(isRoot){
            writer.writeRootStart(out, context);
        }
        if(template instanceof MapTemplate mt){
            UMap map = (UMap) object;
            int mkl = mt.getMaxKeyLength();
            context.setMaxKeyLength(mkl);

            writer.writeMapStart(out, context);
            context.incrementDepth();
            Iterator<Mapping> it = mt.limitDetailLevel(detailLevel).iterator();

            int i = 0; //Index of the current mapping
            while(it.hasNext()){
                Mapping m = it.next();
                boolean optional = m.isOptional();
                boolean last = !it.hasNext();
                String keyName = m.getName();
                @Nullable Object value = optional ? map.getOptional(keyName) : map.getRequired(keyName);
                boolean valueAssigned = value != null;
                //Write mapping prefix (typically a key name)
                writer.writeMappingPrefix(out, context, keyName, m.isOptional(), valueAssigned, i, last);
                //Write actual value
                if(value instanceof UObject<?> uo){
                    //Complex value
                    format(uo, writer, context, out, detailLevel, false);
                    //Restore max key length, may possibly be changed by nested mappings
                    context.setMaxKeyLength(mkl);
                } else if(value != null){
                    //Primitive value
                    writer.writePrimitiveValue(out, context, value);
                } else {
                    //Null value
                    writer.writeValueNull(out, context);
                }
                //Write mapping suffix (typically a value separator)
                writer.writeMappingSuffix(out, context, keyName, m.isOptional(), valueAssigned, i, last);
                i++;
            }
            context.decrementDepth();
            writer.writeMapEnd(out, context);
        } else if(template instanceof ArrayTemplate){
            UArray array = (UArray) object;
            int size = array.size();
            writer.writeArrayStart(out, context, size);
            context.incrementDepth();

            for(int i = 0; i < size; i++){
                boolean last = i == size - 1;
                writer.writeEntryPrefix(out, context, i, last);
                @Nullable Object value = array.get(i);
                if(value instanceof UObject<?> uo){
                    //Complex value
                    format(uo, writer, context, out, detailLevel, false);
                } else if(value != null){
                    //Primitive value
                    writer.writePrimitiveValue(out, context, value);
                } else {
                    //UMap does not support null values in arrays
                    throw new RuntimeException("Internal error: null value in array. Please report this bug.");
                }
                writer.writeEntrySuffix(out, context, i, last);
            }

            context.decrementDepth();
            writer.writeArrayEnd(out, context, size);
        }
        if(isRoot){
            writer.writeRootEnd(out, context);
        }
    }

    public static class Options {

        @NotNull
        public final String encoding;
        public final boolean pretty;
        public final int indentFactor;

        public Options(@NotNull String encoding, boolean pretty, int depthSpaces) {
            this.encoding = Objects.requireNonNull(encoding);
            this.pretty = pretty;
            this.indentFactor = depthSpaces;
        }

        public Options(@NotNull String encoding, boolean pretty) {
            this(encoding, pretty, 4);
        }

        public Options(boolean pretty) {
            this("UTF-8", pretty, 4);
        }

        public Options(boolean pretty, int depthSpaces) {
            this("UTF-8", pretty, depthSpaces);
        }

        public Options() {
            this(false);
        }

        @NotNull
        public static FormatWriter.Options getOrDefault(@Nullable FormatWriter.Options options) {
            return options == null ? new FormatWriter.Options() : options;
        }


    }

}
