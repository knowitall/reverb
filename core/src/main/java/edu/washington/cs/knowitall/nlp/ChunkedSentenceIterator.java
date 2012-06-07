package edu.washington.cs.knowitall.nlp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;

/**
 * A class that combines an Iterator<String> object over sentences with a
 * SentenceChunker object to produce an iterator over {@link ChunkedSentence}
 * objects.
 *
 * @author afader
 *
 */
public class ChunkedSentenceIterator extends AbstractIterator<ChunkedSentence> {

    private SentenceChunker chunker;
    private Iterator<String> sentIter;
    private List<Predicate<ChunkedSentence>> filters;

    private long lastComputeTime = 0;

    /**
     * @param sentIter
     *            an iterator over <code>String</code> sentences.
     * @param chunker
     */
    public ChunkedSentenceIterator(Iterator<String> sentIter,
            SentenceChunker chunker) {
        this.sentIter = sentIter;
        this.chunker = chunker;
        this.filters = new ArrayList<Predicate<ChunkedSentence>>();
    }

    /**
     *
     * @return the time of the last computation in nanoseconds
     */
    public long getLastComputeTime() {
        return this.lastComputeTime;
    }

    /***
     * If a chunked sentence is filtered, it will not be returned.
     *
     * @param filter
     *            A predicate to test chunked sentences against.
     */
    public void addFilter(Predicate<ChunkedSentence> filter) {
        filters.add(filter);
    }

    @Override
    protected ChunkedSentence computeNext() {
        while (sentIter.hasNext()) {
            try {
                long start = System.nanoTime();

                ChunkedSentence chunkedSentence = chunker
                        .chunkSentence(sentIter.next());
                for (Predicate<ChunkedSentence> filter : this.filters) {
                    if (filter.apply(chunkedSentence)) {
                        continue;
                    }
                }

                lastComputeTime = System.nanoTime() - start;

                return chunkedSentence;
            } catch (ChunkerException e) {
                continue;
            }

        }

        lastComputeTime = 0;
        return endOfData();
    }
}
