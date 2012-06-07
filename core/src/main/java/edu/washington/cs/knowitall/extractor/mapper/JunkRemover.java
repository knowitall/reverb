package edu.washington.cs.knowitall.extractor.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 *
 * @author schmmd
 *
 */
public class JunkRemover extends IndependentMapper<String> {

    private final String junk;
    private ArrayList<Pattern> patterns;

    /**
     * Constructs a new <code>JunkRemover</code> object.
     */
    public JunkRemover(String junk) {
        this.junk = junk;
        initializePatterns();
    }

    public JunkRemover() {
        this("()[]{}<>|\\\"");
    }

    private void initializePatterns() {
        patterns = new ArrayList<Pattern>(4);

        // characters in this.junk
        patterns.add(Pattern.compile("[" + Pattern.quote(junk) + "]+"));

        // remove diacritics
        patterns.add(Pattern.compile("\\p{InCombiningDiacriticalMarks}+"));

        // non-ascii
        patterns.add(Pattern.compile("[^\\p{ASCII}]+"));

        //
        patterns.add(Pattern.compile("\\p{Cntrl}"));
    }

    /**
     * Remove strange characters.
     */
    public String doMap(String sent) {
        // convert diacritics into two characters, they will be removed by a
        // pattern
        sent = Normalizer.normalize(sent, Normalizer.Form.NFD);

        for (Pattern p : patterns) {
            sent = p.matcher(sent).replaceAll("");
        }

        return sent;
    }

    /**
     * Applies the <code>JunkRemover</code> mapper to each line in standard
     * input and prints the result.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        JunkRemover remover = new JunkRemover();
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            System.out.println(remover.doMap(line));
        }
    }

}
