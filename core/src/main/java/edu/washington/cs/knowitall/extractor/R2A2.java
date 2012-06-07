package edu.washington.cs.knowitall.extractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import edu.washington.cs.knowitall.argumentidentifier.ArgLearner;
import edu.washington.cs.knowitall.argumentidentifier.ConfidenceMetric;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.util.DefaultObjects;

public class R2A2 extends ReVerbRelationExtractor {

    protected void initializeArgumentExtractors() {
        ArgLearner arg1Extr = new ArgLearner(ArgLearner.Mode.LEFT);
        setArgument1Extractor(arg1Extr);
        ArgLearner arg2Extr = new ArgLearner(ArgLearner.Mode.RIGHT);
        setArgument2Extractor(arg2Extr);
    }

    /**
     * Runs the extractor on either standard input, or the given file. Uses the
     * object returned by the
     * <code>DefaultObjects.getDefaultSentenceReaderHtml</code> method to read
     * <code>NpChunkedSentence</code> objects. Prints each sentence (prefixed by
     * "sentence" and then a tab), followed by the extractions in the form
     * "extraction", arg1, relation, and arg2, separated by tabs.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        BufferedReader reader;
        if (args.length == 0) {
            reader = new BufferedReader(new InputStreamReader(System.in));
        } else {
            reader = new BufferedReader(new FileReader(args[0]));
        }

        int sentenceCount = 0;
        int extractionCount = 0;

        System.err.print("Initializing extractor...");
        R2A2 extractor = new R2A2();
        System.err.println("Done.");

        System.err.print("Initializing confidence function...");
        ConfidenceMetric scoreFunc = new ConfidenceMetric();
        System.err.println("Done.");

        System.err.print("Initializing NLP tools...");
        ChunkedSentenceReader sentReader = DefaultObjects
                .getDefaultSentenceReader(reader);
        System.err.println("Done.");

        for (ChunkedSentence sent : sentReader.getSentences()) {

            sentenceCount++;

            String sentString = sent.getTokensAsString();
            System.out.println(String.format("sentence\t%s\t%s", sentenceCount,
                    sentString));

            for (ChunkedBinaryExtraction extr : extractor.extract(sent)) {

                double score = scoreFunc.getConf(extr);

                String arg1 = extr.getArgument1().toString();
                String rel = extr.getRelation().toString();
                String arg2 = extr.getArgument2().toString();

                String extrString = String.format("%s\t%s\t%s\t%s\t%s",
                        sentenceCount, arg1, rel, arg2, score);

                System.out.println("extraction\t" + extrString);

                extractionCount++;
            }
        }

        System.err.println(String.format(
                "Got %s extractions from %s sentences.", extractionCount,
                sentenceCount));
    }
}
