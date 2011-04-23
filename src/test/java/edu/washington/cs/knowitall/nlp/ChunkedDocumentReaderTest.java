package edu.washington.cs.knowitall.nlp;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


public class ChunkedDocumentReaderTest {
    
    private String docStr;
    private InputStream docStream;
    private static ChunkedDocumentReader reader;
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Before
    public void setUp() throws Exception {

        if (reader == null) {
            reader = new ChunkedDocumentReader();
        }
        
        docStr = "<html><body><p>Smith was born in <b>Washington</b>.</p>"
            + "<p>Jones was the inventor of the stopwatch.</p></html>";
        docStream = new ByteArrayInputStream(docStr.getBytes());
    }

    @Test
    public void testReadDocumentInputStreamString() throws Exception {
        ChunkedDocument doc = reader.readDocument(docStream, "myDoc");
        assertEquals("myDoc", doc.getId());
        assertEquals(2, doc.getSentences().size());
    }

    @Test
    public void testReadDocumentFile() throws Exception {
        File temp = folder.newFile("tmp");
        FileWriter w = new FileWriter(temp);
        w.write(docStr);
        w.close();
        ChunkedDocument doc = reader.readDocument(temp);
        assertEquals(temp.getAbsolutePath(), doc.getId());
        assertEquals(2, doc.getSentences().size());
    }

    @Test
    public void testReadDocumentStringString() throws Exception {
        ChunkedDocument doc = reader.readDocument(docStr, "myDoc");
        assertEquals("myDoc", doc.getId());
        assertEquals(2, doc.getSentences().size());
    }

}
