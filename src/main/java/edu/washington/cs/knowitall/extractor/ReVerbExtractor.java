package edu.washington.cs.knowitall.extractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.extractor.conf.ReVerbConfFunction;
import edu.washington.cs.knowitall.extractor.mapper.ReVerbArgument1Mappers;
import edu.washington.cs.knowitall.extractor.mapper.ReVerbArgument2Mappers;
import edu.washington.cs.knowitall.extractor.mapper.ReVerbRelationMappers;
import edu.washington.cs.knowitall.nlp.ChunkedSentence;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedBinaryExtraction;
import edu.washington.cs.knowitall.nlp.extraction.ChunkedExtraction;
import edu.washington.cs.knowitall.sequence.SequenceException;
import edu.washington.cs.knowitall.util.DefaultObjects;


public class ReVerbExtractor extends RelationFirstNpChunkExtractor {

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
    public ReVerbExtractor() throws ExtractorException {
        initializeRelationExtractor();
        initializeArgumentExtractors();
    }

    private void initializeArgumentExtractors() {
        
        ChunkedArgumentExtractor arg1Extr = 
            new ChunkedArgumentExtractor(ChunkedArgumentExtractor.Mode.LEFT);
        arg1Extr.addMapper(new ReVerbArgument1Mappers());
        setArgument1Extractor(arg1Extr);

        ChunkedArgumentExtractor arg2Extr = new ChunkedArgumentExtractor(
                ChunkedArgumentExtractor.Mode.RIGHT);
        arg2Extr.addMapper(new ReVerbArgument2Mappers());
        setArgument2Extractor(arg2Extr);
    }

    private void initializeRelationExtractor() throws ExtractorException {
        
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

    /**
     * Runs the extractor on either standard input, or the given file. Uses the object returned by
     * the <code>DefaultObjects.getDefaultSentenceReaderHtml</code> method to read <code>NpChunkedSentence</code>
     * objects. Prints each sentence (prefixed by "sentence" and then a tab), followed by the extractions in the
     * form "extraction", arg1, relation, and arg2, separated by tabs. 
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
        ReVerbExtractor extractor = new ReVerbExtractor();
        System.err.println("Done.");
        
        System.err.print("Initializing confidence function...");
        ReVerbConfFunction scoreFunc = new ReVerbConfFunction();
        System.err.println("Done.");
        
        System.err.print("Initializing NLP tools...");
        ChunkedSentenceReader sentReader = DefaultObjects.getDefaultSentenceReader(reader);
        System.err.println("Done.");
        
        for (ChunkedSentence sent : sentReader.getSentences()) {
            
            sentenceCount++;
            
            String sentString = sent.getTokensAsString();
            System.out.println(String.format("sentence\t%s\t%s", sentenceCount, sentString));
            
            
            for (ChunkedBinaryExtraction extr : extractor.extract(sent)) {
                
                double score = scoreFunc.getConf(extr);
                
                String arg1 = extr.getArgument1().toString();
                String rel = extr.getRelation().toString();
                String arg2 = extr.getArgument2().toString();
                
                String extrString = String.format("%s\t%s\t%s\t%s\t%s", sentenceCount, arg1, rel, arg2, score);
                
                System.out.println("extraction\t" + extrString);
                
                extractionCount++;
            }
        }
        
        System.err.println(String.format("Got %s extractions from %s sentences.", extractionCount, sentenceCount));
    }
}
    
