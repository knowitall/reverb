package edu.washington.cs.knowitall.extractor;

import java.util.ArrayList;
import java.util.Collection;

import com.google.common.collect.Iterables;

/**
 * A class used to represent the composition of two <code>Extractor</code> objects.
 * @author afader
 *
 * @param <R>
 * @param <S>
 * @param <T>
 */
public class ExtractorComposition<R, S, T> extends Extractor<R, T> {

    private final Extractor<R,S> rsExtractor;
    private final Extractor<S,T> stExtractor;

    /**
     * Constructs a new extractor that is the composition of the given extractors.
     * @param rsExtractor
     * @param stExtractor
     */
    public ExtractorComposition(Extractor<R,S> rsExtractor, Extractor<S,T> stExtractor) {
        this.rsExtractor = rsExtractor;
        this.stExtractor = stExtractor;
    }

    @Override
    protected Collection<T> extractCandidates(R r) throws ExtractorException {
        Iterable<S> sExtrs = rsExtractor.extract(r);
        ArrayList<T> results = new ArrayList<T>();
        for (S extr : sExtrs) {
        	Iterables.addAll(results, stExtractor.extract(extr));
        }
        return results;
    }

}
