package edu.washington.cs.knowitall.extractor.mapper;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * A mapper used to filter binary extractions by number of words.
 *
 * @author schmmd
 *
 */
public class ChunkedBinaryExtractionWordCountFilter extends
        FilterMapper<ChunkedBinaryExtraction> {

    private int minArgWords = 0;
    private int maxArgWords = Integer.MAX_VALUE;
    private int minPredicateWords = 0;
    private int maxPredicateWords = Integer.MAX_VALUE;

    /**
     * Constructs a new <code>ChunkedBinaryExtractionLengthFilter</code> object.
     *
     * @param minWords
     *            the minimum number of words in the predicate.
     * @param maxWords
     *            the maximum number of words in the predicate.
     */
    public ChunkedBinaryExtractionWordCountFilter(int minArgWords,
            int maxArgWords, int minPredicateWords, int maxPredicateWords) {
        this.minArgWords = minArgWords;
        this.maxArgWords = maxArgWords;
        this.minPredicateWords = minPredicateWords;
        this.maxPredicateWords = maxPredicateWords;
    }

    @Override
    public boolean doFilter(ChunkedBinaryExtraction extraction) {
        return extraction.getArgument1().getLength() >= minArgWords
                && extraction.getArgument1().getLength() <= maxArgWords
                && extraction.getArgument2().getLength() >= minArgWords
                && extraction.getArgument2().getLength() <= maxArgWords
                && extraction.getRelation().getLength() >= minPredicateWords
                && extraction.getRelation().getLength() <= maxPredicateWords;
    }
}
