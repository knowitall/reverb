package edu.washington.cs.knowitall.sequence;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * A class for tagging a sequence using a {@link LayeredTokenPattern} pattern.
 * The tagger is defined by a pattern and a tag. Given a {@link LayeredSequence}
 * object, the {@link #tag(LayeredSequence)} method will return a list of
 * strings, where each string is either the tag, or the {@link #OUT_TAG} symbol.
 * </p>
 * <p>
 * For example, given the sequence "she sells sea shells by the shore", the tag
 * symbol "X" and a regular expression that matches the words starting with s,
 * the tagger will return the list [X, X, X, X, O, O, X].
 * </p>
 * 
 * @author afader
 * 
 */
public class RegexTagger {

    /**
     * The symbol used to represent a token that did not match the pattern.
     */
    public static final String OUT_TAG = "O";

    private LayeredTokenPattern pattern;

    private String tag;

    /**
     * @param pattern
     *            the regular expression to match
     * @param tag
     *            the tag to use for matching tokens
     */
    public RegexTagger(LayeredTokenPattern pattern, String tag) {
        this.pattern = pattern;
        this.tag = tag;
    }

    /**
     * @param seq
     * @return the tagged result
     * @throws SequenceException
     *             if unable to match against seq
     */
    public List<String> tag(LayeredSequence seq) throws SequenceException {

        int n = seq.getLength();
        List<String> results = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            results.add(OUT_TAG);
        }

        LayeredTokenMatcher m = pattern.matcher(seq);
        while (m.find()) {
            for (int i = m.start(); i < m.end(); i++) {
                results.set(i, tag);
            }
        }

        return results;

    }

}
