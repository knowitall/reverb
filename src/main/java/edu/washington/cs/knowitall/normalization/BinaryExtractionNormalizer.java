package edu.washington.cs.knowitall.normalization;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * A class for normalizing {@link ChunkedBinaryExtraction} objects. This class
 * uses {@link LowerCaseNormalizer} to normalize arg1 and arg2, and 
 * {@link VerbalRelationNormalizer} to normalize rel.
 * @author afader
 *
 */
public class BinaryExtractionNormalizer {
    
    private LowerCaseNormalizer argNormalizer;
    private VerbalRelationNormalizer relNormalizer;
    
    /**
     * Constructs a new normalizer object.
     */
    public BinaryExtractionNormalizer() {
        this.argNormalizer = new LowerCaseNormalizer();
        this.relNormalizer = new VerbalRelationNormalizer();
    }
    
    /**
     * Normalizes the given argument
     * @param arg
     * @return the normalized argument
     */
    public NormalizedField normalizeArgument(ChunkedExtraction arg) {
        return argNormalizer.normalizeField(arg);
    }
    
    /**
     * Normalizes the given relation phrase
     * @param rel
     * @return the normalized phrase
     */
    public NormalizedField normalizeRelation(ChunkedExtraction rel) {
        return relNormalizer.normalizeField(rel);
    }
    
    /**
     * Normalizes the given extraction
     * @param extr
     * @return the normalized extraction
     */
    public NormalizedBinaryExtraction normalize(ChunkedBinaryExtraction extr) {
        NormalizedField arg1Norm = normalizeArgument(extr.getArgument1());
        NormalizedField arg2Norm = normalizeArgument(extr.getArgument2());
        NormalizedField relNorm = normalizeRelation(extr.getRelation());
        return new NormalizedBinaryExtraction(extr, arg1Norm, relNorm, arg2Norm);
    }

}
