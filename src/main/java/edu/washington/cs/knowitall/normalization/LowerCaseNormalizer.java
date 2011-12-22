package edu.washington.cs.knowitall.normalization;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * Lowercases each token in the field.
 * @author afader
 *
 */
public class LowerCaseNormalizer implements FieldNormalizer {

    @Override
    /**
     * Lowercases each token in the field.
     */
    public NormalizedField normalizeField(ChunkedExtraction field) {
        int n = field.getLength();
        String[] tokens = new String[n];
        String[] pos = new String[n];
        for (int i = 0; i < n; i++) {
            tokens[i] = field.getToken(i).toLowerCase();
            pos[i] = field.getPosTag(i);
        }
        return new NormalizedField(field, tokens, pos);
    }

}
