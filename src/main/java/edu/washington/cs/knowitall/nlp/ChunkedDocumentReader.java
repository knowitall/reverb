package edu.washington.cs.knowitall.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;

import com.google.common.collect.Iterables;

import edu.washington.cs.knowitall.commonlib.FileUtils;
import edu.washington.cs.knowitall.extractor.ExtractorException;
import edu.washington.cs.knowitall.extractor.SentenceExtractor;
import edu.washington.cs.knowitall.util.DefaultObjects;

/***
 * A class for converting raw text into {@link ChunkedDocument} objects. The
 * behavior of this class depends on two parameters: a {@link SentenceExtractor}
 * object, which converts a String into a list of String sentences; and a
 * {@link SentenceChunker} object, which converts a String sentence into a
 * {@link ChunkedSentence} object.
 *
 * @author afader
 *
 */
public class ChunkedDocumentReader {

    private SentenceExtractor sentExtractor;
    private SentenceChunker sentChunker;

    /**
     * @param sentExtractor
     *            the object responsible for converting a String to String
     *            sentences
     * @param sentChunker
     *            the object responsible for converting a String sentence to a
     *            {@link ChunkedSentence} object
     * @throws IOException
     */
    public ChunkedDocumentReader(SentenceExtractor sentExtractor,
            SentenceChunker sentChunker) throws IOException {
        this.sentExtractor = sentExtractor;
        this.sentChunker = sentChunker;
    }

    /**
     * Uses {@link OpenNlpSentenceChunker} as the default sentence chunker.
     *
     * @param sentExtractor
     *            the object responsible for converting a String to String
     *            sentences
     * @throws IOException
     */
    public ChunkedDocumentReader(SentenceExtractor sentExtractor)
            throws IOException {
        this(sentExtractor, new OpenNlpSentenceChunker());
    }

    /**
     * Uses the object returned by
     * {@link DefaultObjects#getDefaultHtmlSentenceExtractor()} as the default
     * sentence extractor.
     *
     * @param sentChunker
     *            the object responsible for converting a String sentence to a
     *            {@link ChunkedSentence} object
     * @throws IOException
     */
    public ChunkedDocumentReader(SentenceChunker sentChunker)
            throws IOException {
        this(DefaultObjects.getDefaultHtmlSentenceExtractor(), sentChunker);
    }

    /**
     * Uses the object returned by
     * {@link DefaultObjects#getDefaultHtmlSentenceExtractor()} as the default
     * sentence extractor, and {@link OpenNlpSentenceChunker} as the default
     * sentence chunker.
     *
     * @throws IOException
     */
    public ChunkedDocumentReader() throws IOException {
        this(DefaultObjects.getDefaultHtmlSentenceExtractor(),
                new OpenNlpSentenceChunker());
    }

    /**
     * @return the object responsible for converting a String to String
     *         sentences
     */
    public SentenceExtractor getSentenceExtractor() {
        return sentExtractor;
    }

    /**
     * @return the object responsible for converting a String sentence to a
     *         {@link ChunkedSentence} object
     */
    public SentenceChunker getSentenceChunker() {
        return sentChunker;
    }

    /**
     * Reads a document from the input, assigning it the given id
     *
     * @param input
     * @param id
     * @return the document
     * @throws ExtractorException
     */
    public ChunkedDocument readDocument(InputStream input, String id)
            throws ExtractorException {
        StringWriter writer = new StringWriter();
        InputStreamReader reader = new InputStreamReader(input);
        try {
            FileUtils.pipe(reader, writer);
            return readDocument(writer.toString(), id);
        } catch (IOException e) {
            String msg = String.format("Could not read document %s", id);
            throw new ExtractorException(msg, e);
        }
    }

    /**
     * Reads a document from the given file, using
     * {@link File#getAbsolutePath()} as the id of the document.
     *
     * @param file
     * @return the document
     * @throws ExtractorException
     */
    public ChunkedDocument readDocument(File file) throws ExtractorException {
        try {
            return readDocument(new FileInputStream(file),
                    file.getAbsolutePath());
        } catch (IOException e) {
            String msg = String.format("Could not extract from %s", file);
            throw new ExtractorException(msg, e);
        }
    }

    /**
     * Reads a document from the given string, assigning it the given id
     *
     * @param docStr
     * @param id
     * @return the document
     * @throws ExtractorException
     *             if unable to run sentence extractor
     */
    public ChunkedDocument readDocument(String docStr, String id)
            throws ExtractorException {
        ArrayList<String> sents = new ArrayList<String>();
        Iterables.addAll(sents, sentExtractor.extract(docStr));
        ArrayList<ChunkedSentence> chunkedSents = new ArrayList<ChunkedSentence>(
                sents.size());

        int sentNum = 1;
        for (String sent : sents) {
            try {
                chunkedSents.add(sentChunker.chunkSentence(sent));
                sentNum++;
            } catch (ChunkerException e) {
                String msg = String.format(
                        "Could not chunk sentence %s in document %s", sentNum,
                        id);
                throw new ExtractorException(msg);
            }
        }
        return new ChunkedDocument(id, chunkedSents);
    }

}
