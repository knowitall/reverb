package edu.washington.cs.knowitall.extractor.mapper;

import java.lang.Math;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/**
 * A mapper object for <code>NpChunkArgumentExtraction</code> objects that
 * returns the object closest to the relation.
 *
 * @author afader
 *
 */
public class ClosestArgumentMapper extends
        MaxMapper<Integer, ChunkedArgumentExtraction> {

    @Override
    /**
     * Returns the distance between <code>arg</code> and its relation, in number of words.
     */
    public Integer doValueMap(ChunkedArgumentExtraction arg) {
        ChunkedExtraction relation = arg.getRelation();
        int relStart = relation.getStart();
        int argStart = arg.getStart();
        int distance = Math.abs(argStart - relStart);
        return -distance; // return -distance since we want the minimum distance
    }

}
