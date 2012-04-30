package edu.washington.cs.knowitall.normalization;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * An interface defining the basic functionality of a field normalizer. A field
 * normalizer is a function that takes a {@link ChunkedExtraction} object and
 * returns a {@link NormalizedField} object. An example of this function would
 * be a stemmer.
 * 
 * @author afader
 * 
 */
public interface FieldNormalizer {

    /**
     * @param field
     * @return a normalized version of the given field
     */
    public NormalizedField normalizeField(ChunkedExtraction field);

}
