package com.fivesoft.umap;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TypeUtils {

    public static boolean getBoolean(Object o) {
        if(o instanceof Boolean)
            return (boolean) o;
        if(o instanceof Number)
            return ((Number) o).doubleValue() != 0;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    public static byte getByte(Object o) {
        if(o instanceof Number)
            return ((Number) o).byteValue();
        try {
            return Byte.parseByte(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, byte.class);
        }
    }

    public static short getShort(Object o) {
        if(o instanceof Number)
            return ((Number) o).shortValue();
        try {
            return Short.parseShort(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, short.class);
        }
    }

    public static int getInt(Object o) {
        if(o instanceof Number)
            return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, int.class);
        }
    }

    public static long getLong(Object o) {
        if(o instanceof Number)
            return ((Number) o).longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, long.class);
        }
    }

    public static float getFloat(Object o) {
        if(o instanceof Number)
            return ((Number) o).floatValue();
        try {
            return Float.parseFloat(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, float.class);
        }
    }

    public static double getDouble(Object o) {
        if(o instanceof Number)
            return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            throw new TypeException(o, double.class);
        }
    }

    public static char getChar(Object o) {
        if(o instanceof Character)
            return (char) o;
        if(o instanceof Number)
            return (char) ((Number) o).intValue();
        String s = String.valueOf(o);
        if(s.length() != 1)
            throw new TypeException(o, char.class);
        return s.charAt(0);
    }

    public static class TypeException extends RuntimeException {

        public final Object src;
        public final Class<?> dst;

        public TypeException(Object src, @NotNull Class<?> dst) {
            super("Cannot convert " + (src == null ? "null" : src.getClass().getName()) + " to " +
                    Objects.requireNonNull(dst).getName());
            this.src = src;
            this.dst = dst;
        }

    }

}
