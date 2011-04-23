package edu.washington.cs.knowitall.extractor.mapper;

import java.util.HashSet;
import java.util.Set;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A mapper that filters out any extractions containing a token or POS tag from a given set.
 * @author afader
 *
 */
public class StopListFilter extends FilterMapper<ChunkedExtraction> {

    private Set<String> stopTokens;
    private Set<String> stopPosTags;

    /**
     * Constructs a new <code>StopListFilter</code> with empty sets for POS tags and tokens.
     */
    public StopListFilter() {
        this.stopTokens = new HashSet<String>();
        this.stopPosTags = new HashSet<String>();
    }

    /**
     * Adds <code>token</code> to the set of stop tokens.
     * @param token
     */
    public void addStopToken(String token) {
        getStopTokens().add(token);
    }

    /**
     * Adds <code>posTag</code> to the set of stop POS tags.
     * @param posTag
     */
    public void addStopPosTag(String posTag) {
        getStopPosTags().add(posTag);
    }

    /**
     * Constructs a new <code>StopListFilter</code> object from the given sets of tokens and POS
     * tags.
     * @param stopTokens
     * @param stopPosTags
     */
    public StopListFilter(Set<String> stopTokens, Set<String> stopPosTags) {
        this.stopTokens = stopTokens;
        this.stopPosTags = stopPosTags;
    }

    private static boolean containsAny(Set<String> s1, Iterable<String> s2) {
        for (String x : s2) {
            if (s1.contains(x)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the set backing the stop tokens.
     */
    private Set<String> getStopTokens() {
        return stopTokens;
    }

    /**
     * @return the set backing the stop POS tags.
     */
    private Set<String> getStopPosTags() {
        return stopPosTags;
    }

    private boolean tokensValid(ChunkedExtraction extr) {
        Set<String> stopTokens = getStopTokens();
        return !containsAny(stopTokens, extr.getTokens());
    }

    private boolean posTagsValid(ChunkedExtraction extr) {
        Set<String> stopPosTags = getStopPosTags();
        return !containsAny(stopPosTags, extr.getPosTags());
    }

    @Override
    /**
     * Filters out extractions that contain a stop POS tag or stop token.
     */
    public boolean doFilter(ChunkedExtraction extr) {
        return tokensValid(extr) && posTagsValid(extr);
    }

}
