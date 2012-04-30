package edu.washington.cs.knowitall.sequence;

import java.util.Arrays;

import com.google.common.base.Joiner;

/**
 * A class that wraps a String array so it can be used as the key in a
 * {@link java.util.HashSet} object.
 * 
 * @author afader
 * 
 */
public class StringArrayWrapper {

    private String[] data;

    /**
     * Wraps the given data
     * 
     * @param data
     */
    public StringArrayWrapper(String[] data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.data = data;
    }

    /**
     * @return the underlying array
     */
    public String[] getData() {
        return data;
    }

    /**
     * @return a string representation
     */
    public String toString() {
        return "StringArrayWrapper(" + Joiner.on("|").join(data) + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(data);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof StringArrayWrapper))
            return false;
        StringArrayWrapper other = (StringArrayWrapper) obj;
        if (!Arrays.equals(data, other.data))
            return false;
        return true;
    }

}
