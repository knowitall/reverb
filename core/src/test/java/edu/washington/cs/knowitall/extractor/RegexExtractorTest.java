package edu.washington.cs.knowitall.extractor;


import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.extractor.RegexExtractor;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

public class RegexExtractorTest {

    private ChunkedSentence sent1;
    private String verb = "[VB_pos VBD_pos VBG_pos VBN_pos VBP_pos VBZ_pos]";
    private String np = "B-NP_np I-NP_np*";
    private String prep = "[IN_pos TO_pos]";


    @Before
    public void setUp() throws Exception {
        sent1 = new ChunkedSentence(
                new String[] { "Obama", "was", "a", "professor", "of", "Law", "at", "UChicago", "." },
                new String[] { "NNP", "VBD", "DT", "NN", "IN", "NN", "IN", "NNP", "." },
                new String[] { "B-NP", "O", "B-NP", "I-NP", "I-NP", "I-NP", "O", "B-NP", "O" }
        );
    }

    @Test
    public void testExtract1() throws Exception {
        String pattern = "(" + verb + np + prep + "|" + verb + ")+";
        RegexExtractor extractor = new RegexExtractor(pattern);
        Iterable<ChunkedExtraction> extrIter = extractor.extract(sent1);
        List<ChunkedExtraction> extrs = new ArrayList<ChunkedExtraction>();
        Iterables.addAll(extrs, extrIter);
        Assert.assertEquals(1, extrs.size());

        ChunkedExtraction extr = extrs.get(0);
        Assert.assertEquals("was a professor of Law at", extr.toString());
    }

}
