package edu.washington.cs.knowitall.index;

import static org.junit.Assert.*;

import java.io.File;
import java.rmi.Naming;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Searchable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class IndexServerTest {
    
    private File indexDir;
    private IndexServer server;
    private String name = "MyIndex";
    private String rmiName = String.format("//localhost:1099/%s", name);
    
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        indexDir = folder.newFolder("index");
        TestIndex.createIndex(indexDir);
        server = new IndexServer(indexDir, name);
    }
    
    @Test
    public void testName() throws Exception {
        assertEquals(rmiName, server.getFullName());
    }


    @Test
    public void testIndexServer() throws Exception {

        server.startServer();
        Searchable remoteSearchable = (Searchable) Naming.lookup(rmiName);
        Document doc = remoteSearchable.doc(0);
        String tokens = doc.getField("fieldTokens0").stringValue();
        
        assertEquals("Mike", tokens);
        
        
    }

}
