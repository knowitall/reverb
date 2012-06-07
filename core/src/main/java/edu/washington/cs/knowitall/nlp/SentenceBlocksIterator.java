package edu.washington.cs.knowitall.nlp;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.AbstractIterator;

import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.extractor.SentenceExtractor;

public class SentenceBlocksIterator extends AbstractIterator<String> {

    private Iterator<String> sentIter;
    private Iterator<String> blockIter;
    private SentenceExtractor sentExtractor;

    public SentenceBlocksIterator(Iterator<String> blockIter,
            SentenceExtractor sentExtractor) {
        this.blockIter = blockIter;
        this.sentExtractor = sentExtractor;
    }

    private void computeNextSentIter() {
        String block = blockIter.next().trim();
        if (block.length() == 0) {
            sentIter = new ArrayList<String>().iterator();
        } else {
            try {
                sentIter = sentExtractor.extract(block).iterator();
            } catch (ExtractorException e) {
                sentIter = new ArrayList<String>().iterator();
            }
        }
    }

    protected String computeNext() {
        if (sentIter == null && blockIter.hasNext()) {
            computeNextSentIter();
        } else if (sentIter == null && !blockIter.hasNext()) {
            return endOfData();
        }

        while (sentIter.hasNext() || blockIter.hasNext()) {
            if (sentIter.hasNext()) {
                return sentIter.next();
            } else if (blockIter.hasNext()) {
                computeNextSentIter();
            }
        }
        return endOfData();
    }
}
