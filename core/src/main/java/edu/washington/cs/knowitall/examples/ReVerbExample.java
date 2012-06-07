package edu.washington.cs.knowitall.examples;

/* For representing a sentence that is annotated with pos tags and np chunks.*/
import edu.washington.cs.knowitall.nlp.ChunkedSentence;

/* String -> ChunkedSentence */
import edu.washington.cs.knowitall.nlp.OpenNlpSentenceChunker;

/* The class that is responsible for extraction. */
import edu.washington.cs.knowitall.extractor.ReVerbExtractor;

/* The class that is responsible for assigning a confidence score to an
 * extraction.
 */
import edu.washington.cs.knowitall.extractor.conf.ConfidenceFunction;
import edu.washington.cs.knowitall.extractor.conf.ReVerbOpenNlpConfFunction;

/* A class for holding a (arg1, rel, arg2) triple. */
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;

public class ReVerbExample {

    public static void main(String[] args) throws Exception {

        String sentStr = "Michael McGinn is the mayor of Seattle.";

        // Looks on the classpath for the default model files.
        OpenNlpSentenceChunker chunker = new OpenNlpSentenceChunker();
        ChunkedSentence sent = chunker.chunkSentence(sentStr);

        // Prints out the (token, tag, chunk-tag) for the sentence
        System.out.println(sentStr);
        for (int i = 0; i < sent.getLength(); i++) {
            String token = sent.getToken(i);
            String posTag = sent.getPosTag(i);
            String chunkTag = sent.getChunkTag(i);
            System.out.println(token + " " + posTag + " " + chunkTag);
        }

        // Prints out extractions from the sentence.
        ReVerbExtractor reverb = new ReVerbExtractor();
        ConfidenceFunction confFunc = new ReVerbOpenNlpConfFunction();
        for (ChunkedBinaryExtraction extr : reverb.extract(sent)) {
            double conf = confFunc.getConf(extr);
            System.out.println("Arg1=" + extr.getArgument1());
            System.out.println("Rel=" + extr.getRelation());
            System.out.println("Arg2=" + extr.getArgument2());
            System.out.println("Conf=" + conf);
        }
    }
}
