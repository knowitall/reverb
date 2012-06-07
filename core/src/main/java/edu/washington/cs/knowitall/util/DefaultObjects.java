package edu.washington.cs.knowitall.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import edu.washington.cs.knowitall.extractor.HtmlSentenceExtractor;
import edu.washington.cs.knowitall.extractor.SentenceExtractor;
import edu.washington.cs.knowitall.extractor.mapper.BracketsRemover;
import edu.washington.cs.knowitall.extractor.mapper.SentenceEndFilter;
import edu.washington.cs.knowitall.extractor.mapper.SentenceLengthFilter;
import edu.washington.cs.knowitall.extractor.mapper.SentenceStartFilter;
import edu.washington.cs.knowitall.nlp.ChunkedSentenceReader;

public class DefaultObjects {

    public static final String tokenizerModelFile = "en-token.bin";
    public static final String taggerModelFile = "en-pos-maxent.bin";
    public static final String chunkerModelFile = "en-chunker.bin";
    public static final String sentDetectorModelFile = "en-sent.bin";
    public static final String confFunctionModelFile = "reverb-conf-maxent.gz";

    /** Default singleton objects */
    private static BracketsRemover BRACKETS_REMOVER;
    private static SentenceStartFilter SENTENCE_START_FILTER;
    private static SentenceEndFilter SENTENCE_END_FILTER;

    public static InputStream getResourceAsStream(String resource)
            throws IOException {
        InputStream in = DefaultObjects.class.getClassLoader()
                .getResourceAsStream(resource);
        if (in == null) {
            throw new IOException("Couldn't load resource: " + resource);
        } else {
            return in;
        }
    }

    public static void initializeNlpTools() throws IOException {
        getDefaultSentenceDetector();
        getDefaultTokenizer();
        getDefaultPosTagger();
        getDefaultChunker();
    }

    public static Tokenizer getDefaultTokenizer() throws IOException {
        return new TokenizerME(new TokenizerModel(
                getResourceAsStream(tokenizerModelFile)));
    }

    public static POSTagger getDefaultPosTagger() throws IOException {
        return new POSTaggerME(new POSModel(
                getResourceAsStream(taggerModelFile)));
    }

    public static Chunker getDefaultChunker() throws IOException {
        return new ChunkerME(new ChunkerModel(
                getResourceAsStream(chunkerModelFile)));
    }

    public static SentenceDetector getDefaultSentenceDetector()
            throws IOException {
        return new SentenceDetectorME(new SentenceModel(
                getResourceAsStream(sentDetectorModelFile)));
    }

    public static void addDefaultSentenceFilters(SentenceExtractor extractor) {
        if (BRACKETS_REMOVER == null)
            BRACKETS_REMOVER = new BracketsRemover();
        if (SENTENCE_END_FILTER == null)
            SENTENCE_END_FILTER = new SentenceEndFilter();
        if (SENTENCE_START_FILTER == null)
            SENTENCE_START_FILTER = new SentenceStartFilter();
        extractor.addMapper(BRACKETS_REMOVER);
        extractor.addMapper(SENTENCE_END_FILTER);
        extractor.addMapper(SENTENCE_START_FILTER);
        extractor.addMapper(SentenceLengthFilter.minFilter(4));
    }

    public static SentenceExtractor getDefaultSentenceExtractor()
            throws IOException {
        SentenceExtractor extractor = new SentenceExtractor();
        addDefaultSentenceFilters(extractor);
        return extractor;
    }

    public static HtmlSentenceExtractor getDefaultHtmlSentenceExtractor()
            throws IOException {
        HtmlSentenceExtractor extractor = new HtmlSentenceExtractor();
        addDefaultSentenceFilters(extractor);
        return extractor;
    }

    /**
     * Return the default sentence reader.
     *
     * @param in
     * @param htmlSource
     *            - Are sentences from an html source?
     * @return
     * @throws IOException
     */
    public static ChunkedSentenceReader getDefaultSentenceReader(Reader in,
            boolean htmlSource) throws IOException {
        if (htmlSource) {
            return getDefaultSentenceReaderHtml(in);
        } else {
            return getDefaultSentenceReader(in);
        }
    }

    public static ChunkedSentenceReader getDefaultSentenceReader(Reader in)
            throws IOException {
        ChunkedSentenceReader reader = new ChunkedSentenceReader(in,
                getDefaultSentenceExtractor());
        return reader;
    }

    public static ChunkedSentenceReader getDefaultSentenceReaderHtml(Reader in)
            throws IOException {
        ChunkedSentenceReader reader = new ChunkedSentenceReader(in,
                getDefaultHtmlSentenceExtractor());
        return reader;
    }
}
