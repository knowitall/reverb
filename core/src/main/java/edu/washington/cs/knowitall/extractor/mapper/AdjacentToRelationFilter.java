package edu.washington.cs.knowitall.extractor.mapper;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A filter that returns only arguments that are adjacent to the relation.
 * @author afader
 *
 */
public class AdjacentToRelationFilter extends FilterMapper<ChunkedArgumentExtraction> {

    /**
     * Returns <code>true</code> if the given argument is adjacent to its relation.
     */
    public boolean doFilter(ChunkedArgumentExtraction arg) {
        ChunkedExtraction rel = arg.getRelation();
        Range argRange = arg.getRange();
        Range relRange = rel.getRange();
        return argRange.isAdjacentTo(relRange);
    }

}
