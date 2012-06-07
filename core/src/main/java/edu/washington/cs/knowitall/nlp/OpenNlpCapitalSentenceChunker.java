package edu.washington.cs.knowitall.nlp;

import java.io.IOException;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import edu.washington.cs.knowitall.util.DefaultObjects;

/**
 * A class that combines OpenNLP tokenizer, POS tagger, and chunker objects into
 * a single object that converts String sentences that are all capitalized
 * letters to {@link ChunkedSentence} objects.
 *
 * @author schmmd
 *
 */
public class OpenNlpCapitalSentenceChunker extends OpenNlpSentenceChunker {
    private static final String taggerModelFile = "/en-pos-caps-maxent.bin";
    private static final String chunkerModelFile = "/en-chunker-caps.bin";

    /**
     * Constructs a new object using the default models.
     *
     * @throws IOException
     *             if unable to load the models.
     */
    public OpenNlpCapitalSentenceChunker() throws IOException {
        super(DefaultObjects.getDefaultTokenizer(), new POSTaggerME(
                new POSModel(
                        OpenNlpCapitalSentenceChunker.class
                                .getResourceAsStream(taggerModelFile))),
                new ChunkerME(new ChunkerModel(
                        OpenNlpCapitalSentenceChunker.class
                                .getResourceAsStream(chunkerModelFile))));
    }
}
