package edu.washington.cs.knowitall.extractor.mapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A mapper used to filter sentences by number of words.
 * @author afader
 *
 */
public class SentenceLengthFilter extends FilterMapper<String> {

    private int minWords = 0;
    private int maxWords = Integer.MAX_VALUE;
    private static final Pattern wordPattern = Pattern.compile("\\b\\w+\\b");

    /**
     * Constructs a new <code>SentenceLengthFilter</code> object.
     * @param minWords the minimum number of words in the sentence.
     * @param maxWords the maximum number of words in the sentence.
     */
    public SentenceLengthFilter(int minWords, int maxWords) {
        this.minWords = minWords;
        this.maxWords = maxWords;
    }

    /**
     * Constructs a new <code>SentenceLengthFilter</code> object, with no maximum length.
     * @param minWords the minimum number of words.
     * @return a new <code>SentenceLengthFilter</code>.
     */
    public static SentenceLengthFilter minFilter(int minWords) {
        return new SentenceLengthFilter(minWords, Integer.MAX_VALUE);
    }

    /**
     * Constructs a new <code>SentenceLengthFilter</code> object, with no minimum length.
     * @param maxWords the maximum number of words.
     * @return a new <code>SentenceLengthFilter</code>.
     */
    public static SentenceLengthFilter maxFilter(int maxWords) {
        return new SentenceLengthFilter(0, maxWords);
    }

    private int countWords(String s) {
        Matcher m = wordPattern.matcher(s);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    @Override
    public boolean doFilter(String sent) {
        int n = countWords(sent);
        return minWords <= n && n <= maxWords;
    }
}
