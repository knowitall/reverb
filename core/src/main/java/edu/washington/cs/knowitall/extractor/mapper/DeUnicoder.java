package edu.washington.cs.knowitall.extractor.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * Converts some characters from unicode to ascii (such as directional
 * quotations)
 *
 * @author schmmd
 *
 */
public class DeUnicoder extends IndependentMapper<String> {
    static class Replacement {
        public final Pattern pattern;
        public final String replacement;

        public Replacement(Pattern pattern, String replacement) {
            this.pattern = pattern;
            this.replacement = replacement;
        }
    }

    private final List<Replacement> replacements = new ArrayList<Replacement>();

    /**
     * Constructs a new <code>DeUnicoder</code> object.
     */
    public DeUnicoder() {
        initializePatterns();
    }

    private void initializePatterns() {
        // double quotation (")
        replacements.add(new Replacement(Pattern
                .compile("[\u201c\u201d\u201e\u201f\u275d\u275e]"), "\""));

        // single quotation (')
        replacements.add(new Replacement(Pattern
                .compile("[\u2018\u2019\u201a\u201b\u275b\u275c]"), "'"));

        // non-breaking whitespace
        replacements.add(new Replacement(Pattern.compile("\\xa0"), " "));
    }

    /**
     * Remove strange characters.
     */
    public String doMap(String sent) {
        // remove diacritics
        sent = Normalizer.normalize(sent, Normalizer.Form.NFD);

        for (Replacement r : replacements) {
            sent = r.pattern.matcher(sent).replaceAll(r.replacement);
        }

        return sent;
    }

    /**
     * Applies the <code>DeUnicoder</code> mapper to each line in standard input
     * and prints the result.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        DeUnicoder remover = new DeUnicoder();
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            System.out.println(remover.doMap(line));
        }
    }

}
