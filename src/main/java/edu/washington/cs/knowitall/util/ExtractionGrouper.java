package edu.washington.cs.knowitall.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ExtractionGrouper<T> {

    public Map<Integer, List<T>> groupExtractions(Collection<T> extractions);

}
