package edu.washington.cs.knowitall.extractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;
import edu.washington.cs.knowitall.sequence.LayeredTokenMatcher;
import edu.washington.cs.knowitall.sequence.LayeredTokenPattern;

public class RegexGroupExtractor extends
        Extractor<ChunkedSentence, SpanExtraction> {

    private LayeredTokenPattern pattern;

    public RegexGroupExtractor(LayeredTokenPattern pattern) {
        this.pattern = pattern;
    }

    public RegexGroupExtractor(String patternStr) {
        this(new LayeredTokenPattern(patternStr));
    }

    protected Collection<SpanExtraction> extractCandidates(ChunkedSentence sent)
            throws ExtractorException {
        LayeredTokenMatcher m = pattern.matcher(sent);
        List<SpanExtraction> results = new ArrayList<SpanExtraction>();
        while (m.find()) {
            int numFields = m.groupCount();
            List<Range> fieldRanges = new ArrayList<Range>();
            for (int i = 0; i < numFields; i++) {
                int start = m.start(i + 1);
                int end = m.end(i + 1);
                int len = end - start;
                if (start < 0 || end < 0)
                    break;
                fieldRanges.add(new Range(start, len));
            }
            if (fieldRanges.size() > 0) {
                SpanExtraction extr = new SpanExtraction(sent, fieldRanges);
                results.add(extr);
            }
        }
        return results;
    }

}
