package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;
import static edu.washington.cs.knowitall.nlp.extraction.TestExtractions.extractions;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.washington.cs.knowitall.nlp.extraction.SpanExtraction;

public class LuceneExtractionSerializerTest {
	
	private File normIndexDir;

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void setUp() throws Exception {
		normIndexDir = folder.newFolder("normIndex");
		TestIndex.createIndex(normIndexDir);
	}

	@Test
	public void testConvert() throws CorruptIndexException, IOException, ExtractionFormatException {
		IndexReader reader = IndexReader.open(FSDirectory.open(normIndexDir));
		for (int i = 0; i < reader.numDocs(); i++) {
			Document doc = reader.document(i);
			NormalizedSpanExtraction got = LuceneExtractionSerializer.fromDocument(doc);
			SpanExtraction expected = extractions.get(i);
			
			assertEquals(expected.getNumFields(), got.getNumFields());
			assertEquals(expected.getFieldNames(), got.getFieldNames());
			
			for (int j = 0; j < expected.getNumFields(); j++) {
				assertEquals(expected.getField(j).toString(), got.getField(j).toString());
				
			}
			
			assertEquals(expected.getProperties(), got.getProperties());
			
		}
	}

}
