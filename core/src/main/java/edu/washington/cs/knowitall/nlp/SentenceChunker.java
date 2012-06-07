package edu.washington.cs.knowitall.nlp;

public interface SentenceChunker {

    public ChunkedSentence chunkSentence(String sent) throws ChunkerException;
}
