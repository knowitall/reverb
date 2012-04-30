package edu.washington.cs.knowitall.nlp.extraction;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/**
 * A class representing a noun phrase argument of a relation.
 *
 * @author afader
 *
 */
public class ChunkedArgumentExtraction extends ChunkedExtraction {

    private ChunkedExtraction relation;
    private double confidence = .5;

    /**
     * Constructs a new <code>NpChunkArgumentExtraction</code> from
     * <code>sent</code> for <code>relation</code>.
     *
     * @param sent
     *            the source sentence.
     * @param range
     *            the range of the argument in <code>sent</code>
     * @param relation
     */
    public ChunkedArgumentExtraction(ChunkedSentence sent, Range range,
            ChunkedExtraction relation) {
        super(sent, range);
        this.relation = relation;
    }

    public ChunkedArgumentExtraction(ChunkedSentence sent, Range range,
            ChunkedExtraction relation, double confidence) {
        super(sent, range);
        this.relation = relation;
        this.confidence = confidence;
    }

    public ChunkedArgumentExtraction(ChunkedSentence sent, Range range,
            ChunkedExtraction relation, String string) {
        super(sent, range, string);
        this.relation = relation;
    }

    /**
     * @return the relation this is an argument to.
     */
    public ChunkedExtraction getRelation() {
        return relation;
    }

    public double getConfidence() {
        return confidence;
    }
}
