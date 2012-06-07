package edu.washington.cs.knowitall.sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

/**
 * <p>
 * A class that defines a regular expression over the tokens appearing in a
 * {@link LayeredSequence} object.
 * </p>
 * <p>
 * For example, suppose we want to find parts of sentences that match the
 * pattern "DT cow", where "DT" is the part-of-speech tag representing a
 * determiner. Assume that sentences are represented as {@link LayeredSequence}
 * objects, where the words layer has the name "word" and the part-of-speech
 * layer has the name "pos". Then the above pattern can be constructed by
 * calling {@code new LayeredTokenPattern(
 * "DT_pos cow_word")}. Given a test sentence {@code sent}, the
 * {@link #matcher(LayeredSequence)} method will return a
 * {@link LayeredTokenMatcher} object that will allow you to access the ranges
 * and groups.
 * </p>
 * <p>
 * The patterns are expressed using the standard {@link java.util.regex.Pattern}
 * language, but with the following changes.
 * </p>
 * <p>
 * The basic unit of match is not a character, but instead a token. A token
 * consists of two parts: a value and a layer name. A token is expressed using
 * an underscore to separate the two. For example {@code Foo_bar} will match
 * when the token @{code Foo} appears on the layer with the name {@code bar}. In
 * the example above, the token {@code DT_pos} will match the word- POS pair
 * {@code (w, p)} pair when {@code p = DT}. The value of {@code w} is allowed to
 * be anything. Currently there is no way to match the value of multiple layers
 * at once (e.g. match all occurrences of "bank" that are nouns).
 * </p>
 * <p>
 * The value of a token can only have characters from this set:
 * {@code [a-zA-Z0-9\\-.,:;?!"'`]}. The layer name can only have characters from
 * this set: {@code [a-zA-Z0-9\\-]}.
 * </p>
 * <p>
 * When expressing a pattern, tokens must be space separated.
 * </p>
 * <p>
 * In the following examples {@code pos} refers to a part-of-speech layer, and
 * {@code word} refers to a word layer.
 * </p>
 * <ul>
 * <li>
 * {@code ^John_word lives_word in_word NNP_pos+} - matches sentences that start
 * with "John lives in" and then is followed by at least one proper noun.</li>
 * <li>
 * {@code ^(NNP_pos+) lives_word in_word (NNP_pos+) ._pos$} - matches sentences
 * that start with at least one proper noun, followed by "lives in", followed by
 * at least one proper noun, and then ending with a period. Captures the two
 * proper nouns as groups (see {@link LayeredTokenMatcher}).</li>
 * </ul>
 * 
 * @author afader
 * 
 */
public class LayeredTokenPattern {

    // The caller-supplied pattern string
    private String patternString;

    // Dealing with the tokenized pattern
    private LayeredPatternTokenizer tokenizer;
    private int patternLength;
    private String[] patternTokens;
    private String[] patternSymbols;
    private String[] patternLayerNames;

    // Dealing with layers and their alphabets
    private List<String> layerNames;
    private Map<String, Set<String>> layerAlphabets;

    // Dealing with the encoded version of the pattern
    private Encoder encoder;
    private String encodedPatternString;
    private Pattern encodedPattern;

    /**
     * Constructs a new instance from the given String pattern
     * 
     * @param patternString
     * @throws SequenceException
     *             if unable to compile patternString
     */
    public LayeredTokenPattern(String patternString) throws SequenceException {
        this.patternString = patternString;
        tokenizePattern();
        validatePattern();
        buildAlphabets();
        buildEncoder();
        encodePattern();
    }

    @Override
    public String toString() {
        return this.patternString;
    }

    /**
     * Checks to make sure the caller-supplied patternString is valid
     * 
     * @throws SequenceException
     */
    private void validatePattern() throws SequenceException {
        for (int i = 0; i < patternLength - 1; i++) {
            String t1 = patternTokens[i];
            String t2 = patternTokens[i + 1];
            if (t1.equals("[") && t2.equals("^")) {
                String msg = String.format(
                        "Could not create pattern '%s': negative classes are not "
                                + "supported", patternString);
                throw new SequenceException(msg);
            }
        }
    }

