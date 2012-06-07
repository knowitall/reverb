package edu.washington.cs.knowitall.normalization;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.normalization.HeadNounNormalizer;

public class HeadNounNormalizerTest {
    
    private ChunkedSentence sent;
    private ChunkedExtraction field1, field2, field3;

    @Before
    public void setUp() throws Exception {
        
        List<String> tokens = Arrays.asList("The President of the United States and black shoes and blue".split(" "));
        List<String> posTags = Arrays.asList("DT NNP IN DT NNP NNP CC JJ NNS CC JJ".split(" "));
        List<String> npChunkTags = Arrays.asList("O O O O O O O O O O O".split(" "));
        sent = new ChunkedSentence(tokens, posTags, npChunkTags);
        
        field1 = new ChunkedExtraction(sent, new Range(0, 6));
        field2 = new ChunkedExtraction(sent, new Range(7, 2));
        field3 = new ChunkedExtraction(sent, new Range(10, 1));
        
    }

    @Test
    public void testNormalizeField() {
        
        HeadNounNormalizer normalizer = new HeadNounNormalizer();
        
        assertEquals("The President of the United States", normalizer.normalizeField(field1).toString());
        assertEquals("shoe", normalizer.normalizeField(field2).toString());
        assertEquals("blue", normalizer.normalizeField(field3).toString());
        
    }
}
