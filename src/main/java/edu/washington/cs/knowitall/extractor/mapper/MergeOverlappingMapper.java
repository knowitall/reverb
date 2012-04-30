package edu.washington.cs.knowitall.extractor.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * Given a set of <code>ChunkedExtraction</code>s from the same sentence, merges
 * those extractions that are next to each other or overlapping. For example,
 * given the sentence "He wants to go to the store" and the relations "wants to"
 * and "go to", returns "wants to go to".
 *
 * @author afader
 *
 */
public class MergeOverlappingMapper extends Mapper<ChunkedExtraction> {

    public static List<Range> mergeOverlapping(List<Range> ranges) {

        Collections.sort(ranges, Range.getStartComparator());
        List<Range> result = new ArrayList<Range>(ranges.size());
        if (ranges.size() > 1) {
            result.add(ranges.get(0));
            for (int i = 1; i < ranges.size(); i++) {
                Range curr = ranges.get(i);
                Range prev = result.get(result.size() - 1);
                if (prev.isAdjacentOrOverlaps(curr)) {
                    Range updated = curr.join(prev);
                    result.set(result.size() - 1, updated);
                } else {
                    result.add(curr);
                }
            }
            return result;

        } else {
            return ranges;
        }
    }

    @Override
    protected Iterable<ChunkedExtraction> doMap(
            Iterable<ChunkedExtraction> extrs) {
        List<ChunkedExtraction> extrList = new ArrayList<ChunkedExtraction>();
        Iterables.addAll(extrList, extrs);
        if (extrList.size() > 1) {
            ChunkedSentence sent = extrList.get(0).getSentence();
            List<Range> ranges = new ArrayList<Range>(extrList.size());
            for (ChunkedExtraction e : extrList) {
                ranges.add(e.getRange());
            }
            List<Range> mergedRanges = mergeOverlapping(ranges);
            List<ChunkedExtraction> result = new ArrayList<ChunkedExtraction>(
                    mergedRanges.size());
            for (Range r : mergedRanges) {
                ChunkedExtraction extr = new ChunkedExtraction(sent, r);
                result.add(extr);
            }
            return result;
        } else {
            return extrList;
        }
    }

}
