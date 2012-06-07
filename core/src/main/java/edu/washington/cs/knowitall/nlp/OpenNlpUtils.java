package edu.washington.cs.knowitall.nlp;

import java.util.ArrayList;
import java.util.List;

import edu.washington.cs.knowitall.commonlib.Range;

/**
 * A class of static methods for handling OpenNLP formats.
 *
 * @author afader
 *
 */
public class OpenNlpUtils {

    public static final String START_CHUNK = "B-";
    public static final String IN_CHUNK = "I-";
    public static final String START_NP = START_CHUNK + "NP";
    public static final String IN_NP = IN_CHUNK + "NP";
    public static final String POS = "POS";
    public static final String OUT = "O";

    public static boolean isChunkBeginTag(String tag, String chunk) {
        return tag.equals(START_CHUNK + chunk);
    }

    public static boolean isChunkInTag(String tag, String chunk) {
        return tag.equals(IN_CHUNK + chunk);
    }

    public static boolean isNpBeginTag(String s) {
        return s.equals(START_NP);
    }

    public static boolean isNpInTag(String s) {
        return s.equals(IN_NP);
    }

    public static boolean isPossessive(String s) {
        return s.equals(POS);
    }

    public static boolean isInNpChunk(String s) {
        return s.equals(START_NP) || s.equals(IN_NP);
    }

    /**
     * @param tags
     * @param name
     * @return list of <code>Range</code> objects representing the ranges of the
     *         chunks of type <code>name</code> in the sentence.
     */
    public static List<Range> computeChunkRanges(String[] tags, String name) {
        int start = 0;
        int chunkLength = 0;
        boolean inChunk = false;
        List<Range> chunkRanges = new ArrayList<Range>();

        int length = tags.length;

        for (int i = 0; i < length; i++) {
            if (inChunk) {
                if (isChunkInTag(tags[i], name)) {
                    chunkLength++;
                } else {
                    Range r = new Range(start, chunkLength);
                    chunkRanges.add(r);
                    inChunk = false;
                    chunkLength = 0;
                }
            }
            if (isChunkBeginTag(tags[i], name)) {
                inChunk = true;
                start = i;
                chunkLength = 1;
            }
        }

        if (inChunk) {
            Range r = new Range(start, chunkLength);
            chunkRanges.add(r);
        }

        return chunkRanges;
    }

    /**
     * @param npChunkTags
     * @return the <code>Range</code>s of the NP chunks in the given
     *         <code>npChunkTags</code>.
     */
    public static List<Range> computeNpChunkRanges(String[] npChunkTags) {
        return computeChunkRanges(npChunkTags, "NP");
    }

    /**
     * A wrapper to support passing Lists.
     *
     * @param npChunkTags
     * @return
     */
    public static List<Range> computeNpChunkRanges(List<String> npChunkTags) {
        return computeNpChunkRanges(npChunkTags.toArray(new String[] {}));
    }

    /**
     * Modifies <code>npChunkTags</code> so that NP chunks starting with "of"
     * are merged with the previous NP chunk.
     *
     * @param tokens
     * @param npChunkTags
     */
    public static void attachOfs(String[] tokens, String[] npChunkTags) {
        for (int i = 1; i < npChunkTags.length - 1; i++) {
            if (tokens[i].equals("of") && isInNpChunk(npChunkTags[i - 1])
                    && isInNpChunk(npChunkTags[i + 1])) {
                npChunkTags[i] = IN_NP;
                npChunkTags[i + 1] = IN_NP;
            }
        }
    }

    public static void detatchOfs(String[] tokens, String[] npChunkTags) {
        for (int i = 1; i < npChunkTags.length - 1; i++) {
            if (tokens[i].equals("of") && isInNpChunk(npChunkTags[i + 1])) {
                npChunkTags[i] = "O";
                npChunkTags[i + 1] = START_NP;
            }
        }
    }

    /**
     * Modifies the <code>npChunkTags</code> so that NP chunks starting with a
     * possessive 's are merged with the previous NP chunk.
     *
     * @param posTags
     * @param npChunkTags
     */
    public static void attachPossessives(String[] posTags, String[] npChunkTags) {
        for (int i = 1; i < npChunkTags.length - 1; i++) {
            if (isPossessive(posTags[i]) && isInNpChunk(npChunkTags[i - 1])) {
                npChunkTags[i] = IN_NP;
                if (isInNpChunk(npChunkTags[i + 1])) {
                    npChunkTags[i + 1] = IN_NP;
                }
            }
        }
    }

    public static void detatchPossessives(String[] posTags, String[] npChunkTags) {
        for (int i = 1; i < npChunkTags.length - 1; i++) {
            if (isPossessive(posTags[i]) && isInNpChunk(npChunkTags[i + 1])) {
                npChunkTags[i] = OUT;
                if (isInNpChunk(npChunkTags[i + 1])) {
                    npChunkTags[i + 1] = START_NP;
                }
            }
        }
    }

}
