package edu.washington.cs.knowitall.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.nlp.ChunkedDocument;
import edu.washington.cs.knowitall.nlp.ChunkedDocumentReader;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.util.WarcPage;
import edu.washington.cs.knowitall.util.WarcReader;

/**
 * A class for reading a WARC file from standard input, chunking it, and
 * printing the output. 
 * @author afader
 *
 */
public class StreamingWarcChunker extends AbstractIterator<ChunkedDocument> {
    
    private Iterator<WarcPage> iter;
    private ChunkedDocumentReader reader;
    
    public StreamingWarcChunker(InputStream in) throws IOException {
        iter = new WarcReader(in).iterator();
        reader = new ChunkedDocumentReader();
    }

    @Override
    protected ChunkedDocument computeNext() {
        while (iter.hasNext()) {
            try {
                WarcPage page = iter.next();
                String url = page.getWARC_Target_URI();
                String contents = page.getContent();
                return reader.readDocument(contents, url);
            } catch (ExtractorException e) {
                System.err.println(e);
                continue;
            }
        }
        return endOfData();
    }
    
    public static void main(String args[]) throws IOException {
        StreamingWarcChunker proc = new StreamingWarcChunker(System.in);
        while (proc.hasNext()) {
            ChunkedDocument doc = proc.next();
            String url = doc.getId();
            for (ChunkedSentence sent : doc) {
                if (sent != null && url != null) {
                    System.out.println(url + "\t" + sent.toOpenNlpFormat());
                }
            }
        }
    }

    
    

}