    /**
     * Tokenizes the pattern using {@link LayeredPatternTokenizer}, and sets the
     * values of patternSymbols and patternLayerNames.
     * 
     * @throws SequenceException
     */
    private void tokenizePattern() throws SequenceException {
        tokenizer = new LayeredPatternTokenizer();
        patternTokens = tokenizer.tokenize(patternString);
        patternLength = patternTokens.length;
        patternSymbols = new String[patternLength];
        patternLayerNames = new String[patternLength];
        for (int i = 0; i < patternLength; i++) {
            String token = patternTokens[i];
            if (tokenizer.isSymbolLayerName(token)) {
                String[] splitToken = tokenizer.getSymbolLayerName(token);
                patternSymbols[i] = splitToken[0];
                patternLayerNames[i] = splitToken[1];
            } else {
                patternSymbols[i] = null;
                patternLayerNames[i] = null;
            }
        }
    }

    /**
     * Builds the alphabets for each layer in the pattern. These are then fed to
     * the constructor of {@link Encoder}.
     */
    private void buildAlphabets() {
        layerNames = new ArrayList<String>();
        layerAlphabets = new HashMap<String, Set<String>>();
        for (int i = 0; i < patternLength; i++) {
            String layerName = patternLayerNames[i];
            if (layerName != null && !layerAlphabets.containsKey(layerName)) {
                layerNames.add(layerName);
                layerAlphabets.put(layerName, new HashSet<String>());
            }
            String symbol = patternSymbols[i];
            if (layerName != null && symbol != null) {
                layerAlphabets.get(layerName).add(symbol);
            }
        }
    }

    /**
     * Builds a new encoder using the alphabets from the pattern. This encoder
     * will map tuples of values to characters.
     * 
     * @throws SequenceException
     */
    private void buildEncoder() throws SequenceException {
        List<Set<String>> sets = new ArrayList<Set<String>>();
        for (String layerName : layerNames) {
            sets.add(layerAlphabets.get(layerName));
        }
        encoder = new Encoder(sets);
    }

    /**
     * Uses the {@link Encoder} object to take the tokenized pattern and create
     * an encoded representation of it, that can then be compiled as a regular
     * {@link java.util.regex.Pattern} object.
     * 
     * @throws SequenceException
     */
    private void encodePattern() throws SequenceException {
        String[] encodedTokens = new String[patternLength];
        for (int i = 0; i < patternLength; i++) {
            String symbol = patternSymbols[i];
            String layerName = patternLayerNames[i];
            if (symbol == null || layerName == null) {
                encodedTokens[i] = patternTokens[i];
            } else {
                int layerIndex = layerNames.indexOf(layerName);
                char[] classEncoding = encoder.encodeClass(layerIndex, symbol);
                String classEncodingString = new String(classEncoding);
                encodedTokens[i] = "[" + Pattern.quote(classEncodingString)
                        + "]";
            }
        }
        encodedPatternString = Joiner.on("").join(encodedTokens);
        encodedPattern = Pattern.compile(encodedPatternString);
    }

    /**
     * Returns a matcher object, which can be used to scan seq for any
     * subsequences that match this pattern.
     * 
     * @param seq
     * @return the matcher
     * @throws SequenceException
     *             if unable to create a matcher over seq
     */
    public LayeredTokenMatcher matcher(LayeredSequence seq)
            throws SequenceException {
        String encoded = encodeSequence(seq);
        Matcher m = encodedPattern.matcher(encoded);
        return new LayeredTokenMatcher(m);
    }

    /**
     * Takes the given layered sequence object and encodes it using the
     * {@link Encoder} object of this instance.
     * 
     * @param seq
     * @return an encoded version of seq
     * @throws SequenceException
     */
    private String encodeSequence(LayeredSequence seq) throws SequenceException {
        int n = seq.getLength();
        char[] encoded = new char[n];
        for (int i = 0; i < n; i++) {
            String[] tuple = getTupleAt(seq, i);
            encoded[i] = encoder.encode(tuple);
        }
        return new String(encoded);
    }

    /**
     * Returns a vertical "slice" of the given layered sequence at index i. This
     * is a tuple containing each layer's value at index i.
     * 
     * @param seq
     * @param i
     * @return the tuple at index i
     * @throws SequenceException
     *             if unable to get the tuple
     */
    private String[] getTupleAt(LayeredSequence seq, int i)
            throws SequenceException {
        String[] tuple = new String[encoder.size()];
        for (int j = 0; j < layerNames.size(); j++) {
            String layerName = layerNames.get(j);
            if (seq.hasLayer(layerName)) {
                tuple[j] = seq.get(layerNames.get(j), i);
            } else {
                String msg = String.format(
                        "seq does not have layer with name '%s'", layerName);
                throw new SequenceException(msg);
            }
        }
        return tuple;
    }

    /**
     * @return the character-level pattern that this {@link LayeredTokenPattern}
     *         was compiled into.
     */
    public Pattern getEncodedPattern() {
        return encodedPattern;
    }

}
