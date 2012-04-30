package edu.washington.cs.knowitall.normalization;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/***
 * Represents a {@link ChunkedBinaryExtraction} that has normalized versions of
 * arg1, rel, arg2.
 * 
 * @author afader
 * 
 */
public class NormalizedBinaryExtraction extends ChunkedBinaryExtraction {

    private NormalizedField arg1Norm;
    private NormalizedField relNorm;
    private NormalizedField arg2Norm;

    /**
     * Constructs a new normalized extraction from the given source extraction
     * and its normalized fields.
     * 
     * @param extr
     * @param arg1Norm
     * @param relNorm
     * @param arg2Norm
     */
    public NormalizedBinaryExtraction(ChunkedBinaryExtraction extr,
            NormalizedField arg1Norm, NormalizedField relNorm,
            NormalizedField arg2Norm) {
        super(extr.getRelation(), extr.getArgument1(), extr.getArgument2());
        this.arg1Norm = arg1Norm;
        this.relNorm = relNorm;
        this.arg2Norm = arg2Norm;
    }

    /**
     * @return normalized argument1
     */
    public NormalizedField getArgument1Norm() {
        return arg1Norm;
    }

    /**
     * @return normalized relation
     */
    public NormalizedField getRelationNorm() {
        return relNorm;
    }

    /**
     * @return normalized argument2
     */
    public NormalizedField getArgument2Norm() {
        return arg2Norm;
    }

}
