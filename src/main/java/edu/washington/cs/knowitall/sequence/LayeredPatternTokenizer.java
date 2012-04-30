package edu.washington.cs.knowitall.sequence;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class that tokenizes the String representation of patterns for the
 * {@link LayeredTokenPattern} class. A pattern consists of two types of tokens:
 * layer/symbol pairs like {@code The_pos}, and meta-characters defined by
 * {@link java.util.rexex.Pattern}. This class takes a string consisting of
 * these and breaks them into tokens.
 * 
 * @author afader
 * 
 */
public class LayeredPatternTokenizer {

    // The pattern used to match layer/symbol pairs like The_pos
    private final String tokenPatternStr = "([a-zA-Z0-9\\-.,:;?!\"'`$]+)_([a-zA-Z0-9\\-]+)";
    private final Pattern tokenPattern = Pattern.compile(tokenPatternStr);

    // The allowed meta-characters
    private final String[] metaChars = { "(", "[", "{", "\\", "^", "$", "|",
            "]", "}", ")", "?", "*", "+", ".", ":", "=", "!", "<", ">"
    // "-", don't allow the range operator, since it is not well defined
    // for LayeredTokenPattern
    };
    private Set<String> metaCharSet;

    /**
     * Constructs a new tokenizer.
     */
    public LayeredPatternTokenizer() {
        metaCharSet = new HashSet<String>();
        for (String metaChar : metaChars)
            metaCharSet.add(metaChar);
    }

    /**
     * Tokenizes the given text
     * 
     * @param text
     * @return an array of Strings, one for each token in text
     * @throws SequenceException
     *             if unable to tokenize the text
     */
    public String[] tokenize(String text) throws SequenceException {

        // Code for splitting the text into tokens matching the patterns
        // See: http://snippets.dzone.com/posts/show/6453

        if (text == null) {
            text = "";
        }

        int lastMatch = 0;
        LinkedList<String> splitted = new LinkedList<String>();
        Matcher m = tokenPattern.matcher(text);

        while (m.find()) {
            String candidate = text.substring(lastMatch, m.start());
            if (!candidate.matches("^\\s*$")) {
                addCharsOfString(text, candidate, splitted);
            }
            splitted.add(m.group());
            lastMatch = m.end();
        }

        String candidate = text.substring(lastMatch);
        if (!candidate.matches("^\\s*$")) {
            addCharsOfString(text, text.substring(lastMatch), splitted);
        }
        return splitted.toArray(new String[splitted.size()]);

    }

    private void addCharsOfString(String text, String s, List<String> l)
            throws SequenceException {
        for (int i = 0; i < s.length(); i++) {
            String token = s.substring(i, i + 1);
            if (isMetaChar(token)) {
                l.add(s.substring(i, i + 1));
            } else if (!token.matches("^\\s*$")) {
                String msg = String.format(
                        "Could not tokenize pattern '%s': token '%s' is not a "
                                + "layer/symbol pair or a meta-character",
                        text, token);
                throw new SequenceException(msg);
            }
        }
    }

    /**
     * @param text
     * @return true if the given String is a symbol/layer name pair
     */
    public boolean isSymbolLayerName(String text) {
        return tokenPattern.matcher(text).matches();
    }

    /**
     * @param s
     * @return true if the given String is a meta-character
     */
    private boolean isMetaChar(String s) {
        return metaCharSet.contains(s);
    }

    /**
     * @param text
     * @return
     */
    public String[] getSymbolLayerName(String text) {
        return text.split("_");
    }

}
