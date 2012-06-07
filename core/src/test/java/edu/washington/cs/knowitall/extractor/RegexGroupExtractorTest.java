package edu.washington.cs.knowitall.extractor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;
import edu.washington.cs.knowitall.nlp.extraction.TestExtractions;

public class RegexGroupExtractorTest {
    
    public List<ChunkedSentence> sents = TestExtractions.sentences;

    @Before
    public void setUp() throws Exception {
    }
    
    public List<SpanExtraction> extract(RegexGroupExtractor extractor, ChunkedSentence sent) throws Exception {
        List<SpanExtraction> extrs = new ArrayList<SpanExtraction>();
        Iterables.addAll(extrs, extractor.extract(sent));
        return extrs;
    }

    @Test
    public void testExtract1() throws Exception {
        RegexGroupExtractor extractor = new RegexGroupExtractor("(B-NP_np I-NP_np*) is_tok the_tok mayor_tok of_tok (B-NP_np I-NP_np*)");
        List<SpanExtraction> extrs = extract(extractor, sents.get(0));
        assertEquals(1, extrs.size());
        SpanExtraction extr = extrs.get(0);
        assertEquals(2, extr.getNumFields());
        assertEquals("Mike", extr.getField(0).getTokensAsString());
        assertEquals("Seattle", extr.getField(1).getTokensAsString());
    }
    
    @Test
    public void testExtract2() throws Exception {
        RegexGroupExtractor extractor = new RegexGroupExtractor("(B-NP_np I-NP_np*) is_tok the_tok (NN_pos) of_tok (B-NP_np I-NP_np*)");
        List<SpanExtraction> extrs = extract(extractor, sents.get(0));
        assertEquals(1, extrs.size());
        SpanExtraction extr = extrs.get(0);
        assertEquals(3, extr.getNumFields());
        assertEquals("Mike", extr.getField(0).getTokensAsString());
        assertEquals("mayor", extr.getField(1).getTokensAsString());
        assertEquals("Seattle", extr.getField(2).getTokensAsString());
    }

}
