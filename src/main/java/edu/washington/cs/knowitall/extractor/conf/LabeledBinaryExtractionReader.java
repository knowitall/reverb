package edu.washington.cs.knowitall.extractor.conf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.collect.AbstractIterator;

import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.util.IterableAdapter;

/***
 * Used for reading <code>LabeledBinaryExtraction</code> objects from a plain
 * text source. The format of the source should be (for each extraction): - Line
 * 1: Source sentence tokens - Line 2: Source POS tags - Line 3: Source NP Chunk
 * tags - Line 4: Argument1 tokens - Line 5: Argument1 range start and length -
 * Line 6: Relation tokens - Line 7: Relation range start and length - Line 8:
 * Argument2 tokens - Line 9: Argument 2 range start and length - Line 10: label
 * (either 0 or 1)
 *
 * For example: Bush was US President . NNP VBD NNP NNP . B-NP O B-NP I-NP O
 * Bush 0 1 was 1 1 US President 2 2 1
 *
 * This gets loaded into a <code>LabeledBinaryExtraction</code> object
 * representing (Bush, was, US President) with a positive label (=0).
 *
 * @author afader
 *
 */
public class LabeledBinaryExtractionReader {

    private InputStream in;

    /**
     * Constructs a new reader from the given input stream.
     *
     * @param in
     * @throws IOException
     *             if unable to read the source
     */
    public LabeledBinaryExtractionReader(InputStream in) throws IOException {
        this.in = in;
    }

    /**
     * @return an <code>Iterable</code> object containing the labeled
     *         extractions
     * @throws IOException
     */
    public Iterable<LabeledBinaryExtraction> readExtractions()
            throws IOException {
        LBEIterator iter = new LBEIterator(in);
        return new IterableAdapter<LabeledBinaryExtraction>(iter);
    }

    private class LBEIterator extends AbstractIterator<LabeledBinaryExtraction> {

        private BufferedReader reader;

        public LBEIterator(InputStream in) throws IOException {
            reader = new BufferedReader(new InputStreamReader(in));
        }

        private Range readRange(String line) throws IOException {
            String[] startLen = line.split(" ");
            int start = Integer.parseInt(startLen[0]);
            int length = Integer.parseInt(startLen[1]);
            return new Range(start, length);
        }

        private String[] readNextLines() throws IOException {
            String[] lines = new String[10];
            for (int i = 0; i < 10; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IOException("Bad file format");
                } else {
                    lines[i] = line;
                }
            }
            return lines;
        }

        protected LabeledBinaryExtraction computeNext() {
            try {

                String[] lines = readNextLines();

                // First three lines define the sentence
                String[] tokens = lines[0].split(" ");
                String[] posTags = lines[1].split(" ");
                String[] npChunkTags = lines[2].split(" ");
                ChunkedSentence sent = new ChunkedSentence(tokens, posTags,
                        npChunkTags);

                // Next two lines define arg1: first is the tokens, then is the
                // range. Only need
                // the range to construct the extraction.
                Range arg1Range = readRange(lines[4]);

                // Same for the relation and arg2
                Range relRange = readRange(lines[6]);
                Range arg2Range = readRange(lines[8]);

                int label = Integer.parseInt(lines[9]);

                // Construct the extraction
                ChunkedExtraction rel = new ChunkedExtraction(sent, relRange);
                ChunkedArgumentExtraction arg1 = new ChunkedArgumentExtraction(
                        sent, arg1Range, rel);
                ChunkedArgumentExtraction arg2 = new ChunkedArgumentExtraction(
                        sent, arg2Range, rel);
                LabeledBinaryExtraction extr = new LabeledBinaryExtraction(rel,
                        arg1, arg2, label);

                return extr;

            } catch (IOException e) {
                return endOfData();
            } catch (SequenceException e) {
                return endOfData();
            }

        }
    }

}
