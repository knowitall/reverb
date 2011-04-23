package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;
import static edu.washington.cs.knowitall.nlp.extraction.TestExtractions.extractions;


import java.io.File;
import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ExtractionIndexSearcherTest {
    
    private File indexDir;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        indexDir = folder.newFolder("idx");
        TestIndex.createIndex(indexDir);
    }

    @Test
    public void testExtraction() throws IOException, ExtractionFormatException {
        Directory d = FSDirectory.open(indexDir);
        ExtractionIndexSearcher searcher = new ExtractionIndexSearcher(d);
        NormalizedSpanExtraction extr = searcher.extraction(0);
        ChunkedBinaryExtraction expected = extractions.get(0);
        assertEquals(expected.getArgument1().toString(), extr.getField("ARG1").toString());
        assertEquals(expected.getArgument2().toString(), extr.getField("ARG2").toString());
        assertEquals(expected.getRelation().toString(), extr.getField("REL").toString());
    }

}
