package edu.washington.cs.knowitall.extractor.mapper;

import java.util.ArrayList;

/**
 * A mapper class that returns the first maximal element in the object stream according
 * to a function <code>doValueMap(T object)</code>. 
 * @author afader
 *
 * @param <S> a comparable type returned by <code>doValueMap(T object)</code>.
 * @param <T>
 */
public abstract class MaxMapper<S extends Comparable<S>, T> extends Mapper<T> {

    public abstract S doValueMap(T object);

    @Override
    /**
     * Applies <code>doValueMap</code> to each object in <code>objects</code>. Keeps track
     * of the object with the largest value, and returns it at the end of the stream.s
     */
    protected Iterable<T> doMap(Iterable<T> objects) {
        T max = null;
        S maxVal = null;
        for (T object : objects) {
            S val = doValueMap(object);
            if (maxVal == null || val.compareTo(maxVal) == 1) {
                max = object;
                maxVal = val;
            }
        }
        ArrayList<T> result = new ArrayList<T>(1);
        if (max != null) {
            result.add(max);
        }
        return result;
    }

}
