package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ExtractionIndexerTest {
    
    private File normIndexDir;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        normIndexDir = folder.newFolder("normIndex");
        TestIndex.createIndex(normIndexDir);
    }

    @Test
    public void testExtractionIndexerIndexWriter1() 
        throws CorruptIndexException, LockObtainFailedException, IOException {
        
        IndexReader reader = IndexReader.open(FSDirectory.open(normIndexDir));
        Document doc1 = reader.document(0);
        List<Fieldable> fields = doc1.getFields();
        List<String> fieldNames = new ArrayList<String>(fields.size());
        for (Fieldable field : fields) fieldNames.add(field.name());
        
        assertEquals(24, fields.size());
        
        assertEquals("3", doc1.getField("numFields").stringValue());
        
        String[] expectedFields = {
            "prop_conf", "prop_docId",
            "sentTokens", "sentPosTags", "sentChunkTags", "numFields",
            "fieldName0", "fieldTokens0", "fieldStart0", "fieldLength0", "fieldNormTokens0", "fieldNormPosTags0",
            "fieldName1", "fieldTokens1", "fieldStart1", "fieldLength1", "fieldNormTokens1", "fieldNormPosTags1",
            "fieldName2", "fieldTokens2", "fieldStart2", "fieldLength2", "fieldNormTokens2", "fieldNormPosTags2",
        };
        
        for (String expected : expectedFields) {
            assertTrue(fieldNames.contains(expected));
        }
        
        assertEquals(ChunkedBinaryExtraction.ARG1, doc1.getField("fieldName0").stringValue());
        assertEquals(ChunkedBinaryExtraction.REL, doc1.getField("fieldName1").stringValue());
        assertEquals(ChunkedBinaryExtraction.ARG2, doc1.getField("fieldName2").stringValue());
        
        String[] sentTokens = doc1.getField("sentTokens").stringValue().split(" ");
        String[] sentPosTags = doc1.getField("sentPosTags").stringValue().split(" ");
        String[] sentChunkTags = doc1.getField("sentChunkTags").stringValue().split(" ");
        
        assertEquals(sentTokens.length, sentPosTags.length);
        assertEquals(sentPosTags.length, sentChunkTags.length);
        
        for (int i = 0; i < 3; i++) {
            String[] toks = doc1.getField("fieldTokens"+i).stringValue().split(" ");
            int start = Integer.parseInt(doc1.getField("fieldStart"+i).stringValue());
            int length = Integer.parseInt(doc1.getField("fieldLength"+i).stringValue());
            assertEquals(length, toks.length);
            for (int j = 0; j < toks.length; j++) {
                assertEquals(toks[j], sentTokens[start+j]);
            }
        }
        
        Document doc3 = reader.document(2);
        assertEquals("xanthinurium", doc3.getField("fieldNormTokens2").stringValue());
        
    }


}
