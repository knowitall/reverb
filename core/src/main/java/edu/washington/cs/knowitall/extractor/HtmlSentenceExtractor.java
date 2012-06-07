package edu.washington.cs.knowitall.extractor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetector;

import edu.washington.cs.knowitall.extractor.mapper.BracketsRemover;
import edu.washington.cs.knowitall.extractor.mapper.SentenceEndFilter;
import edu.washington.cs.knowitall.extractor.mapper.SentenceLengthFilter;
import edu.washington.cs.knowitall.extractor.mapper.SentenceStartFilter;
import edu.washington.cs.knowitall.util.HtmlUtils;

/**
 * An <code>Extractor</code> class for extracting <code>NpChunkedSentence</code> objects from a
 * <code>String</code> containing HTML. Is backed by an OpenNLP <code>SentenceDetector</code> object.
 * Uses the code in <code>HtmlUtils</code> to extract plain text from HTML.
 * @author afader
 *
 */
public class HtmlSentenceExtractor extends SentenceExtractor {

    /**
     * Constructs a new <code>SentenceExtractor</code> object using the given OpenNLP <code>SentenceDetector</code>
     * object.
     * @param detector
     */
    public HtmlSentenceExtractor(SentenceDetector detector) {
        super(detector);
    }

    /**
     * Constructs a new <code>HtmlSentenceExtractor</code> object using the default OpenNLP
     * <code>SentenceDetector</code> object, as returned by <code>DefaultObjects.getDefaultSentenceDetector()</code>.
     * @throws IOException
     */
    public HtmlSentenceExtractor() throws IOException {
        super();
    }

    @Override
    /**
     * Extracts sentences from the given HTML.
     */
    protected Collection<String> extractCandidates(String htmlBlock) {

        String content = HtmlUtils.removeHtml(htmlBlock);
        String[] lines = content.split("\n");
        List<String> results = new ArrayList<String>();
        SentenceDetector detector = getSentenceDetector();
        for (String line : lines) {
            line = line.trim();
            for (String sent : detector.sentDetect(line)) {
                if (!sent.trim().equals("")) {
                    results.add(sent);
                }
            }
        }
        return results;

    }

    /**
     * Extracts sentences from HTML passed via standard input, or through a file given as an argument
     * to the program. Removes brackets from sentences using the <code>BracketsRemover</code> mapper class,
     * and filters sentences using the <code>SentenceEndFilter</code>, <code>SentenceStartFilter</code>, and
     * <code>SentenceLengthFilter</code> mapper classes. Prints the resulting sentences to standard output,
     * one sentence per line.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        BufferedReader in;
        if (args.length == 1) {
            in = new BufferedReader(new FileReader(args[0]));
        } else {
            in = new BufferedReader(new InputStreamReader(System.in));
        }

        StringBuffer sb = new StringBuffer();
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            sb.append(line);
        }

        HtmlSentenceExtractor extractor = new HtmlSentenceExtractor();
        extractor.addMapper(new BracketsRemover());
        extractor.addMapper(new SentenceEndFilter());
        extractor.addMapper(new SentenceStartFilter());
        extractor.addMapper(SentenceLengthFilter.minFilter(4));

        Iterable<String> sents = extractor.extract(sb.toString());
        for (String sent : sents) {
            System.out.println(sent);
        }

    }

}
