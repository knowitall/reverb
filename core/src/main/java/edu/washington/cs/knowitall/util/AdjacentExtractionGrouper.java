package edu.washington.cs.knowitall.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

public class AdjacentExtractionGrouper implements
        ExtractionGrouper<ChunkedExtraction> {

    private List<ChunkedExtraction> asSortedList(
            Collection<ChunkedExtraction> extrs) {
        List<ChunkedExtraction> sorted = new ArrayList<ChunkedExtraction>(extrs);
        Collections.sort(sorted, new Comparator<ChunkedExtraction>() {
            public int compare(ChunkedExtraction e1, ChunkedExtraction e2) {
                Integer s1 = e1.getStart();
                Integer s2 = e2.getStart();
                return s1.compareTo(s2);
            }
        });
        return sorted;
    }

    @Override
    public Map<Integer, List<ChunkedExtraction>> groupExtractions(
            Collection<ChunkedExtraction> extractions) {

        if (extractions.size() < 2) {
            HashMap<Integer, List<ChunkedExtraction>> results = new HashMap<Integer, List<ChunkedExtraction>>();
            results.put(0, new ArrayList<ChunkedExtraction>(extractions));
            return results;
        }

        List<ChunkedExtraction> sorted = asSortedList(extractions);
        HashMap<Integer, List<ChunkedExtraction>> results = new HashMap<Integer, List<ChunkedExtraction>>();
        int groupNum = 0;
        ChunkedExtraction current = sorted.get(0);

        results.put(0, new ArrayList<ChunkedExtraction>());
        results.get(0).add(current);

        for (int i = 1; i < sorted.size(); i++) {
            ChunkedExtraction e = sorted.get(i);
            if (!e.isAdjacentOrOverlaps(current)) {
                groupNum++;
                results.put(groupNum, new ArrayList<ChunkedExtraction>());
            }
            results.get(groupNum).add(e);
            current = e;
        }

        return results;
    }

}
