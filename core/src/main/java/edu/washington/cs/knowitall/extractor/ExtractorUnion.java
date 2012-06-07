package edu.washington.cs.knowitall.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/***
 * Takes the union of the output of multiple extractors. The output of this
 * extractor is the output of the others concatenated together.
 *
 * @author afader
 *
 * @param <S>
 * @param <T>
 */
public class ExtractorUnion<S, T> extends Extractor<S, T> {

    private List<Extractor<S, T>> extractors;

    /**
     * Constructs an empty extractor that will return an empty set of results
     * from any given input.
     */
    public ExtractorUnion() {
        extractors = new ArrayList<Extractor<S, T>>();
    }

    /**
     * Constructs a new extractor that returns the union of the output from each
     * of the given extractors.
     *
     * @param extractors
     */
    public ExtractorUnion(List<Extractor<S, T>> extractors) {
        this.extractors = new ArrayList<Extractor<S, T>>(extractors.size());
        for (Extractor<S, T> extr : extractors) {
            this.extractors.add(extr);
        }
    }

    /**
     * Adds the given extractor to the union.
     *
     * @param extractor
     */
    public void addExtractor(Extractor<S, T> extractor) {
        extractors.add(extractor);
    }

    /**
     * Returns the results from each extractor. If there are no extractors in
     * the union, returns an empty Iterable.
     *
     * @throws ExtractorException
     *             if unable to extract
     */
    protected Collection<T> extractCandidates(S source)
            throws ExtractorException {

        // No extractors
        if (extractors == null || extractors.size() == 0) {
            return new ArrayList<T>();

        } else {
            ArrayList<T> results = new ArrayList<T>(extractors.size());
            for (Extractor<S, T> e : extractors) {
                for (T extr : e.extract(source)) {
                    results.add(extr);
                }
            }
            return results;
        }
    }

}
