package edu.washington.cs.knowitall.nlp;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.sequence.BIOLayeredSequence;
import edu.washington.cs.knowitall.sequence.SequenceException;

/**
 * An immutable class that represents a tokenized, POS-tagged, and noun-phrase
 * chunked sentence.
 *
 * @author afader
 */
public class ChunkedSentence extends BIOLayeredSequence {

    /**
     * The layer name for the tokens.
     */
    public static final String TOKEN_LAYER = "tok";

    /**
     * The layer name for the part of speech tags.
     */
    public static final String POS_LAYER = "pos";

    /**
     * The layer name for the NP chunk tags.
     */
    public static final String NP_LAYER = "np";

    // a cache for getTokensAsString
    private String tokensAsString = null;

    protected final ImmutableList<Range> offsets;

    /**
     * Constructs a new instance using the given tokens, POS tags, and NP chunk
     * tags, each of which must have the same length. The NP chunks should be
     * expressed using the standard B-NP, I-NP, O tags.
     *
     * @param tokens
     * @param posTags
     * @param npChunkTags
     * @throws SequenceException
     *             if the layers are of different lengths, or if unable to
     *             interpret npChunkTags
     */
    public ChunkedSentence(String[] tokens, String[] posTags,
            String[] npChunkTags) throws SequenceException {
        this(null, ImmutableList.copyOf(tokens), ImmutableList.copyOf(posTags),
                ImmutableList.copyOf(npChunkTags));
    }

    public ChunkedSentence(Range[] offsets, String[] tokens, String[] posTags,
            String[] npChunkTags) throws SequenceException {
        this(ImmutableList.copyOf(offsets), ImmutableList.copyOf(tokens),
                ImmutableList.copyOf(posTags), ImmutableList
                        .copyOf(npChunkTags));
    }

    /**
     * Constructs a new instance using the given tokens, POS tags, and NP chunk
     * tags, each of which must have the same length. The NP chunks should be
     * expressed using the standard B-NP, I-NP, O tags.
     *
     * @param tokens
     * @param posTags
     * @param npChunkTags
     * @throws SequenceException
     *             if the layers are of different lengths, or if unable to
     *             interpret npChunkTags
     */
    public ChunkedSentence(ImmutableList<Range> offsets,
            ImmutableList<String> tokens, ImmutableList<String> posTags,
            ImmutableList<String> npChunkTags) throws SequenceException {
        super(tokens.size());
        this.offsets = offsets;
        addLayer(TOKEN_LAYER, tokens);
        addLayer(POS_LAYER, posTags);
        addSpanLayer(NP_LAYER, npChunkTags);
    }

    public ChunkedSentence(List<String> tokens, List<String> posTags,
            List<String> npChunkTags) throws SequenceException {
        this(null, ImmutableList.copyOf(tokens), ImmutableList.copyOf(posTags),
                ImmutableList.copyOf(npChunkTags));
    }

    public ChunkedSentence(List<Range> offsets, List<String> tokens,
            List<String> posTags, List<String> npChunkTags)
            throws SequenceException {
        this(offsets == null ? null : ImmutableList.copyOf(offsets),
                ImmutableList.copyOf(tokens), ImmutableList.copyOf(posTags),
                ImmutableList.copyOf(npChunkTags));
    }

    /**
     * Constructs a new instance using the given tokens, POS tags, and NP chunk
     * tags, each of which must have the same length. The NP chunks should be
     * expressed using the standard B-NP, I-NP, O tags.
     *
     * @param tokens
     * @param posTags
     * @param npChunkTags
     */
    public ChunkedSentence(ChunkedSentence sent) {
        super(sent.getLength());
        try {
            this.offsets = sent.offsets;
            addLayer(TOKEN_LAYER, sent.getTokens());
            addLayer(POS_LAYER, sent.getPosTags());
            addSpanLayer(NP_LAYER, sent.getChunkTags());
            for (String layerName : sent.getLayerNames()) {
                if (!hasLayer(layerName) && sent.isSpanLayer(layerName)) {
                    addSpanLayer(layerName, sent.getLayer(layerName));
                } else if (!hasLayer(layerName)) {
                    addLayer(layerName, sent.getLayer(layerName));
                }
            }
        } catch (SequenceException e) {
            // if this exception gets thrown, something is very wrong: we
            // should always be able to construct a new instance of a sentence
            // from an existing one.
            throw new IllegalStateException(e);
        }
    }

