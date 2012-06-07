package edu.washington.cs.knowitall.extractor.mapper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * A type of mapper that filters each object in the stream independently. Subclasses
 * extending this class only have to implement the <code>doFilter(T object)</code> method,
 * which should return <code>true</code> if <code>object</code> should remain in the stream,
 * and <code>false</code> if the object should be removed from the stream. 
 * @author afader
 *
 * @param <T>
 */
public abstract class FilterMapper<T> extends Mapper<T> {

    public abstract boolean doFilter(T object);

    private final Predicate<T> pred = new Predicate<T>() {
        public boolean apply(T object) {
            return doFilter(object);
        }
    };

    @Override
    /**
     * Applies the <code>doFilter(T object)</code> method to each object in <code>objects</code>. 
     * If the method returns <code>true</code>, then the object will remain in the string. If the method
     * returns <code>false</code> then the object is removed from the stream.
     */
    protected Iterable<T> doMap(Iterable<T> objects) {
        return Iterables.filter(objects, pred);
    }
}
