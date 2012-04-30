package edu.washington.cs.knowitall.util;

import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

public abstract class AbstractNestedIterator<S, T> extends AbstractIterator<T> {

    private final Iterator<S> outer;
    private Iterator<T> inner;

    protected abstract Iterator<T> computeInnerIterator(S object);

    public AbstractNestedIterator(Iterable<S> outerIterable) {
        this.outer = outerIterable.iterator();
    }

    public AbstractNestedIterator(Iterator<S> outer) {
        this.outer = outer;
    }

    @Override
    protected T computeNext() {
        if (inner != null && inner.hasNext())
            return inner.next();
        while (outer.hasNext()) {
            inner = computeInnerIterator(outer.next());
            if (inner != null && inner.hasNext()) {
                return inner.next();
            } else if (inner == null) {
                return endOfData();
            }
        }
        return endOfData();
    }

}
