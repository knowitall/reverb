package edu.washington.cs.knowitall.index;

import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

/***
 * An interface defining the behavior of an extraction normalizer. An 
 * extraction normalizer takes a {@link SpanExtraction} as input and returns
 * a {@link NormalizedSpanExtraction} as output. An example of such a function
 * would be one that takes a {@link ChunkedBinaryExtraction} and finds the
 * head words for its arguments, and removes auxiliary verbs from its relation.
 * @author afader
 *
 */
public interface SpanExtractionNormalizer {
	
	/**
	 * @param extr
	 * @return the normalized version of extr
	 */
	public NormalizedSpanExtraction normalizeExtraction(SpanExtraction extr);

}
