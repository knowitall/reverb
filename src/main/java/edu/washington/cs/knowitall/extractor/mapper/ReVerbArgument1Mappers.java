package edu.washington.cs.knowitall.extractor.mapper;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;

/**
 * A list of mappers for <code>ReVerbExtractor</code>'s second arguments.
 *
 * @author afader
 *
 */
public class ReVerbArgument1Mappers extends
        MapperList<ChunkedArgumentExtraction> {

    public ReVerbArgument1Mappers() {
        init();
    }

    private void init() {

        // First argument can't be an existential "there"
        addFirstPosTagNotEqualsFilter("EX");

        // First argument can't be a Wh word
        addFirstPosTagNotEqualsFilter("WDT");
        addFirstPosTagNotEqualsFilter("WP$");
        addFirstPosTagNotEqualsFilter("WRB");
        addFirstPosTagNotEqualsFilter("WP");

        // First argument can't be a preposition
        addFirstPosTagNotEqualsFilter("IN");

        // Can't be "that"
        addFirstTokenNotEqualsFilter("that");
        addFirstTokenNotEqualsFilter("which");

        // Can't be reflexive pronoun
        addFirstTokenNotEqualsFilter("myself");
        addFirstTokenNotEqualsFilter("yourself");
        addFirstTokenNotEqualsFilter("himself");
        addFirstTokenNotEqualsFilter("herself");
        addFirstTokenNotEqualsFilter("itself");
        addFirstTokenNotEqualsFilter("oneself");
        addFirstTokenNotEqualsFilter("ourselves");
        addFirstTokenNotEqualsFilter("ourself");
        addFirstTokenNotEqualsFilter("yourselves");
        addFirstTokenNotEqualsFilter("themselves");

        // First argument can't match "ARG1, REL" "ARG1 and REL" or
        // "ARG1, and REL"
        addMapper(new ConjunctionCommaArgumentFilter());

        // First argument should be closest to relation that passes through
        // filters
        addMapper(new ClosestArgumentMapper());
    }

    private void addFirstPosTagNotEqualsFilter(String posTag) {
        final String posTagCopy = posTag;
        addMapper(new FilterMapper<ChunkedArgumentExtraction>() {
            public boolean doFilter(ChunkedArgumentExtraction extr) {
                return !extr.getPosTags().get(0).equals(posTagCopy);
            }
        });
    }

    private void addFirstTokenNotEqualsFilter(String token) {
        final String tokenCopy = token;
        addMapper(new FilterMapper<ChunkedArgumentExtraction>() {
            public boolean doFilter(ChunkedArgumentExtraction extr) {
                return !extr.getTokens().get(0).equals(tokenCopy);
            }
        });
    }

}
