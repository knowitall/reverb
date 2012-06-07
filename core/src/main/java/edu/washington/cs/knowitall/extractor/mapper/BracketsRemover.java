package edu.washington.cs.knowitall.extractor.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Removes square brackets, curly braces, parentheses, and the contained text,
 * from strings.
 *
 * @author afader
 *
 */
public class BracketsRemover extends IndependentMapper<String> {

    private final String[] startBrackets;
    private final String[] endBrackets;
    private ArrayList<Pattern> patterns;

    /**
     * Constructs a new <code>BracketsRemover</code> object.
     */
    public BracketsRemover(String[] startBrackets, String[] endBrackets) {
        this.startBrackets = startBrackets;
        this.endBrackets = endBrackets;
        initializePatterns();
    }

    public BracketsRemover() {
        this(new String[] { "(", "[", "{", "<" }, new String[] { ")", "]", "}",
                ">" });
    }

    private void initializePatterns() {
        int numBrackets = startBrackets.length;
        patterns = new ArrayList<Pattern>();
        for (int i = 0; i < numBrackets; i++) {
            String start = Pattern.quote(startBrackets[i]);
            String end = Pattern.quote(endBrackets[i]);
            String pattern = start + ".*" + end + "\\s*";
            patterns.add(Pattern.compile(pattern));
        }
    }

    /**
     * Returns a copy of <code>sent</code> with its brackets, and the contained
     * text, removed.
     */
    public String doMap(String sent) {
        for (Pattern p : patterns) {
            sent = p.matcher(sent).replaceAll("");
        }
        return sent;
    }

    /**
     * Applies the <code>BracketsRemover</code> mapper to each line in standard
     * input and prints the result.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                System.in));
        BracketsRemover remover = new BracketsRemover();
        for (String line = reader.readLine(); line != null; line = reader
                .readLine()) {
            System.out.println(remover.doMap(line));
        }
    }

}
