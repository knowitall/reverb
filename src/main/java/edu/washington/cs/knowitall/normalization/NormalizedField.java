package edu.washington.cs.knowitall.normalization;

import java.util.List;

import com.google.common.base.Joiner;

import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.sequence.SimpleLayeredSequence;

/**
 * This class represents a field of a {@link SpanExtraction} that has been
 * normalized in some way, e.g. morphological normalization. A normalized field
 * is a {@link SimpleLayeredSequence} with two layers: a token layer, and a POS
 * layer. The token layer contains the normalized words. The POS layer contains
 * the POS tags for those words.
 * 
 * @author afader
 * 
 */
public class NormalizedField extends SimpleLayeredSequence {

    public static final String TOKEN_LAYER = ChunkedSentence.TOKEN_LAYER;
    public static final String POS_LAYER = ChunkedSentence.POS_LAYER;

    private ChunkedExtraction original;

    /**
     * @param original
     *            the original extractions
     * @param tokens
     *            the normalized tokens
     * @param posTags
     *            the POS tags
     * @throws SequenceException
     *             if tokens and posTags are not the same length
     */
    public NormalizedField(ChunkedExtraction original, String[] tokens,
            String[] posTags) throws SequenceException {
        super(tokens.length);
        addLayer(TOKEN_LAYER, tokens);
        addLayer(POS_LAYER, posTags);
        this.original = original;
    }

    /**
     * @param original
     *            the original extractions
     * @param tokens
     *            the normalized tokens
     * @param posTags
     *            the POS tags
     * @throws SequenceException
     *             if tokens and posTags are not the same length
     */
    public NormalizedField(ChunkedExtraction original, List<String> tokens,
            List<String> posTags) throws SequenceException {
        super(tokens.size());
        addLayer(TOKEN_LAYER, tokens);
        addLayer(POS_LAYER, posTags);
        this.original = original;
    }

    /**
     * @return the original field that this came from
     */
    public ChunkedExtraction getOriginalField() {
        return original;
    }

    /**
     * @return the normalized tokens
     */
    public List<String> getTokens() {
        return getLayer(TOKEN_LAYER);
    }

    /**
     * @return the POS tags
     */
    public List<String> getPosTags() {
        return getLayer(POS_LAYER);
    }

    /**
     * @return the normalized tokens joined by spaces
     */
    public String getTokensAsString() {
        return Joiner.on(" ").join(getTokens());
    }

    /**
     * @return the part of speech tags joined by spaces
     */
    public String getPosTagsAsString() {
        return Joiner.on(" ").join(getPosTags());
    }

    /**
     * Returns the tokens joined by spaces.
     */
    public String toString() {
        return getTokensAsString();
    }

}
