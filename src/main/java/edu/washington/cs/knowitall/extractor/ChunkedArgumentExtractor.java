package edu.washington.cs.knowitall.extractor;

import java.util.ArrayList;
import java.util.Collection;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * An <code>Extractor</code> class for extracting noun phrase arguments, given a relation extraction.
 * It can be used to extract arguments to the left or right of the given relation extraction. This extractor
 * returns all candidate arguments for a relation. <code>Mapper</code> classes can be used to filter down the
 * candidate arguments to a small set or a single argument (e.g. see the <code>ClosestArgumentMapper</code> class).
 * @author afader
 *
 */
public class ChunkedArgumentExtractor extends Extractor<ChunkedExtraction, ChunkedArgumentExtraction> {

    /**
     * Controls the mode of an <code>NpChunkArgumentExtractor</code>: the <code>LEFT</code> mode makes the
     * extractor return noun phrase arguments to the left of the relation in the sentence, and the <code>RIGHT</code>
     * mode makes the extractor return noun phrase arguments to the right of the relation in the sentence.
     * @author afader
     */
    public enum Mode {LEFT, RIGHT};
    private Mode mode;

    /**
     * Constructs a new <code>NpChunkArgumentExtractor</code> with the given mode (either <code>LEFT</code> or
     * <code>RIGHT</code>).
     * @param mode
     */
    public ChunkedArgumentExtractor(Mode mode) {
        this.mode = mode;
    }

    /**
     * @return the mode of this extractor.
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @param rel a relation.
     * @param range a range.
     * @return <code>true</code> if <code>range</code> is on the correct side of the relation <code>rel</code>.
     */
    private boolean acceptRange(ChunkedExtraction rel, Range range) {
        if (mode == Mode.LEFT) {
            return rel.getStart() > range.getStart();
        } else if (mode == Mode.RIGHT) {
            return rel.getStart() + rel.getLength() <= range.getStart();
        } else {
            return false;
        }
    }

    /**
     * If any of the ranges in <code>ranges</code> overlap with the relation <code>rel</code>, then modifies
     * them so they do not overlap with <code>rel</code>. For example, if <code>rel</code> is in positions 
     * (4,5,6,7) and there is a range (6,7,8) in <code>ranges</code>, then it is modified to be just (8). 
     * @param rel a relation.
     * @param ranges a collection of <code>Range</code> objects.
     * @return a new collection of <code>Range</code> objects.
     */
    private Collection<Range> removeRangeOverlapWithRelation(ChunkedExtraction rel, Iterable<Range> ranges) {
        Collection<Range> results = new ArrayList<Range>();
        Range relRange = rel.getRange();
        for (Range range : ranges) {
        	Range result = range.removeOverlap(relRange);
        	if (result != null) {
	            results.add(result);
        	}
        }
        return results;
    }

    @Override
    /**
     * Extracts candidate arguments for the given relation <code>rel</code>. If the mode of this
     * <code>NpChunkArgumentExtractor</code> is <code>LEFT</code>, then returns all noun phrases to
     * the left of <code>rel</code>. If the mode is <code>RIGHT</code>, then returns all noun phrases
     * to the right of <code>rel</code>.
     */
    protected Collection<ChunkedArgumentExtraction> extractCandidates(ChunkedExtraction rel) {
        ChunkedSentence sent = rel.getSentence();
        Collection<Range> npChunkRanges = removeRangeOverlapWithRelation(rel, sent.getNpChunkRanges());
        Collection<ChunkedArgumentExtraction> args = new ArrayList<ChunkedArgumentExtraction>();
        for (Range npChunkRange : npChunkRanges) {
            if (acceptRange(rel, npChunkRange)) {
                ChunkedArgumentExtraction arg = new ChunkedArgumentExtraction(sent, npChunkRange, rel);
                args.add(arg);
            }
        }
        return args;
    }



}
