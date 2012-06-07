package edu.washington.cs.knowitall.extractor.conf.featureset;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

/**
 * Parent class for any feature specific to the chunk layer.
 *
 * @author Rob
 *
 */
public abstract class ChunkFeature extends ExtractionFeature {

    private Set<String> chunkTags;

    public ChunkFeature(String... givenTokens) {
        this(Arrays.asList(givenTokens));
    }

    public ChunkFeature(Collection<String> givenChunkTags) {
        this.chunkTags = new HashSet<String>();
        this.chunkTags.addAll(givenChunkTags);
    }

    @Override
    protected abstract Range rangeToExamine(ChunkedBinaryExtraction cbe);

    @Override
    protected boolean testAtIndex(Integer index, ChunkedSentence sentence) {

        String tag = sentence.getChunkTag(index);
        return chunkTags.contains(tag);
    }

    /**
     * Get an expandablePosFeature for tokens right before arg1.
     *
     * @param posTags
     * @return
     */
    public static ChunkFeature withinArg2(String... chunkTags) {
        return new ChunkFeature(chunkTags) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getArgument2().getRange();
            }
        };
    }

    /**
     * Get an expandablePosFeature for tokens right before arg1.
     *
     * @param chunkTags
     * @return
     */
    public static ChunkFeature withinRel(String... chunkTags) {
        return new ChunkFeature(chunkTags) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                return cbe.getRelation().getRange();
            }
        };
    }

    /**
     * Get an expandablePosFeature for tokens right before arg1.
     *
     * @param chunkTags
     * @return
     */
    public static ChunkFeature rightBeforeArg1(String... chunkTags) {
        return new ChunkFeature(chunkTags) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {
                ChunkedArgumentExtraction arg1 = cbe.getArgument1();
                int index = arg1.getStart() - 1;
                if (index < 0 || index >= arg1.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(index, index + 1);
            }
        };
    }

    /**
     * Get an expandablePosFeature for tokens right before arg1.
     *
     * @param chunkTags
     * @return
     */
    public static ChunkFeature rightAfterArg2(String... chunkTags) {
        return new ChunkFeature(chunkTags) {
            @Override
            protected Range rangeToExamine(ChunkedBinaryExtraction cbe) {

                ChunkedArgumentExtraction arg2 = cbe.getArgument2();
                int index = arg2.getStart() + arg2.getLength();
                if (index < 0 || index >= arg2.getSentence().getLength()) {
                    return Range.EMPTY;
                } else
                    return Range.fromInterval(index, index + 1);
            }
        };
    }

}
