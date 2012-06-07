package edu.washington.cs.knowitall.extractor.mapper;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;

/**
 * A list of mappers for <code>ReVerbExtractor</code>'s first arguments.
 *
 * @author afader
 *
 */
public class ReVerbArgument2Mappers extends
        MapperList<ChunkedArgumentExtraction> {

    public ReVerbArgument2Mappers() {
        init();
    }

    private void init() {
        // Second argument can't be a Wh word
        addFirstPosTagNotEqualsFilter("WDT");
        addFirstPosTagNotEqualsFilter("WP$");
        addFirstPosTagNotEqualsFilter("WP");
        addFirstPosTagNotEqualsFilter("WRB");
        addFirstTokenNotEqualsFilter("which");

        // Second argument should be closest to relation that passes through
        // filters
        addMapper(new ClosestArgumentMapper());

        // Second argument should be adjacent to the relation
        addMapper(new AdjacentToRelationFilter());
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
                return !extr.getPosTags().get(0).equals(tokenCopy);
            }
        });
    }

}
