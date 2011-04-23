package edu.washington.cs.knowitall.util;

import java.util.Iterator;

public class IterableAdapter<T> implements Iterable<T> {

    private Iterator<T> iter;

    public IterableAdapter(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public Iterator<T> iterator() {
        return iter;
    }

}
