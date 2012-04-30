package edu.washington.cs.knowitall.extractor.mapper;

import java.util.regex.Pattern;

/**
 * Filters out sentences that do not start with a capital letter or number (ignoring single quotes, double
 * quotes, and whitespace).
 * @author afader
 *
 */
public class SentenceStartFilter extends FilterMapper<String> {

    private static final Pattern startPattern = Pattern.compile("^\\s*[\"']?\\s*[A-Z0-9]");

    @Override
    public boolean doFilter(String sent) {
        return startPattern.matcher(sent).find();
    }
}
