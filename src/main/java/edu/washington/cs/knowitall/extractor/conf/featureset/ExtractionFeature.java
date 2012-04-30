package edu.washington.cs.knowitall.extractor.conf.featureset;

import com.google.common.base.Predicate;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.normalization.BasicFieldNormalizer;

/**
 * A parent class for any feature that picks a particular range and applies a
 * test to all indices within that range.
 *
 * For example, the feature `return true if arg2 contains token "fish"` would be
 * implemented by having rangeToExamine() return arg2.getRange() and
 * testAtIndex() returning sentence.getToken(index).equalsIgnoreCase("fish");
 *
 * @author Rob
 */
public abstract class ExtractionFeature implements
        Predicate<ChunkedBinaryExtraction> {

    protected BasicFieldNormalizer stemmer;

    protected ExtractionFeature() {

        stemmer = new BasicFieldNormalizer();
    }

    protected abstract Range rangeToExamine(ChunkedBinaryExtraction cbe);

    @Override
    public boolean apply(ChunkedBinaryExtraction cbe) {

        ChunkedSentence sentence = cbe.getSentence();
        for (Integer index : rangeToExamine(cbe)) {

            // bounds check
            if (index < 0 || index > sentence.getLength()) {
                continue;
            }

            if (testAtIndex(index, sentence)) {

                return true;
            }

        }
        return false;
    }

    protected abstract boolean testAtIndex(Integer index,
            ChunkedSentence sentence);

    /**
     * Implements the following naive algorithm for locating the head verb
     * within a verb phrase:<br>
     * <br>
     *
     * 1. Start at end of phrase.<br>
     * 2. Work backward until you encounter a posTag.startsWith("V");<br>
     * 3. Return the corresponding token.<br>
     *
     *
     * If exception, throw an IllegalArgumentException if no verb in the
     * relation. If !exception, return null to the client if no verb in the
     * relation.
     *
     * @param relation
     * @returns null if no verb.
     */
    public static Integer indexOfHeadVerb(ChunkedExtraction relation,
            boolean exception) {

        for (int i = relation.getLength() - 1; i >= 0; i--) {

            if (relation.getPosTag(i).startsWith("V")
                    || relation.getPosTag(i).equals("MD")) {

                return i + relation.getStart();
            }

        }
        if (exception)
            throw new IllegalArgumentException(relation.toOpenNlpFormat());
        else
            return null;
    }

}