    public ImmutableList<Range> getOffsets() {
        return this.offsets;
    }

    public Range getRange() {
        return Range.fromInterval(0, this.getLength() - 1);
    }

    /**
     * Returns a new ChunkedSentence object that starts at the given range.
     *
     * @param range
     */
    public ChunkedSentence getSubSequence(Range range) {
        return getSubSequence(range.getStart(), range.getLength());
    }

    /**
     * Returns a new ChunkedSentence object that starts at the given start index
     * and has the given length.
     *
     * @param start
     * @param length
     */
    public ChunkedSentence getSubSequence(int start, int length) {
        try {
            ChunkedSentence result = new ChunkedSentence(getSubSequence(
                    TOKEN_LAYER, start, length), getSubSequence(POS_LAYER,
                    start, length), getSubSequence(NP_LAYER, start, length));
            for (String layerName : getLayerNames()) {
                if (!result.hasLayer(layerName) && isSpanLayer(layerName)) {
                    result.addSpanLayer(layerName,
                            getSubSequence(layerName, start, length));
                } else if (!result.hasLayer(layerName)) {
                    result.addLayer(layerName,
                            getSubSequence(layerName, start, length));
                }
            }
            return result;
        } catch (SequenceException e) {
            // This is an illegal state - we should be able to construct sub-
            // sequences of this object.
            String msg = String.format(
                    "Could not create subsequence of length %s starting at %s",
                    length, start);
            throw new IllegalStateException(msg, e);
        }
    }

    /***
     * Converts a character range into getTokensAsString into a bounding token
     * range.
     *
     * @param charStart
     * @param charEnd
     * @return
     */
    public Range getTokenRange(int charStart, int charEnd) {
        if (charStart < 0) {
            throw new IllegalArgumentException("charStart < 0: " + charStart);
        }
        if (charEnd < 0) {
            throw new IllegalArgumentException("charStart < 0: " + charStart);
        }

        // find the tokens represented
        int x = 0;
        int i = 0;
        int tokenStart = -1, tokenEnd = -1;
        for (String string : this.getTokens()) {
            if (charStart >= x - 1 && charStart <= x + string.length()) {
                tokenStart = i;
            }

            if (charEnd >= x && charEnd <= x + string.length()) {
                tokenEnd = i + 1;
                break;
            }

            x += string.length() + 1;
            i += 1;
        }

        if (tokenStart == -1 || tokenEnd == -1) {
            return null;
        } else {
            return Range.fromInterval(tokenStart, tokenEnd);
        }
    }

