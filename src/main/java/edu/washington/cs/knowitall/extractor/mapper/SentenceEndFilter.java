package edu.washington.cs.knowitall.extractor.mapper;

import java.util.regex.Pattern;

/**
 * Filters out sentences that do not end with a period, question mark, or
 * exclamation point, ignoring double quotes, single quotes, and whitespace at
 * the end.
 *
 * @author afader
 *
 */
public class SentenceEndFilter extends FilterMapper<String> {

    private static final Pattern endPattern = Pattern
            .compile("[.?!]['\"]?\\s*$");

    @Override
    public boolean doFilter(String sent) {
        return endPattern.matcher(sent).find();
    }
}
