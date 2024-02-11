package com.fivesoft.umap.data;
import java.util.Objects;

public class Null {

    public static final Null INSTANCE = new Null();

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return o == this || o == null; // API specifies this broken equals implementation
    }

    // at least make the broken equals(null) consistent with Objects.hashCode(null).
    @Override
    public int hashCode() {
        return Objects.hashCode(null);
    }

    @Override
    public String toString() {
        return "null";
    }

}