    /**
     * Returns a copy of this object.
     */
    public ChunkedSentence clone() {
        try {
            ChunkedSentence clone = new ChunkedSentence(getTokens(),
                    getPosTags(), getChunkTags());
            for (String layerName : getLayerNames()) {
                if (!clone.hasLayer(layerName) && isSpanLayer(layerName)) {
                    clone.addSpanLayer(layerName, getLayer(layerName));
                } else if (!clone.hasLayer(layerName)) {
                    clone.addLayer(layerName, getLayer(layerName));
                }
            }
            return clone;
        } catch (SequenceException e) {
            // This is an illegal state - we should be able to clone the
            // sentence without having errors occur.
            String msg = "Could not clone sentence";
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * @return an unmodifiable list over the tokens of this sentence.
     */
    public ImmutableList<String> getTokens() {
        return getLayer(TOKEN_LAYER);
    }

    /**
     * @return an unmodifiable list over the POS tags of this sentence.
     */
    public ImmutableList<String> getPosTags() {
        return getLayer(POS_LAYER);
    }

    /**
     * @param start
     * @param length
     * @return the first <code>length</code> tokens starting at index
     *         <code>start</code>.
     */
    public ImmutableList<String> getTokens(int start, int length) {
        return getSubSequence(TOKEN_LAYER, start, length);
    }

    /**
     * @param range
     * @return the tokens at the indexes given by <code>range</code>.
     */
    public ImmutableList<String> getTokens(Range range) {
        return getSubSequence(TOKEN_LAYER, range);
    }

    /**
     * @param start
     * @param length
     * @return the first <code>length</code> POS tags starting at index
     *         <code>start</code>.
     */
    public ImmutableList<String> getPosTags(int start, int length) {
        return getSubSequence(POS_LAYER, start, length);
    }

    /**
     * @param range
     * @return the POS tags at the indexes given by <code>range</code>.
     */
    public ImmutableList<String> getPosTags(Range range) {
        return getSubSequence(POS_LAYER, range);
    }

    /**
     * @return an unmodifiable list over the ranges of the NP chunks in this
     *         sentence.
     */
    public ImmutableCollection<Range> getNpChunkRanges() {
        return getSpans(NP_LAYER, "NP");
    }

    /**
     * @return an unmodifiable list over the NP chunk tags of this sentence.
     */
    public ImmutableList<String> getChunkTags() {
        return getLayer(NP_LAYER);
    }

    /**
     * @param start
     * @param length
     * @return the first <code>length</code> NP chunk tags starting at index
     *         <code>start</code>.
     */
    public ImmutableList<String> getChunkTags(int start, int length) {
        return getSubSequence(NP_LAYER, start, length);
    }

    /**
     * @param range
     * @return the first <code>length</code> NP chunk tags in the range
     *         <code>range</code>.
     */
    public ImmutableList<String> getChunkTags(Range range) {
        return getSubSequence(NP_LAYER, range);
    }

    public String getOffsetsAsString() {
        return Joiner.on(" ").join(this.offsets);
    }

    /**
     * @return the tokens of this sentence joined by spaces.
     */
    public String getTokensAsString() {
        if (this.tokensAsString == null) {
            this.tokensAsString = getLayerAsString(TOKEN_LAYER);
        }

        return this.tokensAsString;
    }

    /**
     * @param start
     * @param length
     * @return <code>length</code> tokens starting at <code>start</code>, joined
     *         by spaces.
     */
    public String getTokensAsString(int start, int length) {
        return getLayerAsString(TOKEN_LAYER, start, length);
    }

    /**
     * @param range
     * @return the tokens at the indexes of <code>range</code>, joined by
     *         spaces.
     */
    public String getTokensAsString(Range range) {
        return getTokensAsString(range.getStart(), range.getLength());
    }

    /**
     * @param start
     * @param length
     * @return <code>length</code> POS tags starting at <code>start</code>,
     *         joined by spaces.
     */
    public String getPosTagsAsString(int start, int length) {
        return getLayerAsString(POS_LAYER, start, length);
    }

    /**
     * @param range
     * @return the POS tags at the indexes of <code>range</code>, joined by
     *         spaces.
     */
    public String getPosTagsAsString(Range range) {
        return getLayerAsString(POS_LAYER, range);
    }

    /**
     * @return the POS tags of this sentence, joined by spaces.
     */
    public String getPosTagsAsString() {
        return getLayerAsString(POS_LAYER);
    }

    /**
     * @return the NP chunk tags of this sentence (in B-NP, I-NP, O format),
     *         joined by strings.
     */
    public String getChunkTagsAsString() {
        return getLayerAsString(NP_LAYER);
    }

    /**
     * @return the tokens of this sentence joined by spaces
     */
    public String toString() {
        return getTokensAsString();
    }

    /**
     * @return the tokens, POS tags, and NP chunk tags of this string in Open
     *         NLP format (square brackets around chunks, then token/tag).
     */
    public String toOpenNlpFormat() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < getLength(); i++) {

            String tt = getToken(i) + "/" + getPosTag(i);
            String npChunkTag = getChunkTag(i);
            if (i > 0 && !npChunkTag.startsWith("I-")
                    && !getChunkTag(i - 1).equals("O")) {
                sb.append("]");
            }
            if (npChunkTag.startsWith("B-")) {
                sb.append(" [" + npChunkTag.substring(2));
            }
            sb.append(" " + tt);
        }
        return sb.toString();
    }

    /**
     * @param i
     * @return the token at index i
     */
    public String getToken(int i) {
        return get(TOKEN_LAYER, i);
    }

    /**
     * @param i
     * @return the part-of-speech tag at index i
     */
    public String getPosTag(int i) {
        return get(POS_LAYER, i);
    }

    /**
     * @param i
     * @return the chunk tag at index i
     */
    public String getChunkTag(int i) {
        return get(NP_LAYER, i);
    }
}
