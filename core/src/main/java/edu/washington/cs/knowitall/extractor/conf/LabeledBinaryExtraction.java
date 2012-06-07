package edu.washington.cs.knowitall.extractor.conf;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * Extends the <code>ChunkedBinaryExtraction</code> class to have a binary
 * label.
 *
 * @author afader
 *
 */
public class LabeledBinaryExtraction extends ChunkedBinaryExtraction {

    /**
     * the positive class label
     */
    public static final int POS = 1;

    /**
     * the negative class label
     */
    public static final int NEG = 0;

    private int label;

    /**
     * Constructs a new LabeledBinaryExtraction with the given label
     *
     * @param relation
     * @param argument1
     * @param argument2
     * @param label
     *            either <code>LabeledBinaryExtraction.POS</code> or
     *            <code>LabeledBinaryExtraction.NEG</code>.
     */
    public LabeledBinaryExtraction(ChunkedExtraction relation,
            ChunkedArgumentExtraction argument1,
            ChunkedArgumentExtraction argument2, int label) {
        super(relation, argument1, argument2);
        this.label = label;
    }

    /**
     * Constructs a new LabeledBinaryExtraction with the given label.
     *
     * @param extr
     *            an existing extraction
     * @param label
     *            either <code>LabeledBinaryExtraction.POS</code> or
     *            <code>LabeledBinaryExtraction.NEG</code>.
     */
    public LabeledBinaryExtraction(ChunkedBinaryExtraction extr, int label) {
        super(extr.getRelation(), extr.getArgument1(), extr.getArgument2());
        this.label = label;
    }

    /**
     * Sets the label to positive.
     */
    public void setPositive() {
        this.label = POS;
    }

    /**
     * Sets the label to negative.
     */
    public void setNegative() {
        this.label = NEG;
    }

    /**
     * @return true if positive
     */
    public boolean isPositive() {
        return label == POS;
    }

    /**
     * @return true if negative
     */
    public boolean isNegative() {
        return label == NEG;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + label;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof LabeledBinaryExtraction))
            return false;
        LabeledBinaryExtraction other = (LabeledBinaryExtraction) obj;
        if (label != other.label)
            return false;
        return true;
    }

}
