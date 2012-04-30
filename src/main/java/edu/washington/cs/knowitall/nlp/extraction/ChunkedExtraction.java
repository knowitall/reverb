package edu.washington.cs.knowitall.nlp.extraction;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/**
 * An extraction object that represents a contiguous subsequence of a
 * {@link ChunkedSentence} object.
 *
 * @author afader
 *
 */
public class ChunkedExtraction extends ChunkedSentence {

    private final Range range;
    private final ChunkedSentence sent;
    private String string;

    /**
     * Constructs a new {@link ChunkedExtraction} object representing range in
     * the sentence sent. range must be a subset of [0, {@code sent.getLength()}
     * ).
     *
     * @param sent
     *            the source sentence.
     * @param range
     *            the subsequence of sent that this extraction will represent.
     */
    public ChunkedExtraction(ChunkedSentence sent, Range range) {
        super(sent.getSubSequence(range));
        this.range = range;
        this.sent = sent;
        this.string = null;
    }

    /**
     * Constructs a new <code>NpChunkedExtraction</code> object representing
     * <code>range</code> in the sentence <code>sent</code>. <code>range</code>
     * must be a subset of [0, <code>sent.getLength()</code>).
     *
     * @param sent
     *            the source sentence.
     * @param range
     *            the subsequence of <code>sent</code> that this extraction will
     *            represent.
     * @param string
     *            a string representation of the relation part, usually
     *            different than just the subsequence in the sentence.
     */
    public ChunkedExtraction(ChunkedSentence sent, Range range, String string) {
        super(sent.getSubSequence(range));
        this.range = range;
        this.sent = sent;
        this.string = string;
    }

    /**
     * @return the start index of the extraction.
     */
    public int getStart() {
        return getRange().getStart();
    }

    /**
     * @return the <code>Range</code> object that represents the subsequence in
     *         the source sentence.
     */
    public Range getRange() {
        return range;
    }

    /**
     * @return the source sentence of this extraction.
     */
    public ChunkedSentence getSentence() {
        return sent;
    }

    /**
     * @param extr
     * @return true if this extraction is adjacent to or overlaps with extr in
     *         this sentence.
     */
    public boolean isAdjacentOrOverlaps(ChunkedExtraction extr) {
        return getRange().isAdjacentOrOverlaps(extr.getRange());
    }

    /**
     * @param extr
     * @return true if this extraction overlaps with extr in this sentence.
     */
    public boolean overlapsWith(ChunkedExtraction extr) {
        return getRange().overlapsWith(extr.getRange());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        result = prime * result + ((sent == null) ? 0 : sent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        ChunkedExtraction other = (ChunkedExtraction) obj;
        if (range == null) {
            if (other.range != null)
                return false;
        } else if (!range.equals(other.range))
            return false;
        if (sent == null) {
            if (other.sent != null)
                return false;
        } else if (!sent.equals(other.sent))
            return false;
        return true;
    }

    public String getText() {
        if (string == null) {
            string = this.sent.getTokensAsString(this.getRange());
        }

        return string;
    }

    @Override
    public String toString() {
        return this.getText();
    }
}
