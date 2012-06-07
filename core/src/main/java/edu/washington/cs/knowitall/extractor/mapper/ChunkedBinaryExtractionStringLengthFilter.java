package edu.washington.cs.knowitall.extractor.mapper;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * A mapper used to filter binary extractions by number of characters.
 *
 * @author schmmd
 *
 */
public class ChunkedBinaryExtractionStringLengthFilter extends
        FilterMapper<ChunkedBinaryExtraction> {

    private int minArgLength = 0;
    private int maxArgLength = Integer.MAX_VALUE;
    private int minPredicateLength = 0;
    private int maxPredicateLength = Integer.MAX_VALUE;

    /**
     * Constructs a new <code>ChunkedBinaryExtractionLengthFilter</code> object.
     *
     * @param minWords
     *            the minimum number of characters in the predicate.
     * @param maxWords
     *            the maximum number of characters in the predicate.
     */
    public ChunkedBinaryExtractionStringLengthFilter(int minArgLength,
            int maxArgLength, int minPredicateLength, int maxPredicateLength) {
        this.minArgLength = minArgLength;
        this.maxArgLength = maxArgLength;
        this.minPredicateLength = minPredicateLength;
        this.maxPredicateLength = maxPredicateLength;
    }

    @Override
    public boolean doFilter(ChunkedBinaryExtraction extraction) {
        String arg1 = extraction.getArgument1().getTokensAsString();
        String arg2 = extraction.getArgument2().getTokensAsString();
        String predicate = extraction.getRelation().getTokensAsString();
        return arg1.length() >= minArgLength && arg1.length() <= maxArgLength
                && arg2.length() >= minArgLength
                && arg2.length() <= maxArgLength
                && predicate.length() >= minPredicateLength
                && predicate.length() <= maxPredicateLength;
    }
}
