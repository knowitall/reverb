package edu.washington.cs.knowitall.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayIterator<T> implements Iterator<T> {

    private final T[] array;
    private int nextPos;
    private final int upperBound;

    public ArrayIterator(T[] array) {
        this.array = array;
        this.upperBound = array.length;
        this.nextPos = 0;
    }

    public ArrayIterator(T[] array, int lowerBound, int length) {
        if (lowerBound >= 0 && length > 0
                && length + lowerBound <= array.length) {
            this.array = array;
            this.upperBound = lowerBound + length;
            this.nextPos = lowerBound;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public boolean hasNext() {
        return nextPos < upperBound;
    }

    public T next() {
        if (hasNext()) {
            T ret = array[nextPos];
            nextPos++;
            return ret;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
