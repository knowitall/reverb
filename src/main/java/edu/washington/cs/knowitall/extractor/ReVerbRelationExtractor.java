package edu.washington.cs.knowitall.extractor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.extractor.mapper.ReVerbRelationMappers;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.util.DefaultObjects;


public abstract class ReVerbRelationExtractor extends RelationFirstNpChunkExtractor {

    /**
     * Definition of the "verb" of the relation pattern.
     */
    public static final String VERB =
        // Optional adverb
        "RB_pos? " +
        // Modal or other verbs
        "[MD_pos VB_pos VBD_pos VBP_pos VBZ_pos VBG_pos VBN_pos] " +
        // Optional particle/adverb
        "RP_pos? RB_pos?";

    /**
     * Definition of the "non-verb/prep" part of the relation pattern.
     */
    public static final String WORD = 
        "[$_pos PRP$_pos CD_pos DT_pos JJ_pos JJS_pos JJR_pos NN_pos " +
        "NNS_pos NNP_pos NNPS_pos POS_pos PRP_pos RB_pos RBR_pos RBS_pos " +
        "VBN_pos VBG_pos]";

    /**
     * Definition of the "preposition" part of the relation pattern.
     */
    public static final String PREP = "RB_pos? [IN_pos TO_pos RP_pos] RB_pos?";


    /**
     * The pattern (V(W*P)?)+
     */
    public static final String LONG_RELATION_PATTERN = 
        String.format("(%s (%s* (%s)+)?)+", VERB, WORD, PREP);

    /**
     * The pattern (VP?)+
     */
    public static final String SHORT_RELATION_PATTERN = 
        String.format("(%s (%s)?)+", VERB, PREP);

    /**
     * Constructs a new extractor using the default relation pattern,
     * relation mappers, and argument mappers.
     * @throws ExtractorException if unable to initialize the extractor
     */
    public ReVerbRelationExtractor() throws ExtractorException {
        initializeRelationExtractor();
        initializeArgumentExtractors();
    }

    protected abstract void initializeArgumentExtractors();

    protected void initializeRelationExtractor() throws ExtractorException {
        
        ExtractorUnion<ChunkedSentence, ChunkedExtraction> relExtractor = 
            new ExtractorUnion<ChunkedSentence, ChunkedExtraction>();
        
        try {
            relExtractor.addExtractor(new RegexExtractor(SHORT_RELATION_PATTERN));
        } catch (SequenceException e) {
            throw new ExtractorException(
                "Unable to initialize short pattern extractor", e);
        }
        
        try {
            relExtractor.addExtractor(new RegexExtractor(LONG_RELATION_PATTERN));
        } catch (SequenceException e) {
            throw new ExtractorException(
                "Unable to initialize long pattern extractor", e);
        }
        
        try {
            relExtractor.addMapper(new ReVerbRelationMappers());
        } catch (IOException e) {
            throw new ExtractorException(
                "Unable to initialize relation mappers", e);
        }
        setRelationExtractor(relExtractor);
    }
    
    /**
     * Extracts from the given text using the default sentence reader returned 
     * by {@link DefaultObjects#getDefaultSentenceReader(java.io.Reader)}.
     * @param text
     * @return an iterable object over the extractions
     * @throws ExtractorException if unable to extract
     */
    public Iterable<ChunkedBinaryExtraction> extractFromString(String text) 
        throws ExtractorException {
        try {
            StringReader in = new StringReader(text);
            return extractUsingReader(
                DefaultObjects.getDefaultSentenceReader(in));
        } catch (IOException e) {
            throw new ExtractorException(e);
        }

    }
    
    /**
     * Extracts from the given html using the default sentence reader returned 
     * by {@link DefaultObjects#.getDefaultSentenceReaderHtml(java.io.Reader)}.
     * @param html
     * @return an iterable object over the extractions
     * @throws ExtractorException if unable to extract
     */
    public Iterable<ChunkedBinaryExtraction> extractFromHtml(String html) 
        throws ExtractorException {
        StringReader in = new StringReader(html);
        try {
            return extractUsingReader(
                DefaultObjects.getDefaultSentenceReaderHtml(in));
        } catch (IOException e) {
            throw new ExtractorException(e);
        }
    }
    
    /**
     * Extracts from the given reader
     * @param reader
     * @return extractions
     * @throws ExtractorException if unable to extract
     */
    private Iterable<ChunkedBinaryExtraction> extractUsingReader(
            ChunkedSentenceReader reader) throws ExtractorException {
        
        ArrayList<ChunkedBinaryExtraction> results = 
            new ArrayList<ChunkedBinaryExtraction>();
        
        Iterable<ChunkedSentence> sents = 
            reader.getSentences();
        for (ChunkedSentence sent : sents) {
            Iterables.addAll(results, extract(sent));
        }
        return results;
    }
}
    
