package edu.washington.cs.knowitall.extractor.conf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedArgumentExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;

/***
 * Used for writing <code>LabeledBinaryExtraction</code> objects to plain text.
 * See the documentation for <code>LabeledBinaryExtractionReader</code> for
 * details on the format used.
 *
 * @author afader
 *
 */
public class LabeledBinaryExtractionWriter {

    private PrintStream out;

    /**
     * Constructs a new writer to the given stream.
     *
     * @param stream
     * @throws IOException
     */
    public LabeledBinaryExtractionWriter(OutputStream stream)
            throws IOException {
        out = new PrintStream(stream);
    }

    /**
     * Writes the given labeled extractions to the output stream.
     *
     * @param extrs
     * @throws IOException
     */
    public void writeExtractions(Iterable<LabeledBinaryExtraction> extrs)
            throws IOException {
        for (LabeledBinaryExtraction extr : extrs) {
            writeExtraction(extr);
        }
    }

    /**
     * Writes the given labeled extraction to the output stream.
     *
     * @param extr
     * @throws IOException
     */
    public void writeExtraction(LabeledBinaryExtraction extr)
            throws IOException {
        ChunkedSentence sent = extr.getSentence();
        out.println(sent.getTokensAsString());
        out.println(sent.getPosTagsAsString());
        out.println(sent.getChunkTagsAsString());

        ChunkedArgumentExtraction arg1 = extr.getArgument1();
        out.println(arg1.getTokensAsString());
        out.println(arg1.getRange().getStart() + " "
                + arg1.getRange().getLength());

        ChunkedExtraction rel = extr.getRelation();
        out.println(rel.getTokensAsString());
        out.println(rel.getRange().getStart() + " "
                + rel.getRange().getLength());

        ChunkedArgumentExtraction arg2 = extr.getArgument2();
        out.println(arg2.getTokensAsString());
        out.println(arg2.getRange().getStart() + " "
                + arg2.getRange().getLength());

        int label = extr.isPositive() ? 1 : 0;
        out.println(label);
    }

}
