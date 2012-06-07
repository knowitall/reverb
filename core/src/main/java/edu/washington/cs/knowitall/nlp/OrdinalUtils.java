package edu.washington.cs.knowitall.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.washington.cs.knowitall.commonlib.Range;

public final class OrdinalUtils {

    /**
     * Static members used for identifying ordinal numbers.
     */
    private static final String[] onesNumbers = { "zero", "one", "two",
            "three", "four", "five", "six", "seven", "eight", "nine", "ten",
            "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
            "seventeen", "eighteen", "nineteen" };

    private static final String[] tensNumbers = { "twenty", "thirty", "forty",
            "fifty", "sixty", "seventy", "eighty", "ninety" };

    private static final String[] scalesNumbers = { "hundred", "thousand",
            "million", "billion" };

    private static final int[] scalesInts = { 100, 1000, 1000000, 1000000000 };

    private static final String[] ordinalWords = { "first", "second", "third",
            "fifth", "eighth", "ninth", "twelfth" };

    private static final int[] ordinalWordValues = { 1, 2, 3, 5, 8, 9, 12 };

    private static final Pattern numericOrdSuffix = Pattern
            .compile("(\\d+)(st|nd|rd|th)$");

    private static HashMap<String, Integer> scales;
    private static HashMap<String, Integer> increments;
    private static HashMap<String, Integer> ordinals;
    private static HashMap<String, Integer> numberValues;
    private static boolean isInitialized = false;

    private static void init() {
        increments = new HashMap<String, Integer>();
        scales = new HashMap<String, Integer>();
        numberValues = new HashMap<String, Integer>();
        for (int i = 0; i < onesNumbers.length; i++) {
            increments.put(onesNumbers[i], i);
            numberValues.put(onesNumbers[i], i);
            scales.put(onesNumbers[i], 1);
        }
        for (int i = 0; i < tensNumbers.length; i++) {
            increments.put(tensNumbers[i], 10 * (i + 2));
            numberValues.put(tensNumbers[i], 10 * (i + 2));
            scales.put(tensNumbers[i], 1);
        }
        for (int i = 0; i < scalesNumbers.length; i++) {
            increments.put(scalesNumbers[i], 0);
            scales.put(scalesNumbers[i], scalesInts[i]);
            numberValues.put(scalesNumbers[i], scalesInts[i]);
        }

        ordinals = new HashMap<String, Integer>();
        for (int i = 0; i < ordinalWords.length; i++) {
            ordinals.put(ordinalWords[i], ordinalWordValues[i]);
        }

        isInitialized = true;
    }

    public static boolean isOrdinal(String s) {
        return parseOrdinal(s) != -1;
    }

    public static boolean isNumber(String s) {
        return parseNumber(s) != -1;
    }

    public static long parseNumber(String s) {
        if (!isInitialized)
            init();
        s = s.toLowerCase();
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            if (numberValues.containsKey(s)) {
                return numberValues.get(s);
            } else {
                return parseOrdinal(s);
            }
        }
    }

    public static boolean isBasicOrdinal(String word) {
        if (ordinals.containsKey(word)) {
            return true;
        } else {
            if (word.endsWith("ieth")) {
                word = word.substring(0, word.length() - 4) + "y";
            } else if (word.endsWith("th")) {
                word = word.substring(0, word.length() - 2);
            } else {
                return false;
            }
            return scales.containsKey(word) && increments.containsKey(word);
        }
    }

    public static int parseOrdinal(String s) {
        if (!isInitialized)
            init();

        s = s.replace('-', ' ').toLowerCase();

        // Is numeric with a suffix like -th
        Matcher m = numericOrdSuffix.matcher(s);
        if (m.matches()) {
            String intStr = m.group(1);
            return Integer.parseInt(intStr);
        }

        String[] words = s.split("\\s+");
        String lastWord = words[words.length - 1];
        if (!isBasicOrdinal(lastWord))
            return -1;

        int result = 0;
        int current = 0;
        for (String word : words) {
            int scale, increment;
            if (ordinals.containsKey(word)) {
                scale = 1;
                increment = ordinals.get(word);
            } else {
                if (word.endsWith("ieth")) {
                    word = word.substring(0, word.length() - 4) + "y";
                } else if (word.endsWith("th")) {
                    word = word.substring(0, word.length() - 2);
                }
                if (scales.containsKey(word) && increments.containsKey(word)) {
                    scale = scales.get(word);
                    increment = increments.get(word);
                } else {
                    return -1;
                }
            }
            current = current * scale + increment;
            if (scale > 100) {
                result += current;
                current = 0;
            }
        }

        return result + current;

    }

    public static List<Range> getOrdinalRanges(List<String> list) {
        return getOrdinalRanges(list.toArray(new String[] {}));
    }

    public static List<Range> getOrdinalRanges(ChunkedSentence sent) {
        return getOrdinalRanges(sent.getTokens());
    }

    public static List<Range> getOrdinalRanges(String[] tokens) {
        int n = tokens.length;
        List<Range> results = new ArrayList<Range>();
        for (int i = 0; i < n; i++) {

            while (i < n && !isNumber(tokens[i]))
                i++;

            if (i == n)
                break;

            int start = i;
            while (i < n && isNumber(tokens[i]))
                i++;

            if (isOrdinal(tokens[i - 1])) {
                Range r = new Range(start, i - start);
                results.add(r);
            }

        }
        return results;
    }

    public static String[] tagOrdinals(String[] tokens) {

        int n = tokens.length;
        String[] result = new String[n];
        for (int i = 0; i < n; i++)
            result[i] = "O";

        for (Range range : getOrdinalRanges(tokens)) {
            for (int i = range.getStart(); i < i + range.getLength(); i++) {
                result[i] = "ORD";
            }
        }

        return result;
    }

    public static String[] tagOrdinals(List<String> list) {
        return tagOrdinals(list.toArray(new String[] {}));
    }

    public static String[] tagOrdinals(ChunkedSentence sent) {
        return tagOrdinals(sent.getTokens());
    }

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line;
        while ((line = in.readLine()) != null) {
            String[] tokens = line.split("\\s+");
            String[] tags = tagOrdinals(tokens);
            for (int i = 0; i < tokens.length; i++) {
                System.out.print(tokens[i] + "/" + tags[i] + " ");
            }
            System.out.println();
        }
    }

}
