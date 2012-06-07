package edu.washington.cs.knowitall.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

/***
 * A class for reading in sentences that have already been chunked by the
 * OpenNLP sentence chunker.
 *
 * @author afader
 *
 */
public class PreChunkedSentenceReader implements Iterable<ChunkedSentence> {

    private final BufferedReader input;

    public PreChunkedSentenceReader(BufferedReader input) {
        this.input = input;
    }

    @Override
    public Iterator<ChunkedSentence> iterator() {

        final OpenNlpChunkedSentenceParser parser = new OpenNlpChunkedSentenceParser();

        return new AbstractIterator<ChunkedSentence>() {
            protected ChunkedSentence computeNext() {
                String line;
                try {
                    while ((line = input.readLine()) != null) {
                        try {
                            return parser.parseSentence(line);
                        } catch (ParseException e) {
                            continue;
                        }
                    }
                    return endOfData();
                } catch (IOException e) {
                    return endOfData();
                }

            }
        };

    }

}
