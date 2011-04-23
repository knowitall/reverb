package edu.washington.cs.knowitall.streaming;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.washington.cs.knowitall.io.BufferedReaderIterator;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkerException;
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;

/**
 * A class for reading the Google 1B tab-separated file, chunking the 
 * sentences, and printing a (url, chunked sentence) pair to standard 
 * output.
 * @author afader
 *
 */
public class StreamingG1bChunker {
    
    // The expected number of tab-separated fields per line
    private static final int NUM_FIELDS = 15;
    
    // The index of the sentence in the line
    private static final int SENT_INDEX = 13;
    
    // The index of the URL in the line
    private static final int URL_INDEX = 14;
    
    /**
     * Reads records in, one per line, via standard input. Selects the fields
     * containing the URL and the sentence, chunks the sentence, and then
     * prints the URL and sentence (in OpenNLP chunked sentence format) to
     * standard output.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws IOException {
        
        OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
        
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        BufferedReaderIterator lineIter = new BufferedReaderIterator(r);
        
        while (lineIter.hasNext()) {
            String line = lineIter.next();
            String[] fields = line.split("\t");
            if (fields.length == NUM_FIELDS) {
                String url = fields[URL_INDEX];
                String sentStr = fields[SENT_INDEX];
                try {
                ChunkedSentence sent = chunker.chunkSentence(sentStr);
                    if (sent != null && url != null) {
                        System.out.println(String.format("%s\t%s",
                                url, sent.toOpenNlpFormat()));
                    }
                } catch (ChunkerException e) {
                    continue;
                }
            }
        }
        
    }

}
