package edu.washington.cs.knowitall.nlp;

import java.io.IOException;

public class OpenNlpCombinedSentenceChunker implements SentenceChunker {
    private final OpenNlpSentenceChunker normalChunker;
    private final OpenNlpSentenceChunker capitalChunker;

    private final int threshold;

    public OpenNlpCombinedSentenceChunker() throws IOException {
        this(75);
    }

    public OpenNlpCombinedSentenceChunker(int threshold) throws IOException {
        this.normalChunker = new OpenNlpSentenceChunker();
        this.capitalChunker = new OpenNlpCapitalSentenceChunker();
        this.threshold = threshold;
    }

    @Override
    public ChunkedSentence chunkSentence(String sent) throws ChunkerException {
        // analyse sentence letters
        int upper = 0;
        int lower = 0;
        for (int i = 0; i < sent.length(); i++) {
            char c = sent.charAt(i);
            if (Character.isUpperCase(c)) {
                upper++;
            } else if (Character.isLowerCase(c)) {
                lower++;
            }
        }

        if (sent.length() > 10 && upper + lower > 0
                && (100 * upper) / (upper + lower) > threshold) {
            return capitalChunker.chunkSentence(sent.toUpperCase());
        } else {
            return normalChunker.chunkSentence(sent);
        }
    }
}
