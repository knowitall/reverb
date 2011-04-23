package edu.washington.cs.knowitall.extractor.mapper;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

/**
 * A type of mapper that operates on each object in a stream independently. Subclasses 
 * extending this class only have to implement the <code>doMap(T object)</code> method,
 * which is then applied to each object in the argument of <code>map(Iterable<T> objects)</code>.
 * @author afader
 *
 * @param <T>
 */
public abstract class IndependentMapper<T> extends Mapper<T> {

    private final Function<T,T> mapFunction = new Function<T,T>() {
        public T apply(T object) {
            return doMap(object);
        }
    };

    public abstract T doMap(T object);

    @Override
    /**
     * Applies the <code>doMap(T object)</code> to each object in <code>objects</code>,
     * and returns the results.
     */
    protected Iterable<T> doMap(Iterable<T> objects) {
        return Iterables.transform(objects, mapFunction);
    }

}
