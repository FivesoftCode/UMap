package com.fivesoft.umap.format;

import java.util.Objects;

public class ValueToken {


    private static final ValueToken MAP = new ValueToken(false, 1);
    private static final ValueToken ARRAY = new ValueToken(false, 2);
    private static final ValueToken MAP_LAST = new ValueToken(true, 1);
    private static final ValueToken ARRAY_LAST = new ValueToken(true, 2);

    public final Object value;
    public final boolean isLast;
    private final int type;


    public ValueToken(Object value, boolean isLast) {
        this.value = value;
        this.isLast = isLast;
        this.type = 0;
    }

    public ValueToken(boolean isLast, int type) {
        this.value = null;
        this.isLast = isLast;
        this.type = type;
    }

    public static ValueToken map(boolean isLast) {
        return isLast ? MAP_LAST : MAP;
    }

    public static ValueToken array(boolean isLast) {
        return isLast ? ARRAY_LAST : ARRAY;
    }

    public boolean isMap() {
        return type == 1;
    }

    public boolean isArray() {
        return type == 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueToken that = (ValueToken) o;
        return isLast == that.isLast && type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isLast, type);
    }

    @Override
    public String toString() {
        return "ValueToken{" +
                "value=" + value +
                ", isLast=" + isLast +
                ", type=" + type +
                '}';
    }
}
