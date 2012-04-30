package edu.washington.cs.knowitall.normalization;

import java.util.ArrayList;
import java.util.HashSet;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * Normalizes {@link ChunkedExtraction} arguments by
 * <ul>
 * <li>Lowercasing</li>
 * <li>Removing punctuation</li>
 * <li>Replacing numbers with a # symbol</li>
 * </ul>
 * 
 * @author afader
 * 
 */
public class ArgumentNormalizer implements FieldNormalizer {

    private HashSet<String> ignorePosTags;

    public ArgumentNormalizer() {
        ignorePosTags = new HashSet<String>();
        ignorePosTags.add("``");
        ignorePosTags.add("''");
        ignorePosTags.add("-LRB-");
        ignorePosTags.add("-RRB");
    }

    @Override
    /**
     * Lowercases each token in the field.
     */
    public NormalizedField normalizeField(ChunkedExtraction field) {
        ArrayList<String> tokens = new ArrayList<String>(field.getTokens());
        ArrayList<String> tags = new ArrayList<String>(field.getPosTags());
        int i = 0;
        while (i < tokens.size()) {
            String token = tokens.get(i);
            String tag = tags.get(i);
            if (ignorePosTags.contains(tag)) {
                tokens.remove(i);
                tags.remove(i);
            } else {
                if (tag.equals("CD")) {
                    token = "#";
                } else {
                    token = token.toLowerCase();
                }
                tokens.set(i, token);
                i++;
            }
        }
        return new NormalizedField(field, tokens, tags);
    }

}
