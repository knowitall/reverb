package edu.washington.cs.knowitall.nlp;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

/***
 * Represents an ordered collection of {@link ChunkedSentence} objects with an
 * identifier string.
 *
 * @author afader
 *
 */
public class ChunkedDocument implements Iterable<ChunkedSentence> {

    private String id;
    private List<ChunkedSentence> sents;

    /**
     * Constructs a new ChunkedDocument with the given identifier and sentences.
     *
     * @param id
     * @param sents
     */
    public ChunkedDocument(String id, Iterable<ChunkedSentence> sents) {
        this.id = id;
        this.sents = ImmutableList.copyOf(sents);
    }

    @Override
    public Iterator<ChunkedSentence> iterator() {
        return getSentences().iterator();
    }

    /**
     * @return an immutable view of the sentences in this document
     */
    public List<ChunkedSentence> getSentences() {
        return sents;
    }

    /**
     * @return the id of this document
     */
    public String getId() {
        return id;
    }

}
